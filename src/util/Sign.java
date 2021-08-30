package util;

import java.io.File;
import java.net.MalformedURLException;

public class Sign {

    public Sign(String dir){
        String[] info = findSignInfo(new File(dir), "key.keystore");
        if(info == null)
            throw new RuntimeException(dir+" 无签名文件！");
        String name = new File(dir).getName();
        String apk = dir+"\\dist\\"+name+".apk";
        String out = dir+"\\"+name+"_temp.apk";

        String keystorefile = info[0];
        String password = info[1];

        String s = null;
        try {
            s = getJavaHome()+"\\bin\\jarsigner -keystore \"" + new File(keystorefile).toURI().toURL() +
                    "\" -storepass "+password+" -sigalg MD5withRSA -digestalg SHA1 -signedjar \"" + out + "\" \"" + apk +
                    "\" " + info[2];
            Utils.log("Sign",s);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        RuntimeHelper.getInstance().run(s);
    }

    private  String[] findSignInfo(File dir, String fn) {
        File f = new File(dir.getAbsolutePath() + "\\"+fn);
        while (!f.exists()) {
            dir = dir.getParentFile();
            if (dir == null) {
                f = null;
                break;
            }
            f = new File(dir.getAbsolutePath() + "\\" + fn);
        }
        if(f != null){
            File passf = new File(dir, "password.ini");
            if(!passf.exists() || passf.isDirectory())
                throw new RuntimeException("password.ini NOT FOUND!");
            String[] passinfo = Utils.read(passf).split(";");
            // keystore file path, password
            return new String[]{f.getAbsolutePath(), passinfo[0], passinfo[1]};
        }
        return null;
    }
    private String getJavaHome(){
        String home = System.getenv("JAVA_HOME");
        if (home == null)
            throw new RuntimeException("JAVA_HOME 未定义!");

        return home;
    }
}
