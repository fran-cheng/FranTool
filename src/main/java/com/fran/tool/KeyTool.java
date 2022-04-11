package com.fran.tool;

import com.fran.util.RuntimeHelper;
import com.fran.util.Utils;

import java.io.File;


/**
 * 生成签名文件
 */
class KeyTool {
    KeyTool(String dir) {
        String alias = "key";
        if (new File(dir, "key.keystore").exists()) {
            Utils.log("key.keystore文件已存在");
            return;
        }

        String keystoreFilePath = new File(dir, "key.keystore").getAbsolutePath();
//   生成随机密码 保留8位
        String password = Utils.sha1(System.currentTimeMillis() + Utils.random(0, 10000) + "", 8);
//    生成密钥保存文件
        Utils.writeFile(new File(dir, "password.ini"), password + ";" + alias, "utf-8");

        String cmd = String.format("keytool -genkey -noprompt -alias key -keyalg RSA -dname \"CN=DL, OU=DL, O=DL, L=City, S=Province, C=CN\" -keystore \"%s\" -storepass  %s -keypass %s -validity 1000000 -keysize 2048 -deststoretype pkcs12", keystoreFilePath, password, password);


        RuntimeHelper.getInstance().run(cmd);
    }


}
