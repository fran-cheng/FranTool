package com.fran.tool;


import com.fran.merge.MergeBase;
import com.fran.util.RuntimeHelper;
import com.fran.util.Sign;
import com.fran.util.Utils;

import org.dom4j.Document;

import java.io.*;
import java.util.Scanner;

/**
 * @author 程良明
 * * 说明:主入口
 **/
public class ApkBuild {


    public static void main(String[] args) {
        ApkBuild apkBuild = new ApkBuild();
        if (args.length != 2) {
            throw new RuntimeException("参数不对!");
        }
//
        String key = args[0];

        String path = args[1];
        if (key.equalsIgnoreCase("apkDecompress")) {
            apkBuild.apkDecompress(path);

        } else if (key.equalsIgnoreCase("apkBuild")) {
            apkBuild.deleteBuild(path);
            apkBuild.apkToolBuild(path);
            apkBuild.sign(path);
        } else if (key.equalsIgnoreCase("key")) {
            new KeyTool(path);
        } else if (key.equalsIgnoreCase("apkMerge")) {
            Utils.log("输入需要合并的文件路径: ");
            String pluginPath = new Scanner(System.in).next();
            new MergeBase(path, pluginPath) {
                @Override
                protected void processManiFestXml(Document workDocument) {

                }
            }.merge();
        }

        Utils.log("Done!");
    }


    /**
     * 调用解包
     *
     * @param apkFilePath 待解包APK
     */
    private void apkDecompress(String apkFilePath) {
        String cmd = String.format("apktool d -f --only-main-classes %s ", apkFilePath);
        try {
            RuntimeHelper.getInstance().run(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 调用回编
     *
     * @param apkDirPath 待回编APK
     */
    private void apkToolBuild(String apkDirPath) {
        String cmd = String.format("apktool b %s", apkDirPath);

        try {
            RuntimeHelper.getInstance().run(cmd);
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
        String outApk = zipalign(dir);
        new Sign(dir, outApk);
    }

    /**
     * 对齐
     *
     * @param dir 待对齐APK
     */
    private String zipalign(String dir) {
        String name = new File(dir).getName();
        File distFile = new File(Utils.linkPath(dir, "dist"));

        File[] files = distFile.listFiles();
        assert files != null;
        String buildApk = files[0].getPath();

        String outApk = Utils.linkPath(dir, name + "_temp.apk");

        String cmd = String.format("zipalign -f 4 %s %s", buildApk, outApk);
        RuntimeHelper.getInstance().run(cmd);

        return outApk;
    }

    /**
     * 删除中间的构建文件
     *
     * @param dir 工作路径
     */
    private void deleteBuild(String dir) {
        File build = new File(Utils.linkPath(dir, "build"));
        File dist = new File(Utils.linkPath(dir, "dist"));
        if (build.exists()) {
            deleteDir(build);
        }
        if (dist.exists()) {
            deleteDir(dist);
        }
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
                for (File f : fileList) {
                    deleteDir(f);
                }
            }
        }
        file.delete();
    }

}
