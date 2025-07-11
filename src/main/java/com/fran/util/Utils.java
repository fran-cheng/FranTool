package com.fran.util;


import org.jose4j.json.internal.json_simple.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 程良明
 * * 说明:工具
 **/
public class Utils {
	private static boolean info = true;

	public static void main(String[] args) {
		String apk = "APK´ò°ü";
		String apk2 = "APK合并";
		try {
			System.out.println(new String(apk.getBytes(StandardCharsets.ISO_8859_1), "GBK"));
			System.out.println(new String(apk2.getBytes("GBK"), StandardCharsets.ISO_8859_1));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 日志
	 *
	 * @param s String
	 */
	public static void log(String s) {
		System.out.println(s);
	}

	public static void logInfo(String s) {
		if (info) {
			System.out.println(s);
		}
	}

	/**
	 * 日志
	 *
	 * @param tag String
	 * @param s   String
	 */
	static void log(String tag, String s) {
		System.out.println(tag + " :  " + s);
	}

	/**
	 * SHA-1
	 *
	 * @param strSrc String
	 * @param len    String
	 * @return String
	 */
	public static String sha1(String strSrc, int len) {
		MessageDigest md;
		String strDes;
		byte[] bt = strSrc.getBytes();

		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(bt);
			strDes = bytes2Hex(md.digest());
		} catch (NoSuchAlgorithmException var6) {
			return null;
		}

		return len == 0 ? strDes : strDes.substring(0, len);
	}

	/**
	 * bytes2Hex
	 *
	 * @param bts byte
	 * @return String
	 */
	private static String bytes2Hex(byte[] bts) {
		StringBuilder stringBuilder = new StringBuilder();
		String tmp;

		for (byte bt : bts) {
			tmp = Integer.toHexString(bt & 255);
			if (tmp.length() == 1) {
				stringBuilder.append("0");
			}

			stringBuilder.append(tmp);
		}

		return stringBuilder.toString();
	}

	/**
	 * 生成随机数
	 *
	 * @param min min
	 * @param max max
	 * @return int
	 */
	public static int random(int min, int max) {
		return min + (int) Math.round(Math.random() * (double) (max - min));
	}

	/**
	 * 写文件
	 *
	 * @param file    File
	 * @param content String
	 * @param charset String
	 */
	public static void writeFile(File file, String content, String charset) {
		OutputStream os;

		try {
			if (!file.exists()) {
				makeIfDir(file);
				file.createNewFile();
			}

			os = new FileOutputStream(file);
			os.write(content.getBytes(charset));
			os.close();
		} catch (Exception var6) {
			var6.printStackTrace();
		}
	}

	/**
	 * 生成dir
	 *
	 * @param f File
	 */
	private static void makeIfDir(File f) {
		if (f.isDirectory()) {
			if (!f.exists()) {
				f.mkdirs();
			}
		} else {
			File dir = f.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}

	}

	/**
	 * 读文件
	 *
	 * @param f File
	 * @return String
	 */
	public static String read(File f) {
		String c = null;

		try {
			InputStream fis = new FileInputStream(f);
			c = inputStreamToString(fis);
			fis.close();
		} catch (Exception var5) {
			var5.printStackTrace();
		}

		return c;
	}

	/**
	 * inputStreamToString
	 *
	 * @param is InputStream
	 * @return String
	 */
	private static String inputStreamToString(InputStream is) {

		Scanner scanner = new Scanner(is);
		StringBuilder stringBuilder = new StringBuilder();
		while (scanner.hasNext())
			stringBuilder.append(scanner.next());


		return stringBuilder.toString();
	}

	/**
	 * 拼接地址
	 *
	 * @param basePath 基础路径
	 * @return path
	 */


	public static String linkPath(String basePath, String... dentrys) {
		StringBuilder stringBuilder = new StringBuilder(basePath);
		for (String dentry : dentrys) {
			stringBuilder.append(File.separator);
			stringBuilder.append(dentry);
		}
		return stringBuilder.toString();
	}

	public static void copyFiles(File tempFile, File outPutFile) {

		if (tempFile.isDirectory()) {
			for (File file : Objects.requireNonNull(tempFile.listFiles())) {
				copyFiles(file, new File(outPutFile, file.getName()));
			}
		} else {
			if (!outPutFile.getParentFile().exists()) {
				outPutFile.getParentFile().mkdirs();
			}
			copyOperation(tempFile, outPutFile);
		}
	}

	/**
	 * 拷贝的具体操作
	 *
	 * @param tempFile   输入
	 * @param outPutFile 输出
	 */
	private static void copyOperation(File tempFile, File outPutFile) {
		try (FileInputStream fis = new FileInputStream(tempFile);
			 FileOutputStream fos = new FileOutputStream(outPutFile)) {

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void delDir(File file) {
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			assert list != null;
			for (File f : list) {
				delDir(f);
			}
		}
		file.delete();
	}

	public static byte[] getBytes(File file) throws Exception {
		RandomAccessFile r = new RandomAccessFile(file, "r");
		byte[] buffer = new byte[(int) r.length()];
		r.readFully(buffer);
		r.close();
		return buffer;
	}

	public static void writeFile(File file, byte[] bytes) throws Exception {
		RandomAccessFile r = new RandomAccessFile(file, "rw");
		r.write(bytes);
		r.close();
	}


	/**
	 * 通过 readelf -l 命令获取 .so 文件的对齐大小
	 *
	 * @param soPath .so 文件绝对路径
	 * @return "4KB对齐"、"16KB对齐" 或错误信息
	 */
	public static String checkAlignWithReadElf(String soPath) {
		JSONObject jsonObject = new JSONObject();
		// 校验文件存在性
		File soFile = new File(soPath);
		if (!soFile.exists()) {
			return "错误：文件不存在 - " + soPath;
		}
		if (!soFile.canRead()) {
			return "错误：无读取权限 - " + soPath;
		}
		checkAlignWithReadElf(soFile, jsonObject);

		return jsonObject.toJSONString();
	}

	private static void checkAlignWithReadElf(File file, JSONObject jsonObject) {
		if (file.isDirectory()) {
//			遍历，返回json
			File[] files = file.listFiles();
			for (File f : files) {
				checkAlignWithReadElf(f, jsonObject);
			}
		} else {
			String key = linkPath(file.getParentFile().getName(), file.getName());
			String value = parseReadElfOutput(RuntimeHelper.getInstance().run("readelf -l " + file.getPath(), false));
			jsonObject.put(key, value);
		}
	}

	/**
	 * 解析 readelf -l 的输出，提取 LOAD 段的 Align 值
	 */
	private static String parseReadElfOutput(String output) {
		// 匹配 LOAD 段及其后续行的正则表达式
		Pattern loadPattern = Pattern.compile(
						"LOAD\\s+" +                      // 匹配"LOAD"开头
										"0x[0-9a-fA-F]+\\s+" +           // 匹配Offset
										"0x[0-9a-fA-F]+\\s+" +           // 匹配VirtAddr
										"0x[0-9a-fA-F]+\\s+" +           // 匹配PhysAddr
										"0x[0-9a-fA-F]+\\s+" +           // 匹配FileSiz
										"0x[0-9a-fA-F]+\\s+" +           // 匹配MemSiz
										"[RWE ]+\\s+" +                  // 匹配Flags
										"0x([0-9a-fA-F]+)"               // 匹配Align并捕获值
		);

		Matcher loadMatcher = loadPattern.matcher(output);

		// 收集所有LOAD段的对齐值
		Set<String> alignValues = new HashSet<>();
		while (loadMatcher.find()) {
			alignValues.add(loadMatcher.group(1));
		}

		// 检查对齐值是否一致
		if (alignValues.isEmpty()) {
			return "未找到LOAD段，可能不是有效的ELF共享库";
		} else if (alignValues.size() > 1) {
			return "警告：多个LOAD段对齐值不一致：" + alignValues;
		} else {
			String alignHex = alignValues.iterator().next();
			long align = Long.parseLong(alignHex, 16);

			if (align == 0x1000) {
				return "4KB对齐";
			} else if (align == 0x4000) {
				return "16KB对齐";
			} else {
				return "对齐值：" + align + "字节（" + (align / 1024) + "KB）";
			}
		}
	}

}
