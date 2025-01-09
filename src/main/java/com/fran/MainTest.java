package com.fran;


import com.fran.tool.ApkBuild;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) throws Exception {


		String path = "D:\\FranGitHub\\FranTool\\out\\apk\\app-release";


		ApkBuild.main(new String[]{"dexEncrypt", path});

	}


}