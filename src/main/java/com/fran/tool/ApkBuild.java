package com.fran.tool;


import com.fran.aab.Apk2Aab;
import com.fran.merge.MergeBase;
import com.fran.util.RuntimeHelper;
import com.fran.util.Sign;
import com.fran.util.Utils;

import org.dom4j.Document;

import java.io.File;
import java.util.Scanner;

/**
 * @author 程良明
 * * 说明:主入口
 **/
public class ApkBuild {


	public static void main(String[] args) throws Exception {
		ApkBuild apkBuild = new ApkBuild();
		if (args.length != 2) {
			throw new RuntimeException("参数不对!");
		}
//
		String key = args[0];

		String path = args[1];
		if (key.equalsIgnoreCase("apkDecompress")) {
			apkBuild.apkDecompress(path);

		} else if (key.equalsIgnoreCase("apkBuild")) {
			apkBuild.deleteBuild(path);
			apkBuild.apkToolBuild(path);
			apkBuild.sign(path);
		} else if (key.equalsIgnoreCase("key")) {
			new KeyTool(path);
		} else if (key.equalsIgnoreCase("apkMerge")) {
			Utils.log("输入需要合并的文件路径: ");
			String pluginPath = new Scanner(System.in).next();
			new MergeBase(path, pluginPath) {
				@Override
				protected void processManiFestXml(Document workDocument) {

				}
			}.merge();
		} else if (key.equalsIgnoreCase("apk2aab")) {
			new Apk2Aab(path).process();
		} else if (key.equalsIgnoreCase("aabInstall")) {
			new Apk2Aab(new File(path).getParent()).installAab(path);
		} else if (key.equalsIgnoreCase("dexEncrypt")) {
			apkBuild.dexEncrypt(path);
		}

		Utils.log("Done!");
	}


	/**
	 * 调用解包
	 *
	 * @param apkFilePath 待解包APK
	 */
	private void apkDecompress(String apkFilePath) {
		String cmd = String.format("apktool d -f --only-main-classes %s ", apkFilePath);
		try {
			RuntimeHelper.getInstance().run(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 调用回编
	 *
	 * @param apkDirPath 待回编APK
	 */
	private void apkToolBuild(String apkDirPath) {
		String cmd = String.format("apktool b %s", apkDirPath);

		try {
			RuntimeHelper.getInstance().run(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对整个apk进行加壳
	 */

	private void apkEncrypt() {
		// TODO: 2025/1/8 直接把apk整个加密隐藏 ，可能涉及很多问题，后续看情况是否实现，理论上跟热更差不多
	}

	/**
	 * 对dex进行加密
	 */
	private void dexEncrypt(String dir) throws Exception {
		//修改入口，或者application 确保加载的时候解密dex
		AESTool aesTool = new AESTool();
		aesTool.changeManifestXml(dir);

		// TODO: 2025/1/7 做白名单，根据类路径来确保部分不能在解密后加载的dex，移动到壳包

		//   回编译
		apkToolBuild(dir);
		String buildApkPath = Utils.linkPath(dir, "build", "apk");
		//对dex进行加密，以及合并到同一个文件？
		File[] dexFiles = new File(buildApkPath).listFiles((file, s) -> {
			String fileName = s.toLowerCase();
			return fileName.endsWith("dex");
		});
//		加密dex
		assert dexFiles != null;
		for (File file : dexFiles) {
			String context = aesTool.encryptFile(file);
			Utils.writeFile(new File(file.getParent(), file.getName().replace("dex", "xed")), context, "utf-8");
			file.delete();
		}
		Utils.copyFiles(new File("D:\\FranGitHub\\FranTool\\out\\apk\\shellDex\\classes.dex"), new File(buildApkPath, "classes.dex"));
		File[] distFiles = new File(dir, "dist").listFiles((file, s) -> {
			String fileName = s.toLowerCase();
			return fileName.endsWith("apk");
		});
		assert distFiles != null;
		File outFile = distFiles[0];
//		重新压缩apk
		Utils.zip(new File(buildApkPath), outFile);
//		对齐，签名
		zipalign(dir);
		sign(dir);
	}

	/**
	 * 签名
	 *
	 * @param dir 待签名APK
	 */
	private void sign(String dir) {
		String outApk = zipalign(dir);
		new Sign(dir, outApk);
	}

	/**
	 * 对齐
	 *
	 * @param dir 待对齐APK
	 */
	private String zipalign(String dir) {
		String name = new File(dir).getName();
		File distFile = new File(Utils.linkPath(dir, "dist"));

		File[] files = distFile.listFiles();
		assert files != null;
		String buildApk = files[0].getPath();

		String outApk = Utils.linkPath(dir, name + "_temp.apk");

		String cmd = String.format("zipalign -f 4 %s %s", buildApk, outApk);
		RuntimeHelper.getInstance().run(cmd);

		return outApk;
	}

	/**
	 * 删除中间的构建文件
	 *
	 * @param dir 工作路径
	 */
	private void deleteBuild(String dir) {
		File build = new File(Utils.linkPath(dir, "build"));
		File dist = new File(Utils.linkPath(dir, "dist"));
		if (build.exists()) {
			deleteDir(build);
		}
		if (dist.exists()) {
			deleteDir(dist);
		}
	}

	/**
	 * 删除文件，文件夹
	 *
	 * @param file 删除路径
	 */
	private void deleteDir(File file) {
		if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			if (fileList != null) {
				for (File f : fileList) {
					deleteDir(f);
				}
			}
		}
		file.delete();
	}

}
