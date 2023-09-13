package com.fran.aab;

import com.fran.util.RuntimeHelper;
import com.fran.util.Utils;

import java.io.File;

/**
 * @author 程良明
 * @date 2023/9/8
 * * * 说明:从Apktool解包路径下生成aab
 * 参考https://juejin.cn/post/6982111395621896229
 **/

@Deprecated // TODO: 2023/9/13 时间关系，先使用了  apk2aar 打包，后续再研究 
public class Apk2Aab {
	public static void main(String[] args) {

		Apk2Aab aab = new Apk2Aab();
		aab.compile();
//		aab.linkSources();
		// TODO: 2023/9/8 解压 base.apk
	}


	/**
	 * 编译资源
	 */
	private void compile() {
		String aaptPath = "D:\\FranGitHub\\FranTool\\tool\\aab-tool\\aapt2.exe";
		String apkDecodePath = "D:\\FranGitHub\\FranTool\\Runtime\\app-debug";
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
		String apkDecodePath = "D:\\FranGitHub\\FranTool\\Runtime\\app-debug";
		String aaptPath = "D:\\FranGitHub\\FranTool\\tool\\aab-tool\\aapt2.exe";
		String androidJarPath = "D:\\FranGitHub\\FranTool\\tool\\aab-tool\\android.jar";
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
	 */
	private void unZipBase() {
		String aaptPath = "D:\\FranGitHub\\FranTool\\tool\\aab-tool\\aapt2.exe";
		String unZipPath = "D:\\FranGitHub\\FranTool\\tool\\aab-tool\\aapt2.exe";
		String apkDecodePath = "D:\\FranGitHub\\FranTool\\Runtime\\app-debug";
		String cmdUnZip = String.format("%s compile --dir %s -o compiled_resources.zip", aaptPath, Utils.linkPath(apkDecodePath, "res"));
		try {
			RuntimeHelper.getInstance().run(cmdUnZip);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝资源
	 */
	private void copySources() {
//		创建 base/manifest 将 base/AndroidManifest.xml 剪切过来
		File manifesFile = new File("");
	}
}
