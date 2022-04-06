package com.fran.util;


import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Utils {
    /**
     * 日志
     *
     * @param s String
     */
    public static void log(String s) {
        System.out.println(s);
    }

    /**
     * 日志
     *
     * @param tag String
     * @param s   String
     */
    public static void log(String tag, String s) {
        System.out.println(tag + " :  " + s);
    }

    /**
     * SHA-1
     *
     * @param strSrc String
     * @param len    String
     * @return String
     */
    public static String sha1(String strSrc, int len) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();

        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(bt);
            strDes = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException var6) {
            return null;
        }

        return len == 0 ? strDes : strDes.substring(0, len);
    }

    /**
     * bytes2Hex
     *
     * @param bts byte
     * @return String
     */
    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;

        for (int i = 0; i < bts.length; ++i) {
            tmp = Integer.toHexString(bts[i] & 255);
            if (tmp.length() == 1) {
                des = des + "0";
            }

            des = des + tmp;
        }

        return des;
    }

    /**
     * 生成随机数
     *
     * @param min min
     * @param max max
     * @return int
     */
    public static int random(int min, int max) {
        return min + (int) Math.round(Math.random() * (double) (max - min));
    }

    /**
     * 写文件
     *
     * @param file    File
     * @param content String
     * @param charset String
     * @return boolean
     */
    public static boolean writeFile(File file, String content, String charset) {
        OutputStream os = null;

        try {
            if (!file.exists()) {
                makeIfDir(file);
                file.createNewFile();
            }

            os = new FileOutputStream(file);
            os.write(content.getBytes(charset));
            os.close();
            return true;
        } catch (Exception var6) {
            var6.printStackTrace();
            return false;
        }
    }

    /**
     * 生成dir
     *
     * @param f File
     */
    public static void makeIfDir(File f) {
        if (f.isDirectory()) {
            if (!f.exists()) {
                f.mkdirs();
            }
        } else {
            File dir = f.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

    }

    /**
     * 读文件
     *
     * @param f File
     * @return String
     */
    public static String read(File f) {
        String c = null;

        try {
            InputStream fis = new FileInputStream(f);
            c = inputStreamToString(fis);
            fis.close();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return c;
    }

    /**
     * inputStreamToString
     *
     * @param is InputStream
     * @return String
     */
    public static String inputStreamToString(InputStream is) {

        Scanner scanner = new Scanner(is);
        StringBuffer stringBuffer = new StringBuffer();
        while (scanner.hasNext())
            stringBuffer.append(scanner.next());


        return stringBuffer.toString();
    }

    /**
     * 拼接地址
     *
     * @param basePath 基础路径
     * @return path
     */


    public static String linkPath(String basePath, String... dentrys) {
        StringBuilder stringBuilder = new StringBuilder(basePath);
        for (String dentry : dentrys) {
            stringBuilder.append(File.separator);
            stringBuilder.append(dentry);
        }
        return stringBuilder.toString();
    }

}
