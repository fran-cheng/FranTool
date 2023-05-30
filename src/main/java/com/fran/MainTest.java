package com.fran;


import com.fran.util.Utils;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 程良明
 * @date 2023/4/7
 * * * 说明:测试用
 **/
public class MainTest {
	public static void main(String[] args) {


		String str = Utils.read(new File("/Users/fran/JavaProject/FranTool/YYGGames.js"));

		Map<String, String> map = getHexStrFormString(str);

		for (String key : map.keySet()) {
			str = str.replace(key, map.get(key));
		}

		Utils.writeFile(new File("/Users/fran/JavaProject/FranTool/Clm_YYGGames.js"), str, "utf-8");

	}

	/**
	 * 提取16进制 '\x 开头的字符串，并转换成string
	 *
	 * @param str 输入
	 * @return Map<String, String>   16进制字符串与string的映射
	 */
	private static Map<String, String> getHexStrFormString(String str) {
		Map<String, String> hexStrMapStr = new HashMap<>();
		String key = "\\x";
		int tempStartIndex = 0;
		while (true) {
			int startIndex = str.indexOf("'" + key, tempStartIndex);
			tempStartIndex = startIndex + 1;
			int endIndex = str.indexOf("'", tempStartIndex);
			if (startIndex < 0 || endIndex < 0) {
				break;
			}
			String targetStr = str.substring(tempStartIndex, endIndex);
			tempStartIndex = endIndex;
			hexStrMapStr.put(targetStr, hexStr2String(targetStr));
		}

		return hexStrMapStr;
	}

	private static void test() {
		// TODO: 2023/5/6 字符串转16进制

		String str = "程良明";

		String builder = str2HexString(str);

		System.out.println("clm 2bin : " + builder);

		String origin = hexStr2String(builder);
		System.out.println("clm origin: " + origin);

		String hexStr = HexBin.encode(str.getBytes(StandardCharsets.UTF_8));
		System.out.println("clm hexStr: " + hexStr);
		System.out.println("clm hexStr2: " + new String(HexBin.decode(hexStr), StandardCharsets.UTF_8));
	}

	/**
	 * 字符串转16进制
	 *
	 * @param str
	 * @return
	 */
	private static String str2HexString(String str) {
		StringBuilder builder = new StringBuilder();
		for (char c : str.toCharArray()) {
			builder.append("\\x").append(Integer.toHexString(c));
		}
		return builder.toString();
	}

	private static String hexStr2String(String hexStr) {
		StringBuilder builder = new StringBuilder();
		String[] strings = hexStr.split("\\\\x");

		for (String str : strings) {
			if (str.length() > 0) {
				builder.append(str);
			}
		}
		System.out.println("clm hexStr2String: " + hexStr);
		System.out.println("clm builder: " + builder.toString());
		String target = new String(HexBin.decode(builder.toString().replace("\\", "")), StandardCharsets.UTF_8);
		System.out.println("clm hexStr2StringTarget: " + target);
		return target;
	}

}