package com.fran.aab;

import com.fran.util.RuntimeHelper;
import com.fran.util.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import brut.androlib.Config;
import brut.common.BrutException;
import brut.util.AaptManager;
import brut.util.Jar;

/**
 * @author 程良明
 * @date 2023/9/8
 * * * 说明:从Apktool解包路径下生成aab
 * 参考https://juejin.cn/post/6982111395621896229
 **/

public class Apk2Aab {
	public static void main(String[] args) throws IOException {

		Apk2Aab aab = new Apk2Aab();
//		aab.compile();
//		aab.linkSources();
//		aab.unZipBase();
		aab.copySources();
		// TODO: 2023/9/8 解压 base.apk
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
	private void compile() {
		String aaptPath = getAapt2Path();
		String apkDecodePath = "D:\\FranGitHub\\FranTool\\runtime\\20230922-X2-gf";
		String cmdCompile = String.format("%s compile --legacy --dir %s -o compiled_resources.zip", aaptPath, Utils.linkPath(apkDecodePath, "res"));
		try {
			RuntimeHelper.getInstance().run(cmdCompile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关联资源
	 */
	private void linkSources() {

		String apkDecodePath = "D:\\FranGitHub\\FranTool\\runtime\\20230922-X2-gf";
		String aaptPath = getAapt2Path();
		String androidJarPath = "D:\\FranGitHub\\FranTool\\tool\\aab-tool\\android.jar";
//		String androidJarPath = Config.getDefaultConfig().frameworkDirectory;

		String minVersion = "24";
		String targetVersion = "31";
		String versionCode = "1";
		String versionName = "1.0.0";
		String cmdLink = String.format("%s link --proto-format -o base.apk -I %s --min-sdk-version %s --target-sdk-version %s --version-code %s --version-name %s --manifest %s -R compiled_resources.zip --auto-add-overlay",
						aaptPath, androidJarPath, minVersion, targetVersion, versionCode, versionName, Utils.linkPath(apkDecodePath, "AndroidManifest.xml"));
		try {
			RuntimeHelper.getInstance().run(cmdLink);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 解压base.apk
	 * 通过unzip解压到base文件夹，目录结构：
	 */
	private String unZipBase() {

		String zipFilePath = "D:\\FranGitHub\\FranTool\\base.apk";
		String destDirectory = "D:\\FranGitHub\\FranTool\\runtime\\20230922-X2-gf\\base";
		byte[] buffer = new byte[1024];
		try {
			// 创建解压缩输入流
			FileInputStream fis = new FileInputStream(zipFilePath);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				String fileName = zipEntry.getName();
				File newFile = new File(destDirectory + File.separator + fileName);
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
	private void copySources() throws IOException {
//		创建 base/manifest 将 base/AndroidManifest.xml 剪切过来
		File manifesFile = new File("");

		String apkDecodeBasePath = "D:\\FranGitHub\\FranTool\\runtime\\20230922-X2-gf\\base";
		String apkDecodePath = "D:\\FranGitHub\\FranTool\\runtime\\20230922-X2-gf";

		File androidManifestFile = new File(Utils.linkPath(apkDecodeBasePath, "AndroidManifest.xml"));
		if (androidManifestFile.exists()) {
			Utils.copyOperation(androidManifestFile, new File(Utils.linkPath(apkDecodeBasePath, "manifest", "AndroidManifest.xml")));
		}

		File assetsFile = new File(Utils.linkPath(apkDecodePath, "assets"));
		if (assetsFile.exists()) {
			Utils.copyOperation(assetsFile, new File(Utils.linkPath(apkDecodeBasePath, "assets")));
		}

		File libFile = new File(Utils.linkPath(apkDecodePath, "lib"));
		if (libFile.exists()) {
			Utils.copyOperation(libFile, new File(Utils.linkPath(apkDecodeBasePath, "lib")));
		}

		File unknownFile = new File(Utils.linkPath(apkDecodePath, "unknown"));
		if (unknownFile.exists()) {
			Utils.copyOperation(unknownFile, new File(Utils.linkPath(apkDecodeBasePath, "root")));
		}


		File kotlinFile = new File(Utils.linkPath(apkDecodePath, "kotlin"));
		if (kotlinFile.exists()) {
			Utils.copyOperation(kotlinFile, new File(Utils.linkPath(apkDecodeBasePath, "root")));
		}

	}
}
