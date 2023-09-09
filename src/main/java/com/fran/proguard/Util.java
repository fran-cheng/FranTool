package com.fran.proguard;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import java.io.*;
import java.util.*;

public class Util {


    public static int random(int min, int max) {
        return min + (int)Math.round(Math.random() * (max - min));
    }




    public static byte[] read(File f){
        InputStream fis = null;
        byte[] c = null;
        try{
            fis = new FileInputStream(f);
            c = inputStreamToByte(fis);
            fis.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return c;
    }

    public static byte[] inputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final int bufsize = 8196;
        final byte[] cbuf = new byte[bufsize];
        for (int readBytes = is.read(cbuf, 0, bufsize); readBytes != -1; readBytes = is.read(cbuf, 0, bufsize)) {
            bos.write(cbuf, 0, readBytes);
        }
        byte[] bs = bos.toByteArray();
        bos.close();
        return bs;
    }

    public static boolean writeFile(String file, String content, String charset){
        OutputStream os = null;
        File f = new File(file);
        try{
            if(!f.exists()){
                Util.makeIfDir(f);
                f.createNewFile();
            }

            os = new FileOutputStream(file);
            os.write(content.getBytes(charset));
            os.close();
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static void makeIfDir(String dirPath) {
        File f = new File(dirPath);
        makeIfDir(f);
    }

    public static void makeIfDir(File f){
        if (f.isDirectory()) {
            if (!f.exists())
                f.mkdirs();
        } else {
            File dir = f.getParentFile();
            if (!dir.exists())
                dir.mkdirs();
        }
    }

    public static boolean deleteAll(File f){
        boolean r = true;
        if(f.exists()){
            if(f.isDirectory()){
                File[] fs = f.listFiles();
                if(fs!=null){
                    for(int i=0;i<fs.length;i++){
                        if(!deleteAll(fs[i]) && r)
                            r = false;
                    }
                }
            }
            if(!f.delete() && r)
                r = false;
        }
        return r;
    }

    public static void listAllFiles(List<File> ls, File dir, String extention){
        extention = extention.toLowerCase();
        File[] fs = dir.listFiles();
        for(int i=0;i<fs.length;i++){
            if(fs[i].isDirectory()){
                listAllFiles(ls, fs[i], extention);
            }
            else {
                if(extention != null){
                    if(extention.contains(Util.getExt(fs[i].getName()).toLowerCase())){
                        ls.add(fs[i]);
                    }
                }
                else ls.add(fs[i]);
            }
        }
    }

    public static String getExt(String fileName, String def) {
        int qidx = fileName.lastIndexOf('?');
        if(qidx>0){
            fileName = fileName.substring(0, qidx);
        }
        int lst = fileName.lastIndexOf(".");
        return (lst>0)?fileName.substring(lst + 1):def;
    }

    public static String getExt(String fileName) {
        return getExt(fileName, "");
    }

    /**
     * ��ȡ�ı���һ������ָ��λ�ÿ�ͷ��ָ��λ�ý����ڵ��ı����ݡ�
     * ��ȡ��ʽ��
     * $[key]
     * [*] ��ʾ�����ı���
     * ���磺 [*]start$[parameter]end[*]
     * @param src �ı�
     * @param pattern
     * @return
     */
    public static List<Map<String, String>> evalScopeAll(String src, String pattern){
        return evalScope0(src, pattern, true);
    }
    /**
     * ��ȡ�ı���һ������ָ��λ�ÿ�ͷ��ָ��λ�ý����ڵ��ı����ݡ�
     * ��ȡ��ʽ��
     * $[key]
     * [*] ��ʾ�����ı���
     * ���磺 [*]start$[parameter]end[*]
     * @param src �ı�
     * @param pattern
     * @return
     */
    public static Map<String, String> evalScope(String src, String pattern){
        List<Map<String, String>> ls = evalScope0(src, pattern, false);
        return ls.isEmpty() ? new HashMap<>() : ls.get(0);
    }


    private static List<Map<String, String>> evalScope0(String src, String pattern, boolean all){
        int len = src.length();
        int patternLen = pattern.length();
        int idx=0;
        String head;
        String tail;
        int strfrom;
        int strtoOfAny;
        int strtoOfOpen;
        int k = 0;
        int srcidx = 0;
        int prevclosebg = 0;
        String key;
        String value;
        final String S = "$[";
        final String E = "]";
        final String ANY = "[*]";
        ArrayList<Map<String, String>> ls = null;
        Map<String, String> sc = new HashMap<>();
        while(srcidx < len){
            if(idx>=patternLen){
                if(!all)
                    break;
                idx = 0;
                prevclosebg = 0;
                if(sc.size() > 0){
                    if(ls == null)
                        ls = new ArrayList<>();
                    ls.add(sc);
                    sc = new HashMap<>();
                }
            }
            //   ${xxx}|[*][head]<-${}
            idx = pattern.indexOf(S, idx);
            if(idx <= -1)
                break;
            strfrom = pattern.lastIndexOf(ANY, idx);
            if(strfrom<prevclosebg){
                strfrom = prevclosebg;
            }else strfrom += ANY.length();
            if(strfrom == idx)
                head = null;
            else head = pattern.substring(strfrom, idx);

            k = pattern.indexOf(E, idx);
            key = pattern.substring(idx+S.length(), k);
            // }idx
            idx = k+E.length();
            prevclosebg = idx;
            // if }$
            if(idx == patternLen){
                if(head !=null){
                    srcidx = src.indexOf(head, srcidx);
                    if(srcidx==-1)
                        break;
                    srcidx += head.length();
                }
                if(srcidx<len){
                    value = src.substring(srcidx);
                    sc.put(key, value);
                    //System.out.println("���Map������"+key+" = "+value);
                }
                break;
            }else{
                strtoOfAny = pattern.indexOf(ANY, idx);
                strtoOfOpen = pattern.indexOf(S, idx);

                if(strtoOfOpen == strtoOfAny){
                    tail = pattern.substring(idx, patternLen);
                    idx = patternLen;
                }

                else if(strtoOfAny<strtoOfOpen){
                    tail = pattern.substring(idx, strtoOfAny);
                    idx = strtoOfAny + ANY.length();
                }
                else if(strtoOfOpen <= -1)
                    break;
                else {
                    tail = pattern.substring(idx, strtoOfOpen);
                    idx = strtoOfOpen;
                }


                if(head !=null) {
                    srcidx = src.indexOf(head, srcidx);
                    if(srcidx == -1)
                        break;
                    srcidx = srcidx + head.length();
                }
                k = src.indexOf(tail, srcidx);
                if(k == -1)
                    break;
                value = src.substring(srcidx, k);
                srcidx = k;
                sc.put(key, value);
                //System.out.println("���Map������"+key+" = "+value);
            }
        }
        if(ls == null){
            if(sc.size() == 0)
                return Collections.emptyList();
            ls = new ArrayList<>(1);
        }
        if(sc.size() > 0)
            ls.add(sc);
        return ls;
    }

    /**
     * ���ļ���Ŀ¼�µ��ļ����Ƶ�Ŀ��Ŀ¼��. ���Ŀ���ļ��Ѵ��ڣ����ǡ�
     * @param f �ļ���Ŀ¼
     * @param toParent Ŀ��Ŀ¼
     */
    public static void copyIntoParent(File f, File toParent) throws FileNotFoundException, IOException {
        if(!toParent.exists()){
            Util.makeIfDir(toParent);
        }else if(!toParent.isDirectory()){
            throw new IOException(toParent+" ����Ŀ¼!");
        }

        if(!f.exists())
            throw new IOException(f + "�����ڣ�");
        if(f.isFile())
            copyFileToDirectory(f, toParent);
        else {
            File newDir = new File(toParent, f.getName()+"/");
            newDir.mkdirs();
            copyDirFilesToDirectory(f, newDir);
        }
    }

    public static void filetofile(File src, File target) throws IOException {
        if(target.exists() && target.isFile())
            target.delete();

        FileInputStream fis = null;
        FileOutputStream fos = null;
        try{
            System.out.println("���� "+src + " -> "+target);
            fis = new FileInputStream(src);
            target.createNewFile();
            fos = new FileOutputStream(target);
            inputStreamToOutputStream(fis, fos);
        }finally{
            if(fis != null) try { fis.close();}catch(Exception ex){};
            if(fos != null) try { fos.close();}catch(Exception ex){};
        }
    }

    /**
     * ɾ��Ŀ¼���ļ����������ļ������Ƿ����ļ���
     * @param f
     */
    public static boolean delete(File f){
        System.out.println("ɾ�� "+f);
        return Util.deleteAll(f);
    }

    /**
     * �ƶ�Ŀ¼���ļ���Ŀ���ļ�����
     */
    public static boolean move(File f, File toParent) throws FileNotFoundException, IOException {
        copyIntoParent(f, toParent);
        return delete(f);
    }

    private static void copyFileToDirectory(File f, File toParent) throws FileNotFoundException, IOException {
        File target = new File(toParent, f.getName());
        filetofile(f, target);
    }

    private static void copyDirFilesToDirectory(File f, File toParent) throws FileNotFoundException, IOException {
        File[] children = f.listFiles();
        if(children != null){
            for(int i=0;i<children.length;i++){
                File t = children[i];
                if(t.isDirectory()){
                    File targetDir = new File(toParent, t.getName());
                    if(!targetDir.exists())
                        targetDir.mkdir();
                    copyDirFilesToDirectory(t, targetDir);
                }
                else copyFileToDirectory(t, toParent);
            }
        }
    }

    public static boolean isEmpty(Object s) {
        if (s == null)
            return true;
        return "".equalsIgnoreCase(s.toString());
    }

    private static long sSeed = System.currentTimeMillis();

    public static long nextSeed(){
        return (++sSeed);
    }

    public static void main(String[] args) {
    }

    public static boolean writeFile(String file, byte[] bs){
        OutputStream os = null;
        File f = new File(file);
        try{
            if(!f.exists()){
                Util.makeIfDir(f);
                f.createNewFile();
            }
            os = new FileOutputStream(file);
            os.write(bs);
            os.close();
        }catch(Exception ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    public static long inputStreamToOutputStream(InputStream is, OutputStream os) throws IOException {
        long c = 0;
        final int bufsize = 8196 * 10;
        final byte[] cbuf = new byte[bufsize];

        for (int readBytes = is.read(cbuf, 0, bufsize); readBytes != -1; readBytes = is.read(cbuf, 0, bufsize)) {
            c += readBytes;
            os.write(cbuf, 0, readBytes);
        }
        return c;
    }

    public static String read(File f, String charset) {
        InputStream fis;
        String c = null;
        try{
            fis = new FileInputStream(f);
            c = inputStreamToString(fis, charset);
            fis.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return c;
    }

    public static String inputStreamToString(InputStream is, String charset) throws IOException {
        if(charset.toLowerCase().startsWith("utf-")){
            //�ɼ��������ͣ����޳�bom
            BOMInputStream bomIn = new BOMInputStream(is, false, ByteOrderMark.UTF_8, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE);
            //����⵽bom����ʹ��bom��Ӧ�ı���
            if(bomIn.hasBOM())
                charset = bomIn.getBOMCharsetName();
            is = bomIn;
        }
        final int bufsize = 8196 * 10;
        final byte[] cbuf = new byte[bufsize];
        StringBuilder sb = new StringBuilder();
        for (int readBytes = is.read(cbuf, 0, bufsize); readBytes != -1; readBytes = is.read(cbuf, 0, bufsize)) {
            sb.append(new String(cbuf, 0, readBytes, charset));
        }
        return sb.toString();
    }

}
