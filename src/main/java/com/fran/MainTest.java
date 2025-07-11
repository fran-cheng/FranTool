package com.fran;


import com.fran.util.Utils;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) throws Exception {


		String path = "F:\\Downloaded\\20250707102347-X5_vn\\lib\\arm64-v8a";


//		String cmd = "readelf -l " + path;
////		ApkBuild.main(new String[]{"dexEncrypt", path});
//
//		String string = RuntimeHelper.getInstance().run(cmd);
//
//		System.out.println("clm");
		System.out.println(Utils.checkAlignWithReadElf(path));
	}

}