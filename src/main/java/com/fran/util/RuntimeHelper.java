package com.fran.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class RuntimeHelper {

    public static boolean sIsWindow;
    private static RuntimeHelper mInstance;

    public static RuntimeHelper getInstance() {
        if (mInstance == null)
            synchronized (RuntimeHelper.class) {
                if (mInstance == null)
                    mInstance = new RuntimeHelper();
            }

        return mInstance;
    }

    //    执行DOS命令,exe等
    public void run(String s) {
        String command = s.replace("\"", "");
        sIsWindow = System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1;
        Runtime run = Runtime.getRuntime();
        try {
            if (sIsWindow)
                command = "cmd /c " + s;
            Process process = run.exec(command);
            Utils.log(s);
            InputStream reader = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(reader));
            String ss;
            while ((ss = bufferedReader.readLine()) != null) {
                Utils.log(ss);
            }
            if (process.waitFor() == 0) {
//                    Utils.log("执行成功");
            } else {
                Utils.log("执行失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
