package com.fran;


import com.fran.tool.AESTool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) throws Exception {

		String path = "D:\\FranGitHub\\FranTool\\out\\apk\\app-release";
//		ApkBuild.main(new String[]{"apkBuild", path});
		String buildPath = "D:\\FranGitHub\\FranTool\\out\\apk\\app-release\\build\\apk";

		AESTool aesTool = new AESTool();
//		原始
		String classesDex = "D:\\FranGitHub\\FranTool\\out\\apk\\app-release\\build\\apk\\classes.dex";
//		加密后
		String classesXh = "D:\\FranGitHub\\FranTool\\out\\apk\\app-release\\build\\apk\\classes.xh";

		//		解密后
		String classesXhDex = "D:\\FranGitHub\\FranTool\\out\\apk\\app-release\\build\\apk\\xh.dex";

//		加密操作
		byte[] classesDexAllByte = new FileInputStream(classesDex).readAllBytes();
		String encryptStrBase64 = aesTool.encrypt(classesDexAllByte);
		try {
			// 先将Base64编码的字符串解码为字节数组
			byte[] encryptBytes = encryptStrBase64.getBytes(StandardCharsets.UTF_8);
			// 使用文件输出流写入到文件
			FileOutputStream fos = new FileOutputStream(classesXh);
			fos.write(encryptBytes);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


//		解密操作
		byte[] classesXhAllByte = new FileInputStream(classesXh).readAllBytes();
		byte[] decryptStrBase64 = aesTool.decrypt(new String(classesXhAllByte));
		try {
			// 先将Base64编码的字符串解码为字节数组
			byte[] decryptBytes = decryptStrBase64;
			// 使用文件输出流写入到文件
			FileOutputStream fos = new FileOutputStream(classesXhDex);
			fos.write(decryptBytes);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}