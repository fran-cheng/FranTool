package com.fran.proguard;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApkProguard {
    private File mDir;
    private File mSmaliDir;
    private File mResDir;
    private File mAndroidManifest;
    private ArrayList<String[]> mSmaliReplacements;
    private ArrayList<String[]> mResReplacements;

    public ApkProguard(String dir) {
        mDir = new File(dir);
        mSmaliDir = new File(dir, "smali/");
        mResDir = new File(dir, "res/");
        mAndroidManifest = new File(dir, "AndroidManifest.xml");
        mSmaliReplacements = new ArrayList<>();
        mResReplacements = new ArrayList<>();
    }

    public static void main(String[] args) throws IOException {
            dating();
    }


    private static void dating() throws IOException {
        ApkProguard c = new ApkProguard("C:\\Data\\Work\\APK\\GP\\Dating\\Dating_match\\dating_match\\");
        c.addSmaliReplacement("api.datinginmyregion.top", "api.datinginmyregion.top");
        c.changePackageTo("datingapp.cooldating.datingmyregion", "datingapp.datingmatch.quickmatch");
        c.changePackageTo("datingapp.datingcod.datinginregion", "datingapp.easymatch.easydating");
        c.proguardSmali("datingapp");
        c.proguardImages();
//        c.changeIcon("C:\\Data\\Work\\APK\\GP\\Dating\\Dating_match\\icon\\icon.png", "C:\\Data\\Work\\APK\\GP\\Dating\\Dating_match\\icon\\icon.png", "C:\\Data\\Work\\APK\\GP\\Dating\\Dating_match\\icon\\icon_round.png");
    }

    private List<File> listFiles(File parent, String ext) {
        if (parent == null)
            parent = mSmaliDir;
        ArrayList<File> ls = new ArrayList<>();
        Util.listAllFiles(ls, parent, ext);
        return ls;
    }

    private Map<String, String> getManifestInfo(String pattern) {
        String content = Util.read(mAndroidManifest, "utf-8");
        return Util.evalScope(content, pattern);
    }

    public void addSmaliReplacement(String sourceString, String targetString) {
        mSmaliReplacements.add(new String[]{sourceString, targetString});
    }

    public void addResReplacement(String sourceString, String targetString) {
        mResReplacements.add(new String[]{sourceString, targetString});
    }

    private void doReplacement(File file, List<String[]> replacements) {
        String c = Util.read(file, "utf-8");
        boolean modified = false;
        for (String[] ss : replacements) {
            if (c.contains(ss[0])) {
                c = c.replace(ss[0], ss[1]);
                if (!modified)
                    modified = true;
            }
        }
        if (modified) {
            Util.writeFile(file.getAbsolutePath(), c, "utf-8");
            System.out.println("Write " + file);
        }
    }

    public void applyReplacement() {
        if (mSmaliReplacements.size() > 0) {
            List<File> smaliFiles = listFiles(mSmaliDir, "smali");
            for (File f : smaliFiles) {
                doReplacement(f, mSmaliReplacements);
            }
            mSmaliReplacements.clear();
        }
        if (mResReplacements.size() > 0) {
            List<File> resFiles = listFiles(mResDir, "xml");
            resFiles.add(mAndroidManifest);
            for (File f : resFiles) {
                doReplacement(f, mResReplacements);
            }
            mResReplacements.clear();
        }
    }

    public void proguardImages() {
        List<File> pngFiles = listFiles(mResDir, "png");
        for (File f : pngFiles) {
            String name = f.getName();
            if (name.startsWith("abc_") || name.startsWith("common_") || name.startsWith("design_") || name.startsWith("google"))
                continue;
            if (name.endsWith(".9.png")) {
                System.out.println("Ignore nine-patch file " + f);
                continue;
            }
            try {
                ImageUtil.randomImage(f, f, "png", 80);
                System.out.println("Rand " + f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void changePackageTo(String srcPackage, String targetPackage) throws IOException {
        String[] ss1 = srcPackage.split("\\.");
        String[] ss2 = targetPackage.split("\\.");
        if (ss1.length != ss2.length)
            throw new RuntimeException("Դ����Ŀ�����ȱ������.");
        addSmaliReplacement("L" + srcPackage.replace('.', '/'),
                "L" + targetPackage.replace('.', '/'));
        addResReplacement(srcPackage, targetPackage);
        File f = new File(mSmaliDir, srcPackage.replace('.', '/'));
        for (int i = ss1.length - 1; i >= 0; i--) {
            File parent = f.getParentFile();
            File newName = new File(parent, ss2[i]);
            if (f.exists() && f.isDirectory() && !f.equals(newName)) {
                System.out.println(f + " -> " + newName);
                if (!f.renameTo(newName)) {
                    if (newName.exists() && newName.isDirectory()) {
                        File[] fs = f.listFiles();
                        for (File a : fs) {
                            if (!Util.move(a, newName))
                                throw new IOException("������ʧ�ܣ�" + f + " -> " + newName);
                        }
                        Util.deleteAll(f);
                        continue;
                    }
                    throw new IOException("������ʧ�ܣ�" + f + " -> " + newName);
                }
            }
            f = parent;
        }
        applyReplacement();
    }

    private void scaleIconTo(String dirName, String iconName, String target) throws IOException {
        File icon = new File(mResDir, dirName + "/" + iconName + ".png");
        if (icon.exists()) {
            if (Util.isEmpty(target))
                throw new RuntimeException("ͼ���Ҳ������ͼ�ꡣ" + icon);
            BufferedImage bimg = ImageIO.read(icon);
            int width = bimg.getWidth();
            int height = bimg.getHeight();
            ImageUtil.scale(target, icon.getAbsolutePath(), height, width, false);
        }
    }

    private void scaleDirIcon(String dirName, String iconName, String targetIconPath, String targetIconForegroundPath,
                              String roundIconName, String targetRoundIconPath) throws IOException {
        File dir = new File(mResDir, dirName);
        if (dir.exists()) {
            scaleIconTo(dirName, iconName, targetIconPath);
            scaleIconTo(dirName, iconName + "_foreground", targetIconForegroundPath);
            if (!Util.isEmpty(roundIconName))
                scaleIconTo(dirName, roundIconName, targetRoundIconPath);
        }
    }

    public void changeIcon(String ic_launcher, String ic_launcher_foreground, String ic_launcher_round) throws IOException {
        Map<String, String> ics = getManifestInfo("<application[*]android:icon=\"@$[icon]\"[*]android:roundIcon=\"@$[roundIcon]\"");
        String iconName = ics.get("icon");
        String[] ss = iconName.split("/");
        iconName = ss[ss.length - 1];
        String roundIconName = ics.get("roundIcon");
        if (!Util.isEmpty(roundIconName)) {
            ss = roundIconName.split("/");
            roundIconName = ss[ss.length - 1];
        }
        scaleDirIcon("mipmap-xxxhdpi", iconName, ic_launcher, ic_launcher_foreground, roundIconName, ic_launcher_round);
        scaleDirIcon("mipmap-xxhdpi", iconName, ic_launcher, ic_launcher_foreground, roundIconName, ic_launcher_round);
        scaleDirIcon("mipmap-xhdpi", iconName, ic_launcher, ic_launcher_foreground, roundIconName, ic_launcher_round);
        scaleDirIcon("mipmap-mdpi", iconName, ic_launcher, ic_launcher_foreground, roundIconName, ic_launcher_round);
        scaleDirIcon("mipmap-hdpi", iconName, ic_launcher, ic_launcher_foreground, roundIconName, ic_launcher_round);
    }

    public void proguardSmali(String packageName) {
        List<File> smaliFiles = listFiles(packageName == null ? mSmaliDir : new File(mSmaliDir, packageName.replace('.', '/')), "smali");
        for (File f : smaliFiles) {
            String src = Util.read(f, "utf-8");
            SmaliParser sl = new SmaliParser(src, new SmaliProguardCallback());
            String ct = null;
            System.out.println("Proguard "+f);
            ct = sl.parse();
            Util.writeFile(f.getAbsolutePath(), ct, "utf-8");
        }
    }
}

