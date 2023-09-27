package com.fran.tool;

import com.fran.util.RuntimeHelper;
import com.fran.util.Utils;

import java.io.File;


/**
 * @author 程良明
 * * 说明:生成签名文件
 **/
public class KeyTool {
	private static final String KET_FILE_NAME = "key.keystore";

	KeyTool(String dir) {
		String alias = "key";
		if (new File(dir, KET_FILE_NAME).exists()) {
			Utils.log("key.keystore文件已存在");
			return;
		}

		String keystoreFilePath = new File(dir, "key.keystore").getAbsolutePath();
//   生成随机密码 保留8位
		String password = Utils.sha1(System.currentTimeMillis() + Utils.random(0, 10000) + "", 8);
//    生成密钥保存文件
		Utils.writeFile(new File(dir, "password.ini"), password + ";" + alias, "utf-8");

		String cmd = String.format("keytool -genkey -noprompt -alias key -keyalg RSA -dname CN=DL,OU=DL,O=DL,L=City,S=Province,C=CN -keystore %s -storepass  %s -keypass %s -validity 1000000 -keysize 2048 -deststoretype pkcs12", keystoreFilePath, password, password);


		RuntimeHelper.getInstance().run(cmd);
	}

	public static String[] findSignInfo(File dir) {
		return findSignInfo(dir, true);
	}

	public static String[] findSignInfo(File dir, boolean isForce) {
		File f = new File(dir.getAbsolutePath(), KET_FILE_NAME);
		while (!f.exists()) {
			dir = dir.getParentFile();
			if (dir == null) {
				f = null;
				break;
			}
			f = new File(dir.getAbsolutePath(), KET_FILE_NAME);
		}
		if (f != null) {
			File passf = new File(dir, "password.ini");
			if (!passf.exists() || passf.isDirectory()) {
				throw new RuntimeException("password.ini NOT FOUND!");
			}
			String[] passinfo = Utils.read(passf).split(";");
			// keystore file path, password
			return new String[]{f.getAbsolutePath(), passinfo[0], passinfo[1]};
		}
		if (isForce) {
			new KeyTool(dir.getPath());

			return findSignInfo(dir);
		} else {
			return null;
		}
	}


}
