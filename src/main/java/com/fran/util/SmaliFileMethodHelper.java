package com.fran.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 程良明
 * @date 2024/3/11
 * * * 说明:计算smali文件的最大方法数
 **/
public class SmaliFileMethodHelper {

	public static final SmaliFileMethodHelper getInstance() {
		return InnerHolder.sInstance;
	}

	public static void main(String[] args) {
		String classPath = "E:\\www\\hw\\10007-240307120819429111809\\smali";
		Set<String> methodCount = SmaliFileMethodHelper.getInstance().getMethodCount(classPath);
		System.out.println("clm count : " + methodCount);
		System.out.println("clm count : " + methodCount.size());
	}

	public Set<String> getMethodCount(String path) {
		return getMethodCount(new File(path));
	}

	public Set<String> getMethodCount(File file) {
		getMethodCountByDir(file);
		return mMethodCount;
	}

	public void getMethodCountByDir(File file) {
		if (file.isDirectory()) {
			File[] listFile = file.listFiles();
			if (listFile != null) {
				for (File tempFile : listFile) {
					getMethodCountByDir(tempFile);
				}
			}
		} else {
			getMethodCountByFile(file);
		}
	}

	private void getMethodCountByFile(File file) {
		if (!file.isFile()) {
			return;
		}
		String content = readFileFromLine(file);
		processMethodCount(content);
	}

	private Set<String> mMethodCount = new HashSet<>();

	private void processMethodCount(String content) {
		String className = getClassByFile(content);
		String regex = "\\.method\\s.+|invoke-.*->.*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String line = matcher.group();
			String targetStr;
			if (line.startsWith(".method")) {
				targetStr = makeMethodInvoke(line, className);
			} else {
				targetStr = parseMethodInvoke(line);
			}
			mMethodCount.add(targetStr.trim());
		}
	}

	private String parseMethodInvoke(String line) {
		return line.substring(line.lastIndexOf(" "));
	}

	private String makeMethodInvoke(String line, String className) {
		return className + "->" + line.substring(line.lastIndexOf(" ")).trim();
	}

	private String readFileFromLine(File file) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line).append("\r");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	private String getClassByFile(String content) {
		String className = "";
		String regex = "\\.class.+";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			String line = matcher.group();
			className = line.substring(line.indexOf(" L"));
		}
		return className;
	}

	private SmaliFileMethodHelper() {

	}

	private static class InnerHolder {
		private static SmaliFileMethodHelper sInstance = new SmaliFileMethodHelper();
	}
}
