package com.fran.util;


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
import java.util.Objects;
import java.util.Scanner;

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
}
