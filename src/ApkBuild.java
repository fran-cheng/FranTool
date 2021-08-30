import util.RuntimeHelper;
import util.Sign;
import util.Utils;

import java.io.*;

import static java.lang.Integer.valueOf;


public class ApkBuild {

    private void apkToolBuild(String apkDir) {
        RuntimeHelper r = RuntimeHelper.getInstance();
        String s = "apktool b  \"" + apkDir.replace("\\", "/") + "\"";

        try {
            r.run(s);
//             Utils.print("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ApkBuild apkBuild = new ApkBuild();
        if (args.length != 1)
            throw new RuntimeException("参数不对!");
//
        String arg1 = args[0];

        String arg2 = args[1];
        if (arg1.equalsIgnoreCase("apkDecompress")) {
            apkBuild.apkDecompress(arg2);

        } else if (arg1.equalsIgnoreCase("apkBuild")) {
            apkBuild.deleteBuild(arg2);
            apkBuild.apkToolBuild(arg2);
            apkBuild.sign(arg2);
        } else if (arg1.equalsIgnoreCase("key")) {
            new KeyTool(arg2);
        }

        Utils.log("Done!");
    }


    /**
     * 调用解包
     *
     * @param apkFile 待解包APK
     */
    private void apkDecompress(String apkFile) {
        RuntimeHelper r = RuntimeHelper.getInstance();
        String s = "apktool d -f  \"" + apkFile.replace("\\", "/") + "\"";
        try {
            r.run(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 签名
     *
     * @param dir 待签名APK
     */
    private void sign(String dir) {
        new Sign(dir);
        zipalign(dir);
    }

    /**
     * 对齐
     *
     * @param dir 待对齐APK
     */
    private void zipalign(String dir) {
        String name = new File(dir).getName();
        String apk = dir + "\\" + name + "_temp.apk";
        String out = dir + "\\" + name + "_signed" + ".apk";
        String s = "zipalign -f 4 " + apk + " " + out;
        RuntimeHelper.getInstance().run(s);
        new File(apk).delete();
    }

    /**
     * 删除中间的构建文件
     *
     * @param dir 工作路径
     */
    private void deleteBuild(String dir) {
        File build = new File(dir + "\\build");
        File dist = new File(dir + "\\dist");
        if (build.exists())
            deleteDir(build);
        if (dist.exists())
            deleteDir(dist);

    }

    /**
     * 删除文件，文件夹
     *
     * @param file 删除路径
     */
    private void deleteDir(File file) {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File f : fileList)
                    deleteDir(f);
            }
        }
        file.delete();
    }

}
