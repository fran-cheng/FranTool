package com.fran.aab;

import com.fran.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.nio.file.Files;

/**
 * @author 程良明
 * @date 2024/7/9
 * * * 说明:
 **/
public class FranBundleConfig {
	private static final String BUNDLE_CONFIG_TEMPLATE = "bundleConfigTemplate.json";

	public static JsonObject load(Reader reader) {
		return new Gson().fromJson(reader, JsonObject.class);
	}

	public static JsonObject load(File path, String mRootPath) {

		JsonObject var2 = null;
		if (!BUNDLE_CONFIG_TEMPLATE.equals(path.getName())) {
			path = new File(path, BUNDLE_CONFIG_TEMPLATE);
			if (!path.exists()) {
				System.out.println("读取默认配置 bundleConfigTemplate!!! ");
//			  拿默认配置copy过去
				String pathTemplate = Utils.linkPath(mRootPath, "property", "bundleConfigTemplate.json");
				path = new File(pathTemplate);
			}
		}
		try (BufferedReader in = Files.newBufferedReader(path.toPath())) {
			var2 = load(in);
		} catch (Exception ignored) {
			System.out.println("clm bundleConfigTemplate error: " + ignored.getMessage());
		}
		return var2;
	}
}
