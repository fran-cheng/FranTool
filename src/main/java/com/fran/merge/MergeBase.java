package com.fran.merge;

import com.fran.util.Utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 程良明
 * @date 2022/10/10
 * * 说明:合并2个apk
 * 1、将plugin的copy到work。
 * 2、当处于文件合并的时候保留work的（例如:AndroidManiFest.xml, value/下的xml文件）
 * 3、当处于文件替换的时候即由plugin替换work的对应文件
 **/
public abstract class MergeBase {
    private final String[] mCopyWhiteList = new String[]{"assets", "lib", "res", "smali"};
    private final String mWorkPath;
    private final String mPluginPath;
    private static final String KEY = "0x7f";

    public static void main(String[] args) {
        MergeBase mergeBase = new MergeBase("F:\\Work\\2022-10\\hcrmx_", "F:\\AndroidProject\\leaning\\baseApp\\app\\build\\outputs\\apk\\release\\app-release-unsigned") {
            @Override
            protected void processManiFestXml(Document workDocument) {

            }
        };
        mergeBase.merge();
    }

    /**
     * 构造方法
     *
     * @param workPath   apktool解包后的文件路径
     * @param pluginPath apktool解包后的文件路径
     */
    public MergeBase(String workPath, String pluginPath) {
        mWorkPath = workPath;
        mPluginPath = pluginPath;

    }

    /**
     * 合并操作
     */
    public void merge() {
        try {
            mergeManiFestXml(mWorkPath, mPluginPath);
        } catch (DocumentException e) {
            System.err.println("合并AndroidManiXml 出错");
            e.printStackTrace();
        }
        copyPluginSource(mWorkPath, mPluginPath, mCopyWhiteList);
    }

    /**
     * 拷贝插件资源
     */
    public void copyPluginSource(String workPath, String pluginPath, String[] whiteList) {
        Map<String, String> pluginIdMap = null;
        Utils.log("开始处理资源文件");
        File workDir = new File(workPath);
        File pluginDir = new File(pluginPath);
        File[] workSmaliFiles = workDir.listFiles((file, s) -> {
            String fileName = s.toLowerCase();
            return fileName.startsWith("smali");
        });

        if (workSmaliFiles != null && workSmaliFiles.length > 9) {
            workSmaliFiles = Arrays.stream(workSmaliFiles).sorted(Comparator.comparingInt(file -> file.getName().length())).collect(Collectors.toList()).toArray(File[]::new);

        }

        File[] needProcessFiles = pluginDir.listFiles((file, s) -> {
            String fileName = s.toLowerCase();
            for (String whiteName : whiteList) {
                if (fileName.startsWith(whiteName)) {
                    return true;
                }
            }
            return false;
        });

        if (needProcessFiles != null) {
            for (File file : needProcessFiles) {
                String fileName = file.getName();
                Utils.log(String.format("处理%s文件夹", fileName));
                switch (fileName) {
                    case "lib":
                        copyLibs(workDir, file);
                        break;
                    case "res":
                        pluginIdMap = mergeRes(file);
                        break;
                    default:
                        if (fileName.startsWith("smali")) {
                            copySmaliOperation(file, new File(file.getPath().replace(pluginPath, workPath)), pluginIdMap, workSmaliFiles);
                        } else {
                            Utils.logInfo("替换" + fileName);
                            copyOperation(file, new File(file.getPath().replace(pluginPath, workPath)));
                        }
                        break;
                }
            }
        }
    }

    /**
     * 合并Res文件夹
     *
     * @param file File
     */
    private Map<String, String> mergeRes(File file) {

        Map<String, String> pluginIdMap = null;
        //                    value的需要合并，其他直接覆盖
        File[] mergeValueFiles = file.listFiles((file1, s) -> s.startsWith("value"));
        File[] copyFiles = file.listFiles((file1, s) -> !s.startsWith("value"));
        if (copyFiles != null) {
            Utils.log("替换Res文件");
            for (File tempFile : copyFiles) {
                for (File xmlFile : Objects.requireNonNull(tempFile.listFiles())) {
                    copyOperation(xmlFile, new File(xmlFile.getPath().replace(mPluginPath, mWorkPath)));
                }
            }
        }
        if (mergeValueFiles != null) {
            SAXReader saxReader = new SAXReader();
            for (File tempFile : mergeValueFiles) {
                //  合并操作
                for (File xmlFile : Objects.requireNonNull(tempFile.listFiles())) {
                    String fileType = xmlFile.getName().replace(".xml", "");
                    File workXmlFile = new File(xmlFile.getPath().replace(mPluginPath, mWorkPath));
                    if (workXmlFile.exists()) {
                        Utils.logInfo("合并: " + xmlFile.getName());
                        try {
                            if ("public".equals(fileType)) {
                                //  public合并

                                pluginIdMap = mergePublicXml(saxReader, xmlFile, workXmlFile);
                                continue;
                            }
                            mergeValueXml(saxReader, xmlFile, workXmlFile);
                        } catch (DocumentException e) {
                            System.err.printf("合并%s出错%n", xmlFile.getName());
                            e.printStackTrace();
                        }
                    } else {
                        copyOperation(xmlFile, new File(xmlFile.getPath().replace(mPluginPath, mWorkPath)));
                    }

                }
            }
        }

        return pluginIdMap;
    }

    /**
     * 合并Res/value 的xml文件
     *
     * @param saxReader   SAXReader
     * @param xmlFile     File
     * @param workXmlFile File
     */
    private void mergeValueXml(SAXReader saxReader, File xmlFile, File workXmlFile) throws DocumentException {
        Document pluginXml = saxReader.read(xmlFile);
        Document workXml = saxReader.read(workXmlFile);

        Element pluginRootElement = pluginXml.getRootElement();
        Element workRootElement = workXml.getRootElement();
        List<String> workNameList = new ArrayList<>(1024);
        Map<String, Element> workMapElement = new HashMap<>(1024);
        for (Element element : workRootElement.elements()) {
            String name = element.attributeValue("name");
            if (workNameList.contains(name)) {
                continue;
            }
            workNameList.add(name);
            workMapElement.put(name, element);
        }
        List<String> pluginNameList = new ArrayList<>(1024);
        for (Element element : pluginRootElement.elements()) {
            String name = element.attributeValue("name");
            if (pluginNameList.contains(name)) {
                continue;
            }
            pluginNameList.add(name);
            if (workNameList.contains(name)) {
//                                                以插件为准，移除work的
                Element oldElement = workMapElement.get(name);
                workRootElement.remove(oldElement);
            }
        }
//                                        合并
        workRootElement.appendContent(pluginRootElement);
        writeXmlFile(workXmlFile.getPath(), workXml);
    }

    /**
     * 合并public.xml文件
     *
     * @param saxReader   SAXReader
     * @param xmlFile     pluginPublicXmlFile
     * @param workXmlFile workPublicXmlFile
     * @return id值映射
     */
    private Map<String, String> mergePublicXml(SAXReader saxReader, File xmlFile, File workXmlFile) throws DocumentException {
        Map<String, String> oldNewIdMap = new HashMap<>(1024);
        Map<String, Map<String, Integer>> workMapTypeName = new HashMap<>(1024);
        Document pluginXml = saxReader.read(xmlFile);
        Document workXml = saxReader.read(workXmlFile);
        Element pluginRootElement = pluginXml.getRootElement();
        Element workRootElement = workXml.getRootElement();
        Map<String, Integer> typeMaxIdMap = new HashMap<>(16);
        for (Element element : workRootElement.elements()) {
            String type = element.attributeValue("type");
            String name = element.attributeValue("name");
            String idString = element.attributeValue("id");
            int id = Integer.parseInt(idString.substring(2), 16);
            Map<String, Integer> nameIdMap = workMapTypeName.getOrDefault(type, new HashMap<>(1024));
            nameIdMap.put(name, id);
            workMapTypeName.put(type, nameIdMap);
            if (typeMaxIdMap.getOrDefault(type, 0) < id) {
//              更新类型最大值
                typeMaxIdMap.put(type, id);
            }
        }
//            获取最大Id，用来添加新类型
        int allTypeMaxId = 0;
        for (int id : typeMaxIdMap.values()) {
            if (id > allTypeMaxId) {
                allTypeMaxId = id;
            }
        }
//            改变plugin值
        for (Element element : pluginRootElement.elements()) {
            String type = element.attributeValue("type");
            String name = element.attributeValue("name");
            String idStr = element.attributeValue("id");
            if (workMapTypeName.containsKey(type)) {
                if (workMapTypeName.get(type).containsKey(name)) {
                    Utils.logInfo("移除plugin name: " + name + " : " + workMapTypeName.get(type).get(name) + " : " + type);
                    String newId = "0x" + Integer.toHexString(workMapTypeName.get(type).get(name));
                    oldNewIdMap.put(idStr, newId);
                    pluginRootElement.remove(element);
                } else {
                    int maxId = typeMaxIdMap.get(type);
                    int newId = maxId + 1;
                    String value = "0x" + Integer.toHexString(newId);
                    element.attribute("id").setValue(value);
                    typeMaxIdMap.put(type, newId);
                    Utils.logInfo(String.format("修改plugin name: %s ,id: 新值: %s", name, newId));
                    Map<String, Integer> map = workMapTypeName.getOrDefault(type, new HashMap<>(1024));
                    map.put(name, newId);
                    workMapTypeName.put(type, map);
                    oldNewIdMap.put(idStr, value);
                }
            } else {
//                    类型不存在，需要更改所有的id
                int id = typeMaxIdMap.getOrDefault(type, 0);
                id = id == 0 ? ((allTypeMaxId >> 16) + 1) << 16 : id + 1;
                String value = "0x" + Integer.toHexString(id);
                element.attribute("id").setValue(value);
                typeMaxIdMap.put(type, id);
                allTypeMaxId = id;
                Utils.logInfo(String.format("修改plugin name: %s ,id: 新值: %s", name, value));
                Map<String, Integer> map = workMapTypeName.getOrDefault(type, new HashMap<>(1024));
                map.put(name, id);
                workMapTypeName.put(type, map);
                oldNewIdMap.put(idStr, value);
            }
        }
//            合并xml
        workRootElement.appendContent(pluginRootElement);
//             重新排序,可不做
        Utils.log("排序public.xml");
        List<Element> nodes = workRootElement.elements();
        nodes.sort(Comparator.comparing(element -> element.attributeValue("type")));
        for (Element element : nodes) {
            workRootElement.remove(element);
        }
        workRootElement.setContent(new ArrayList<>(nodes));
        writeXmlFile(workXmlFile.getPath(), workXml);
        return oldNewIdMap;
    }

    private void copyLibs(File workDir, File file) {
        String[] workLibFileName = new File(workDir, "lib").list();

        if (workLibFileName == null || workLibFileName.length == 0) {
            for (File tempFile : Objects.requireNonNull(file.listFiles())) {
                copyOperation(tempFile, new File(tempFile.getPath().replace(mPluginPath, mWorkPath)));
            }
        } else {
            List<String> list = Arrays.asList(workLibFileName);
            File[] pluginLibApiFiles = file.listFiles((file1, s) -> list.contains(s));
            if (pluginLibApiFiles != null) {
                for (File tempFile : pluginLibApiFiles) {
                    Utils.log("拷贝plugin的lib");
                    copyOperation(tempFile, new File(tempFile.getPath().replace(mPluginPath, mWorkPath)));
                }
            }
        }
    }

    /**
     * 合并后的文件序列化
     */
    private void writeXmlFile(String outPutPath, Document document) {
        Utils.log("写入: " + outPutPath);
        XMLWriter writer = null;
        try (BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(outPutPath))) {
            writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
            writer.write(document);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 合并AndroidManiFestXml文件
     */
    private void mergeManiFestXml(String workPath, String pluginPath) throws DocumentException {
        String workManifestPath = Utils.linkPath(workPath, "AndroidManifest.xml");
        String pluginManifestPath = Utils.linkPath(pluginPath, "AndroidManifest.xml");
        if (new File(pluginManifestPath).exists()) {
            Utils.log("开始合并清单文件");
            SAXReader saxReader = new SAXReader();
            Document workDocument = saxReader.read(workManifestPath);
            Document pluginDocument = saxReader.read(pluginManifestPath);
            String workPackName = workDocument.getRootElement().attributeValue("package");
            String pluginPackName = pluginDocument.getRootElement().attributeValue("package");
            Element workManifestElement = workDocument.getRootElement();
            Element pluginManifestElement = pluginDocument.getRootElement();
//      处理插件包名
            processPluginPackageName(workPackName, pluginPackName, pluginManifestElement);
            //        合并2个xml文件
            workManifestElement.appendContent(pluginManifestElement);
//        去除Manifest重复
            processElementToSole(workManifestElement, true);
//        扩展处理ManiFestXML文件
            processManiFestXml(workDocument);
//        写文件
            writeXmlFile(Utils.linkPath(workPath, "AndroidManifest.xml"), workDocument);
        }
    }

    /**
     * 用于扩展修改清单文件
     * Document在此方法后，会写入xml文件
     *
     * @param workDocument Document
     */
    protected abstract void processManiFestXml(Document workDocument);

    /**
     * 将插件的packageName替换成work的packageName
     *
     * @param pluginElement Element
     */
    private void processPluginPackageName(String workPackName, String pluginPackName, Element pluginElement) {
        Utils.log("处理插件标签: " + pluginElement.getName());
        for (Element element : pluginElement.elements()) {
            if (element.hasContent()) {
                processPluginPackageName(workPackName, pluginPackName, element);
            } else {
                for (Attribute attribute : element.attributes()) {
                    String value = attribute.getValue();
                    if (value.contains(pluginPackName)) {
                        Utils.logInfo(String.format("插件标签%s,value:%s,将%s,替换成%s", element.getName(), value, pluginPackName, workPackName));
                        attribute.setValue(value.replace(pluginPackName, workPackName));
                    }
                }
            }

        }
    }

    /**
     * 处理合并后的元素，确保唯一值
     *
     * @param workElement      标签元素
     * @param isProcessContent 是否处理标签子元素的内容
     */
    private void processElementToSole(Element workElement, boolean isProcessContent) {
        Utils.log("处理标签: " + workElement.getName());
        List<String> nameList = new ArrayList<>();
        List<String> noneRootElementList = new ArrayList<>();
        for (Element element : workElement.elements()) {
            String name = element.getName();
            String value = element.attributeValue("name");
            if (!element.hasContent() || !isProcessContent) {

                if (value == null) {
                    StringBuilder builder = new StringBuilder();
                    for (Attribute attribute : element.attributes()) {
                        builder.append(attribute.getName()).append(attribute.getValue());
                    }
                    value = builder.toString();
                    Utils.log(String.format("注意: 因为name为null，所以判断%s的attribute", element.getName()));
                }

                value = name + ":" + value;

                if (nameList.contains(value)) {
                    Utils.logInfo("去除重复元素： " + value);
                    workElement.remove(element);
                } else {
                    nameList.add(value);
                }
            } else if (!noneRootElementList.contains(name)) {
                noneRootElementList.add(name);
            }
        }

        if (isProcessContent) {
            //        处理需要合并的标签
            for (String noneName : noneRootElementList) {
                Utils.logInfo(String.format("合并%s,使其唯一", noneName));
                mergeElementFromName(workElement, noneName);
                processElementToSole(workElement.element(noneName), false);
            }
        }

    }

    /**
     * 将Element下的 name 合并成一个
     *
     * @param manifestElement Element
     */
    private void mergeElementFromName(Element manifestElement, String name) {
        Element tempElement = null;
        for (Element element : manifestElement.elements(name)) {
            if (tempElement != null && element != tempElement) {
                tempElement.appendContent(element);
                manifestElement.remove(element);
            }
            tempElement = element;
        }
    }

    /**
     * 拷贝的具体操作
     * 存在的文件重新写入替换
     *
     * @param tempFile   输入
     * @param outPutFile 输出
     */
    private void copyOperation(File tempFile, File outPutFile) {

        if (tempFile.isDirectory()) {
            for (File file : Objects.requireNonNull(tempFile.listFiles())) {
                copyOperation(file, new File(outPutFile, file.getName()));
            }
        } else {
            outPutFile.getParentFile().mkdirs();
            try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(tempFile));
                 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outPutFile))) {
                byte[] chars = new byte[1024];
                int length;
                while ((length = bufferedInputStream.read(chars)) != -1) {
                    bufferedOutputStream.write(chars, 0, length);
                }
                bufferedOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拷贝的具体操作
     * 存在的文件重新写入替换
     * 其实直接替换也可以。。。
     * 替换顺序:由上到下， 先smali的具体文件，到具体文件的父母，存在就替换，都不存在就copy到最后一个smali文件夹
     *
     * @param tempFile   输入
     * @param outPutFile 输出
     */
    private void copySmaliOperation(File tempFile, File outPutFile, Map<String, String> map, File[] workSmaliFile) {
        String path = outPutFile.getPath();
        path = path.substring(path.indexOf("smali"));
        if (tempFile.isDirectory()) {
            for (File file : Objects.requireNonNull(tempFile.listFiles())) {
                copySmaliOperation(file, new File(outPutFile, file.getName()), map, workSmaliFile);
            }
        } else {
            String outPath = path.substring(path.indexOf(File.separator));
            for (int i = 0; i < workSmaliFile.length; i++) {
                File exitFile = new File(workSmaliFile[i], outPath);
                if (exitFile.exists() || exitFile.getParentFile().exists()) {
                    outPutFile = exitFile;
                    break;
                }
                if (i == workSmaliFile.length - 1) {
                    outPutFile = exitFile;
                }
            }
            outPutFile.getParentFile().mkdirs();
            String outPutFilePath = outPutFile.getPath();
            outPutFilePath = outPutFilePath.substring(outPutFilePath.indexOf("smali"));
            Utils.logInfo(String.format("从%s替换%s", path, outPutFilePath));

            try (BufferedReader buffReader = new BufferedReader(new FileReader(tempFile, StandardCharsets.UTF_8));
                 BufferedWriter buffWriter = new BufferedWriter(new FileWriter(outPutFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = buffReader.readLine()) != null) {
                    String resValue = getHexString(line);
                    if (resValue != null) {
                        String targetValue = map.get(resValue);
                        line = amendLine(line, resValue, targetValue);
                    }
                    buffWriter.write(line + "\r\n");
                }
                buffWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getHexString(String line) {
        String resValue = null;
        if (line.contains(KEY)) {
            int startIndex = line.indexOf(KEY);
            try {
                resValue = line.substring(startIndex, startIndex + 10);
            } catch (Exception ignored) {

            }
        }

        return resValue;
    }

    private String amendLine(String line, String sourceString, String targetString) {
        if (sourceString != null && targetString != null) {
            if (!targetString.equals(sourceString)) {
                // TODO: 2022/10/19  const/high16  转
                Utils.logInfo(String.format("用:%s ; 替换: %s ", targetString, sourceString));
                line = line.replace(sourceString, targetString);
//                处理部分只需要搞16位的id更改后 如 const/high16 p1, 0x7f060000
                if (line.contains("const/high16")) {
                    line = line.replace("const/high16", "const");
                }
            }
        }
        return line;
    }
}
