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
public class Apk2Aab {
	public static void main(String[] args) {
		String apkDecodePath = "";
		String cmdCompile = String.format("aapt2 compile --dir %s -o compiled_resources.zip", Utils.linkPath(apkDecodePath, "res"));
		String androidJarPath = "";
		String minVersion = "";
		String targetVersion = "";
		String versionCode = "";
		String versionName = "";
		String cmdLink = String.format("aapt2 link --proto-format -o base.apk -I %s \\--min-sdk-version %s --target-sdk-version %s \\--version-code $s --version-name %s \\--manifest %s \\-R compiled_resources.zip --auto-add-overlay",
						androidJarPath,minVersion,targetVersion,versionCode,versionName,Utils.linkPath(androidJarPath,"AndroidManifest.xml"));
		try {
			RuntimeHelper.getInstance().run(cmdCompile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO: 2023/9/8 解压 base.apk
	}


	private void copySources(){
//		创建 base/manifest 将 base/AndroidManifest.xml 剪切过来
		File manifesFile = new File("");
	}
}
