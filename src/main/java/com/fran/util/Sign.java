package com.fran.util;

import com.fran.tool.KeyTool;

import java.io.File;

/**
 * @author 程良明
 * * 说明:签名
 **/
public class Sign {

	public Sign(String dir, String inApk) {
		String[] info = findSignInfo(new File(dir), "key.keystore");
		if (info == null) {
//            throw new RuntimeException(dir + " 无签名文件！");
			Utils.log("Sign", "生成签名文件");
			new KeyTool(dir);
			info = findSignInfo(new File(dir), "key.keystore");
		}
		String name = new File(dir).getName();
		String out = Utils.linkPath(dir, name + "_sign.apk");

		String keystorefile = info[0];
		String password = info[1];
		String apksignerPath = getApkSigner();

		String s = String.format("java -jar %s sign --ks %s --ks-pass pass:%s --in %s --out %s", apksignerPath, keystorefile, password, inApk, out);
		Utils.log("Sign", s);
		RuntimeHelper.getInstance().run(s);

		new File(inApk).delete();
	}

	private String[] findSignInfo(File dir, String fn) {
		File f = new File(dir.getAbsolutePath(), fn);
		while (!f.exists()) {
			dir = dir.getParentFile();
			if (dir == null) {
				f = null;
				break;
			}
			f = new File(dir.getAbsolutePath(), fn);
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
		return null;
	}

	private String getApkSigner() {
		File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

		return Utils.linkPath(file.getParent(), "apksigner.jar");
	}
}
