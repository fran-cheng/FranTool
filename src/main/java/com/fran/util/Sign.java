package com.fran.util;

import java.io.File;
import java.net.MalformedURLException;

public class Sign {

    public Sign(String dir) {
        String[] info = findSignInfo(new File(dir), "key.keystore");
        if (info == null)
            throw new RuntimeException(dir + " 无签名文件！");
        String name = new File(dir).getName();
        String apk = dir + "\\" + name + "_temp.apk";
        String out = dir + "\\" + name + "_sign.apk";

        String keystorefile = info[0];
        String password = info[1];
        String apksignerPath = getApkSigner();

        String s = String.format("java -jar %s sign --ks %s --ks-pass pass:%s --in %s --out %s", apksignerPath, keystorefile, password, apk, out);
        Utils.log("Sign", s);
        RuntimeHelper.getInstance().run(s);

        new File(apk).delete();
    }

    private String[] findSignInfo(File dir, String fn) {
        File f = new File(dir.getAbsolutePath() + "\\" + fn);
        while (!f.exists()) {
            dir = dir.getParentFile();
            if (dir == null) {
                f = null;
                break;
            }
            f = new File(dir.getAbsolutePath() + "\\" + fn);
        }
        if (f != null) {
            File passf = new File(dir, "password.ini");
            if (!passf.exists() || passf.isDirectory())
                throw new RuntimeException("password.ini NOT FOUND!");
            String[] passinfo = Utils.read(passf).split(";");
            // keystore file path, password
            return new String[]{f.getAbsolutePath(), passinfo[0], passinfo[1]};
        }
        return null;
    }

    private String getApkSigner() {
        File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        String jarFile = file.getParent() + File.separator + "apksigner.jar";

        return jarFile;
    }
}
