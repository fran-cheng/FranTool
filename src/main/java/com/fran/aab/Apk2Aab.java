package com.fran.aab;

import com.fran.tool.KeyTool;
import com.fran.util.RuntimeHelper;
import com.fran.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import brut.androlib.apk.ApkInfo;
import brut.androlib.exceptions.AndrolibException;
import brut.common.BrutException;
import brut.util.AaptManager;

/**
 * @author 程良明
 * @date 2023/9/8
 * * * 说明:从Apktool解包路径下生成aab
 * 参考https://juejin.cn/post/6982111395621896229
 **/

public class Apk2Aab {
	private final String mApkDecodePath;
	private final String mWorkPath;
	private String mRootPath;

	public static void main(String[] args) throws IOException {
		Apk2Aab aab = new Apk2Aab("D:\\FranGitHub\\FranTool\\runtime\\app-debug");
//		aab.process();
		aab.installAab("D:\\FranGitHub\\FranTool\\runtime\\app-debug");
	}


	/**
	 * 构造方法
	 *
	 * @param apkDecodePath apktool解包后的文件路径
	 */
	public Apk2Aab(String apkDecodePath) {
		String classPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
		if (classPath.endsWith(".jar")) {
			File jarFile = new File(classPath);
			mRootPath = jarFile.getParent();
		} else {
			mRootPath = "D:\\FranGitHub\\FranTool\\tool";
		}
		mApkDecodePath = apkDecodePath;
		mWorkPath = Utils.linkPath(apkDecodePath, "fran_aab_work");
		File workFile = new File(mWorkPath);
		if (workFile.exists()) {
			Utils.delDir(workFile);
		}
		workFile.mkdirs();
	}

	public void installAab(String aabFilePath) {
		String bundleToolPath = Utils.linkPath(mRootPath, "aab-tool", "bundletool.jar");
		File aabFile = new File(aabFilePath);
		String apksPath = Utils.linkPath(aabFile.getParent(), aabFile.getName().replace(".aab", "") + ".apks");
		if (!new File(apksPath).exists()) {
			String[] key = KeyTool.findSignInfo(aabFile.getParentFile(), false);
			String cmdApks;
			if (key == null) {
				cmdApks = String.format("java -jar %s build-apks --bundle=%s --output=%s", bundleToolPath, aabFilePath, apksPath);
			} else {
				cmdApks = String.format("java -jar %s build-apks --bundle=%s --output=%s --ks=%s --ks-key-alias=%s --ks-pass=pass:%s --key-pass=pass:%s", bundleToolPath, aabFilePath, apksPath, key[0], key[2], key[1], key[1]);
			}


//		生成apks
			RuntimeHelper.getInstance().run(cmdApks);
		}


		String adbCmd = "adb devices";

		String devices = RuntimeHelper.getInstance().run(adbCmd);
		String[] deviceList = devices.split("\n");
		if (deviceList.length < 2) {
			Utils.log("请先链接设备");
		} else {
			//			默认第一个
			String deviceName = deviceList[1].replace("device", "").trim();
			if (deviceList.length > 2) {
				Utils.log("请先选择要安装的设备id或者名称");
				String str = new Scanner(System.in).next();
				try {
					int index = Integer.parseInt(str);
					deviceName = deviceList[index].replace("device", "").trim();
				} catch (Exception ignored) {
					if (devices.contains(str)) {
						deviceName = str;
					} else {
						Utils.log("输入有误，选择默认的设备 : " + deviceName);
					}
				}
			}
			Utils.log("正在安装的设备是 : " + deviceName);

			//		安装apk
			String cmdInstallApks = String.format("java -jar %s install-apks --apks=%s --device-id=%s", bundleToolPath, apksPath, deviceName);

			RuntimeHelper.getInstance().run(cmdInstallApks);
		}


	}

	public void process() throws IOException {
		String compileFIlePath = compile();
		String baseApkPath = linkSources(compileFIlePath);
		String basePath = unZipBase(baseApkPath);
		copySources(basePath);
		generateAAB(basePath);
	}

	private void generateAAB(String baseUnZipPath) {
		String bundleToolPath = Utils.linkPath(mRootPath, "aab-tool", "bundletool.jar");
		String outPutAabPath = Utils.linkPath(mWorkPath, "base.aab");
		File baseZipFile = new File(Utils.linkPath(mWorkPath, "base.zip"));
		try {
			FileOutputStream fos = new FileOutputStream(baseZipFile);
			ZipOutputStream zipOut = new ZipOutputStream(fos, StandardCharsets.UTF_8);
			File[] fileToZip = new File(baseUnZipPath).listFiles();

			assert fileToZip != null;
			for (File file : fileToZip) {
				zipFile(file, file.getName(), zipOut);
			}


			zipOut.close();
			fos.close();

			System.out.println("文件压缩成功！");
		} catch (IOException e) {
			e.printStackTrace();
		}


		String cmd = String.format("java -jar %s build-bundle --modules=%s --output=%s", bundleToolPath, baseZipFile.getPath(), outPutAabPath);
		RuntimeHelper.getInstance().run(cmd);

		String dir = mApkDecodePath;
		String[] info = KeyTool.findSignInfo(new File(dir));
		String name = new File(dir).getName();
		String outSignAab = Utils.linkPath(dir, name + "_sign.aab");

		String keystorefile = info[0];
		String password = info[1];


		String s = String.format("jarsigner -keystore %s -storepass %s -sigalg MD5withRSA -digestalg SHA1 -signedjar %s %s %s", keystorefile, password, outSignAab, outPutAabPath, info[2]);

		RuntimeHelper.getInstance().run(s);

//		Utils.delDir(new File(mWorkPath));
	}


	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			File[] children = fileToZip.listFiles();
			for (File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
			}
			return;
		}

		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);

		byte[] bytes = new byte[4096];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}

		fis.close();
	}

	private String getAapt2Path() {
		try {
			File aapt = AaptManager.getAapt2();
			return aapt.getPath();
		} catch (BrutException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 编译资源
	 */
	private String compile() {
		String compileFIlePath = Utils.linkPath(mWorkPath, "compiled_resources.zip");
		String aaptPath = getAapt2Path();
		String cmdCompile = String.format("%s compile --legacy --dir %s -o %s", aaptPath, Utils.linkPath(mApkDecodePath, "res"), compileFIlePath);
		try {
			RuntimeHelper.getInstance().run(cmdCompile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compileFIlePath;
	}

	/**
	 * 关联资源
	 */
	private String linkSources(String compileFIlePath) {
		String outBaseApk = Utils.linkPath(mWorkPath, "base.apk");
		String aaptPath = getAapt2Path();
		String androidJarPath = Utils.linkPath(mRootPath, "aab-tool", "android.jar");

		String minVersion = "21";
		String targetVersion = "33";
		String versionCode = "1";
		String versionName = "1.0";

		File apkdecodeFile = new File(mApkDecodePath);
		try {
			ApkInfo apkInfo = ApkInfo.load(apkdecodeFile);
			minVersion = apkInfo.getMinSdkVersion();
			targetVersion = apkInfo.getTargetSdkVersion();
			versionCode = apkInfo.versionInfo.versionCode;
			versionName = apkInfo.versionInfo.versionName;
		} catch (AndrolibException e) {
			throw new RuntimeException(e);
		}


		String cmdLink = String.format("%s link --proto-format -o %s -I %s --min-sdk-version %s --target-sdk-version %s --version-code %s --version-name %s --manifest %s -R %s --auto-add-overlay",
						aaptPath, outBaseApk, androidJarPath, minVersion, targetVersion, versionCode, versionName, Utils.linkPath(mApkDecodePath, "AndroidManifest.xml"), compileFIlePath);
		try {
			RuntimeHelper.getInstance().run(cmdLink);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return outBaseApk;
	}


	/**
	 * 解压base.apk
	 * 通过unzip解压到base文件夹，目录结构：
	 */
	private String unZipBase(String apkFilePath) {

		String destDirectory = Utils.linkPath(mWorkPath, "base");
		byte[] buffer = new byte[4096];
		try {
			// 创建解压缩输入流
			FileInputStream fis = new FileInputStream(apkFilePath);
			ZipInputStream zis = new ZipInputStream(fis, StandardCharsets.UTF_8);
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				File newFile;
				if (fileName.endsWith("AndroidManifest.xml")) {
					newFile = new File(Utils.linkPath(destDirectory, "manifest", fileName));
				} else {
					newFile = new File(destDirectory + File.separator + fileName);
				}


				// 创建目录
				new File(newFile.getParent()).mkdirs();
				FileOutputStream fos = new FileOutputStream(newFile);
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				zipEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
			fis.close();
			System.out.println("解压缩完成");
		} catch (IOException e) {
			e.printStackTrace();
		}


		return destDirectory;
	}

	/**
	 * 拷贝资源
	 */
	private void copySources(String basePath) throws IOException {

		String apkDecodeBasePath = basePath;
		String apkDecodePath = mApkDecodePath;

		File assetsFile = new File(Utils.linkPath(apkDecodePath, "assets"));
		if (assetsFile.exists()) {
			Utils.copyFiles(assetsFile, new File(Utils.linkPath(apkDecodeBasePath, "assets")));
		}

		File libFile = new File(Utils.linkPath(apkDecodePath, "lib"));
		if (libFile.exists()) {
			Utils.copyFiles(libFile, new File(Utils.linkPath(apkDecodeBasePath, "lib")));
		}

		File unknownFile = new File(Utils.linkPath(apkDecodePath, "unknown"));
		if (unknownFile.exists()) {
			Utils.copyFiles(unknownFile, new File(Utils.linkPath(apkDecodeBasePath, "root")));
		}


		File kotlinFile = new File(Utils.linkPath(apkDecodePath, "kotlin"));
		if (kotlinFile.exists()) {
			Utils.copyFiles(kotlinFile, new File(Utils.linkPath(apkDecodeBasePath, "root", "kotlin")));
		}


		File buildFile = new File(Utils.linkPath(apkDecodePath, "build", "apk"));
		if (!buildFile.exists()) {
			String cmd = String.format("apktool b %s", apkDecodePath);

			try {
				RuntimeHelper.getInstance().run(cmd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		File[] dexFiles = buildFile.listFiles((file1, s) -> s.startsWith("classes") && s.endsWith(".dex"));
		for (File dexFile : dexFiles) {
			Utils.copyFiles(dexFile, new File(Utils.linkPath(apkDecodeBasePath, "dex", dexFile.getName())));
		}
	}
}
