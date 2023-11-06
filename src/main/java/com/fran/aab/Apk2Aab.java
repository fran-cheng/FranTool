package com.fran.aab;

import com.fran.tool.KeyTool;
import com.fran.util.RuntimeHelper;
import com.fran.util.Utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import brut.androlib.apk.ApkInfo;
import brut.androlib.exceptions.AndrolibException;
import brut.common.BrutException;
import brut.util.AaptManager;

/**
 * @author 程良明
 * @date 2023/9/8
 * * * 说明:从Apktool解包路径下生成aab
 * 参考https://juejin.cn/post/6982111395621896229
 **/

public class Apk2Aab {
    private final String mApkDecodePath;
    private final String mWorkPath;
    private String mRootPath;

    /**
     * 分pad，  assets的Reg（正则）
     */
    private String[] mPadAssetsRegs;

    /**
     * 分pad， 资源包的名字 一般都是跟mPadAssetsRegs的长度一致
     */
    private String[] mPadFileNames;

    public static void main(String[] args) throws IOException {
        Apk2Aab aab = new Apk2Aab("E:\\work\\xh\\2023-11\\aab\\10005-230925164927659848747");
        aab.process();
//		aab.installAab("D:\\FranGitHub\\FranTool\\runtime\\app-debug");

        // TODO: 2023/11/2 aab分pad

    }


    /**
     * 构造方法
     *
     * @param apkDecodePath apktool解包后的文件路径
     */
    public Apk2Aab(String apkDecodePath) {
        String classPath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (classPath.endsWith(".jar")) {
            File jarFile = new File(classPath);
            mRootPath = jarFile.getParent();
        } else {
            mRootPath = "D:\\FranGitHub\\FranTool\\tool";
        }
        mApkDecodePath = apkDecodePath;
        mWorkPath = Utils.linkPath(apkDecodePath, "fran_aab_work");
        File workFile = new File(mWorkPath);
        if (workFile.exists()) {
            Utils.delDir(workFile);
        }
        workFile.mkdirs();
    }

    /**
     * 安装aab
     */
    public void installAab(String aabFilePath) {
        String bundleToolPath = Utils.linkPath(mRootPath, "aab-tool", "bundletool.jar");
        File aabFile = new File(aabFilePath);
        String apksPath = Utils.linkPath(aabFile.getParent(), aabFile.getName().replace(".aab", "") + ".apks");
        if (!new File(apksPath).exists()) {
            String[] key = KeyTool.findSignInfo(aabFile.getParentFile(), false);
            String cmdApks;
            if (key == null) {
                cmdApks = String.format("java -jar %s build-apks --bundle=%s --output=%s", bundleToolPath, aabFilePath, apksPath);
            } else {
                cmdApks = String.format("java -jar %s build-apks --bundle=%s --output=%s --ks=%s --ks-key-alias=%s --ks-pass=pass:%s --key-pass=pass:%s", bundleToolPath, aabFilePath, apksPath, key[0], key[2], key[1], key[1]);
            }


//		生成apks
            RuntimeHelper.getInstance().run(cmdApks);
        }


        String adbCmd = "adb devices";

        String devices = RuntimeHelper.getInstance().run(adbCmd);
        String[] deviceList = devices.split("\n");
        if (deviceList.length < 2) {
            Utils.log("请先链接设备");
        } else {
            //			默认第一个
            String deviceName = deviceList[1].replace("device", "").trim();
            if (deviceList.length > 2) {
                Utils.log("请先选择要安装的设备id或者名称");
                String str = new Scanner(System.in).next();
                try {
                    int index = Integer.parseInt(str);
                    deviceName = deviceList[index].replace("device", "").trim();
                } catch (Exception ignored) {
                    if (devices.contains(str)) {
                        deviceName = str;
                    } else {
                        Utils.log("输入有误，选择默认的设备 : " + deviceName);
                    }
                }
            }
            Utils.log("正在安装的设备是 : " + deviceName);

            //		安装apk
            String cmdInstallApks = String.format("java -jar %s install-apks --apks=%s --device-id=%s", bundleToolPath, apksPath, deviceName);

            RuntimeHelper.getInstance().run(cmdInstallApks);
        }


    }

    /**
     * 处理apk2aab
     */
    public void process() {
        //        生成pad的  跟目录名字
        mPadFileNames = new String[]{"init_pack"};
//        assets下的  文件名
        mPadAssetsRegs = new String[]{"ab"};
        // TODO: 2023/11/2 划分资源包
        String compileFilePath = compile();

        List<File> padZipList = new ArrayList<>();

//        padZipList.add(generatePad(compileFilePath, "init_pack", "ab"));


//        生成zip的路径（先生成apk，获取AndroidManifest.xml.拷贝assets的。生成zip）
        File padFile = generatePad(compileFilePath, "init_pack", "ab");

        padZipList.add(padFile);

        String baseApkPath = linkSources(compileFilePath, "base.apk", Utils.linkPath(mApkDecodePath, "AndroidManifest.xml"));
        String basePath = unZipBase(baseApkPath, "base");

        copySources(basePath, mApkDecodePath, mPadAssetsRegs);
        generateAAB(basePath, padZipList);
    }


    /**
     * 构建pad（Play Asset Delivery），除了这个还有个功能模块（<a href="https://developer.android.com/guide/playcore/feature-delivery?hl=zh-cn#feature-module-manifest">...</a>）
     * <a href="https://developer.android.com/guide/app-bundle/asset-delivery?hl=zh-cn#next-step-instructions">...</a>
     */
    private File generatePad(String compileFilePath, String padName, String reg) {
        String packageName = "com.superwarrior.android";

        //       生成 manifest下的AndroidManifest.xml
        String xmlPath = Utils.linkPath(mWorkPath, padName + "_temp", "AndroidManifest.xml");

//        从assets copy 到tempFile的assets
        generatePadManifest(packageName, padName, xmlPath);

        String padApkPath = linkSources(compileFilePath, padName + ".apk", xmlPath);

        String padPath = unZipBase(padApkPath, padName);

        File assetsFile = new File(mApkDecodePath, "assets");
        // 创建正则表达式模式对象
        Pattern pattern = Pattern.compile(reg);
        File[] canCopy = assetsFile.listFiles(file -> file.isDirectory() && pattern.matcher(file.getName()).find());

        if (canCopy != null) {
            for (File file : canCopy) {
                Utils.copyFiles(file, new File(Utils.linkPath(padPath, "assets"), file.getName()));
            }
        }


//        压缩成pad.zip

        File padZipFile = new File(Utils.linkPath(mWorkPath, padName + ".zip"));
        try {
            FileOutputStream fos = new FileOutputStream(padZipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fos, StandardCharsets.UTF_8);
            File[] fileToZip = new File(padPath).listFiles();

            assert fileToZip != null;
            for (File file : fileToZip) {
                if (!"resources.pb".equals(file.getName())) {
                    zipFile(file, file.getName(), zipOut);
                }
            }


            zipOut.close();
            fos.close();

            System.out.println("文件压缩成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
//         构建asset.pb文件   说明模块协议缓冲区 (*.pb) 文件：这些文件提供了一些元数据，有助于向各个应用商店（如 Google Play）说明每个应用模块的内容。例如，BundleConfig.pb 提供了有关 bundle 本身的信息（如用于构建 app bundle 的构建工具版本），native.pb 和 resources.pb 说明了每个模块中的代码和资源，这在 Google Play 针对不同的设备配置优化 APK 时非常有用。
        return padZipFile;
    }

    /**
     * 所有 install-time Asset Pack 的总下载大小上限为 1 GB,默认使用install-time
     */
    private void generatePadManifest(String packageName, String split, String xmlPath) {
        Document document = DocumentHelper.createDocument();
        Element manifest = document.addElement("manifest");
        manifest.addNamespace("android", "http://schemas.android.com/apk/res/android");
        // 添加"dist"命名空间前缀的命名空间声明
        manifest.addNamespace("dist", "http://schemas.android.com/apk/distribution");
        manifest.addAttribute("package", packageName);
        manifest.addAttribute("split", split);
        Element module = manifest.addElement(new QName("module", manifest.getNamespaceForPrefix("dist")));
        module.addAttribute(new QName("type", manifest.getNamespaceForPrefix("dist")), "asset-pack");

        Element delivery = module.addElement(new QName("delivery", manifest.getNamespaceForPrefix("dist")));
        delivery.addElement(new QName("install-time", manifest.getNamespaceForPrefix("dist")));

        Element fusing = module.addElement(new QName("fusing", manifest.getNamespaceForPrefix("dist")));
        fusing.addAttribute(new QName("include", manifest.getNamespaceForPrefix("dist")), "true");


        new File(xmlPath).getParentFile().mkdirs();
        writeXmlFile(xmlPath, document);
    }

    private void writeXmlFile(String outPutPath, Document document) {
        Utils.log("写入: " + outPutPath);
        XMLWriter writer = null;
        try (BufferedOutputStream fileWriter = new BufferedOutputStream(Files.newOutputStream(Paths.get(outPutPath)))) {
            writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
            writer.write(document);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateAAB(String baseUnZipPath, List<File> padList) {
        String bundleToolPath = Utils.linkPath(mRootPath, "aab-tool", "bundletool.jar");
        String outPutAabPath = Utils.linkPath(mWorkPath, "base.aab");
        File baseZipFile = new File(Utils.linkPath(mWorkPath, "base.zip"));
        try {
            FileOutputStream fos = new FileOutputStream(baseZipFile);
            ZipOutputStream zipOut = new ZipOutputStream(fos, StandardCharsets.UTF_8);
            File[] fileToZip = new File(baseUnZipPath).listFiles();

            assert fileToZip != null;
            for (File file : fileToZip) {
                zipFile(file, file.getName(), zipOut);
            }


            zipOut.close();
            fos.close();

            System.out.println("文件压缩成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String modules = baseZipFile.getPath();
        if (padList.size() > 0) {
            for (File file : padList) {
                modules = modules + "," + file.getPath();
            }
        }


        String cmd = String.format("java -jar %s build-bundle --modules=%s --output=%s", bundleToolPath, modules, outPutAabPath);
        RuntimeHelper.getInstance().run(cmd);

        String dir = mApkDecodePath;
        String[] info = KeyTool.findSignInfo(new File(dir));
        String name = new File(dir).getName();
        String outSignAab = Utils.linkPath(dir, name + "_sign.aab");

        String keystorefile = info[0];
        String password = info[1];


        String s = String.format("jarsigner -keystore %s -storepass %s -sigalg MD5withRSA -digestalg SHA1 -signedjar %s %s %s", keystorefile, password, outSignAab, outPutAabPath, info[2]);

        RuntimeHelper.getInstance().run(s);

//		Utils.delDir(new File(mWorkPath));
    }


    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws
                    IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }

        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[4096];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        fis.close();
    }

    private String getAapt2Path() {
        try {
            File aapt = AaptManager.getAapt2();
            return aapt.getPath();
        } catch (BrutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 编译资源
     */
    private String compile() {
        String compileFIlePath = Utils.linkPath(mWorkPath, "compiled_resources.zip");
        String aaptPath = getAapt2Path();
        String cmdCompile = String.format("%s compile --legacy --dir %s -o %s", aaptPath, Utils.linkPath(mApkDecodePath, "res"), compileFIlePath);
        try {
            RuntimeHelper.getInstance().run(cmdCompile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return compileFIlePath;
    }

    /**
     * 关联资源
     */
    private String linkSources(String compileFilePath, String outName, String androidXmlPath) {
        String outBaseApk = Utils.linkPath(mWorkPath, outName);
        String aaptPath = getAapt2Path();
        String androidJarPath = Utils.linkPath(mRootPath, "aab-tool", "android.jar");

        String cmdLink;
        if (outName.contains("base.apk")) {
            String minVersion = "21";
            String targetVersion = "33";
            String versionCode = "1";
            String versionName = "1.0";

            File apkdecodeFile = new File(mApkDecodePath);
            try {
                ApkInfo apkInfo = ApkInfo.load(apkdecodeFile);
                minVersion = apkInfo.getMinSdkVersion();
                targetVersion = apkInfo.getTargetSdkVersion();
                versionCode = apkInfo.versionInfo.versionCode;
                versionName = apkInfo.versionInfo.versionName;
            } catch (AndrolibException e) {
                throw new RuntimeException(e);
            }
            cmdLink = String.format("%s link --proto-format -o %s -I %s --min-sdk-version %s --target-sdk-version %s --version-code %s --version-name %s --manifest %s -R %s --auto-add-overlay",
                            aaptPath, outBaseApk, androidJarPath, minVersion, targetVersion, versionCode, versionName, androidXmlPath, compileFilePath);

        } else {
            cmdLink = String.format("%s link --proto-format -o %s -I %s --manifest %s --auto-add-overlay",
                            aaptPath, outBaseApk, androidJarPath, androidXmlPath);
        }
        try {
            RuntimeHelper.getInstance().run(cmdLink);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outBaseApk;
    }


    /**
     * 解压base.apk
     * 通过unzip解压到base文件夹，目录结构：
     */
    private String unZipBase(String apkFilePath, String outputName) {

        String destDirectory = Utils.linkPath(mWorkPath, outputName);
        byte[] buffer = new byte[4096];
        try {
            // 创建解压缩输入流
            FileInputStream fis = new FileInputStream(apkFilePath);
            ZipInputStream zis = new ZipInputStream(fis, StandardCharsets.UTF_8);
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                File newFile;
                if (fileName.endsWith("AndroidManifest.xml")) {
                    newFile = new File(Utils.linkPath(destDirectory, "manifest", fileName));
                } else {
                    newFile = new File(destDirectory + File.separator + fileName);
                }


                // 创建目录
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();
            System.out.println("解压缩完成");
        } catch (IOException e) {
            e.printStackTrace();
        }


        return destDirectory;
    }

    /**
     * 拷贝资源
     */
    private void copySources(String apkDecodeBasePath, String apkDecodePath, String[] notCopyAssetsFileRegList) {

        File assetsFile = new File(Utils.linkPath(apkDecodePath, "assets"));
        if (assetsFile.exists()) {
//          先判断是否符合  notCopyAssetsFileRegList
            StringBuilder regs = new StringBuilder();
            for (String reg : notCopyAssetsFileRegList) {
                if (regs.length() > 0) {
                    regs.append("|");
                }
                regs.append(reg);
            }
            // 创建正则表达式模式对象
            Pattern pattern = Pattern.compile(regs.toString());
            File[] canCopy = assetsFile.listFiles(file -> !file.isDirectory() || !pattern.matcher(file.getName()).find());

            if (canCopy != null) {
                for (File file : canCopy) {
                    Utils.copyFiles(file, new File(Utils.linkPath(apkDecodeBasePath, "assets"), file.getName()));
                }
            }
        }

        File libFile = new File(Utils.linkPath(apkDecodePath, "lib"));
        if (libFile.exists()) {
            Utils.copyFiles(libFile, new File(Utils.linkPath(apkDecodeBasePath, "lib")));
        }

        File unknownFile = new File(Utils.linkPath(apkDecodePath, "unknown"));
        if (unknownFile.exists()) {
            Utils.copyFiles(unknownFile, new File(Utils.linkPath(apkDecodeBasePath, "root")));
        }


        File kotlinFile = new File(Utils.linkPath(apkDecodePath, "kotlin"));
        if (kotlinFile.exists()) {
            Utils.copyFiles(kotlinFile, new File(Utils.linkPath(apkDecodeBasePath, "root", "kotlin")));
        }


        File buildFile = new File(Utils.linkPath(apkDecodePath, "build", "apk"));
        if (!buildFile.exists()) {
            String cmd = String.format("apktool b %s", apkDecodePath);

            try {
                RuntimeHelper.getInstance().run(cmd);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File[] dexFiles = buildFile.listFiles((file1, s) -> s.startsWith("classes") && s.endsWith(".dex"));
        assert dexFiles != null;
        for (File dexFile : dexFiles) {
            Utils.copyFiles(dexFile, new File(Utils.linkPath(apkDecodeBasePath, "dex", dexFile.getName())));
        }
    }
}
