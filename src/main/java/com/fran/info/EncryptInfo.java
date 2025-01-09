package com.fran.info;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 程良明
 * @date 2025/1/9
 * * * 说明:
 **/
public class EncryptInfo {
	private static final String FRAN_ENCRYPT = "encrypt.yml";
	/**
	 * 保留在壳的smali文件的路径
	 */
	private List<String> path = new ArrayList<>();

	public List<String> getPath() {
		return path;
	}

	public void setPath(List<String> path) {
		this.path = path;
	}

	public EncryptInfo() {
	}

	private static Yaml getYaml() {
		return new Yaml();
	}


	public static EncryptInfo load(InputStream is) {
		return (EncryptInfo) getYaml().loadAs(is, EncryptInfo.class);
	}

	public static EncryptInfo load(File franPadFile) {

		EncryptInfo var2 = null;
		if (!FRAN_ENCRYPT.equals(franPadFile.getName())) {
			franPadFile = new File(franPadFile, FRAN_ENCRYPT);
		}
		try (InputStream in = Files.newInputStream(franPadFile.toPath())) {
			var2 = load(in);
		} catch (Exception ignored) {
			System.out.println("clm encrypt error: " + ignored.getMessage());
		}
		return var2;
	}
}
