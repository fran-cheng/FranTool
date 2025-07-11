package com.fran.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author 程良明
 * * 说明:调用命令行工具
 **/
public class RuntimeHelper {

	private static RuntimeHelper mInstance;

	public static RuntimeHelper getInstance() {
		if (mInstance == null) {
			synchronized (RuntimeHelper.class) {
				if (mInstance == null) {
					mInstance = new RuntimeHelper();
				}
			}
		}

		return mInstance;
	}

	/**
	 * 执行DOS命令,exe等
	 */
	public String run(String command) {
		StringBuilder stringBuilder = new StringBuilder("");
		Runtime run = Runtime.getRuntime();
		try {
			Utils.log(command);
			if (System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS")) {
				command = "cmd /c " + command;
			}
			Process process = run.exec(command);
			InputStream reader = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(reader));
			String ss;
			while ((ss = bufferedReader.readLine()) != null) {
				Utils.log(ss);
				stringBuilder.append(ss).append("\n");
			}
			if (process.waitFor() != 0) {
				Utils.log("执行失败: " + process.waitFor());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return stringBuilder.toString();
	}

	/**
	 * 执行DOS命令,exe等
	 */
	public String run(String command, boolean isShowLog) {
		StringBuilder stringBuilder = new StringBuilder("");
		Runtime run = Runtime.getRuntime();
		try {
			Utils.log(command);
			if (System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS")) {
				command = "cmd /c " + command;
			}
			Process process = run.exec(command);
			InputStream reader = process.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(reader));
			String ss;
			while ((ss = bufferedReader.readLine()) != null) {
				if (isShowLog) {
					Utils.log(ss);
				}
				stringBuilder.append(ss).append("\n");
			}
			if (process.waitFor() != 0) {
				Utils.log("执行失败: " + process.waitFor());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return stringBuilder.toString();
	}
}
