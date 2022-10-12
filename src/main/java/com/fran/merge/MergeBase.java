package com.fran.merge;

import com.fran.util.Utils;
import com.fran.utils.FileUtils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author 程良明
 * @date 2022/10/10
 * * 说明:合并2个apk
 * 1、将plugin的copy到work。
 * 2、当处于文件合并的时候保留work的（例如:AndroidManiFest.xml, value/下的xml文件）
 * 3、当处于文件替换的时候即由plugin替换work的对应文件
 **/
public class MergeBase {
    private String[] mCopyWhiteList = new String[]{"assets", "lib", "res", "smali"};
    protected String mWorkPackName;
    protected String mPluginPackName;
    private final String mWorkPath;
    private final String mPluginPath;
    protected Document mWorkDocument;
    protected Document mPluginDocument;
    private Map<String, Map<String, Integer>> mMergePublicMap;
    /**
     * 插件修改过后的新旧id映射
     */
    private Map<String, String> mPluginIdMap;

    public static void main(String[] args) {
        MergeBase mergeBase = new MergeBase("F:\\Work\\Test\\A", "F:\\Work\\Test\\B");
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
        String workManifestPath = Utils.linkPath(workPath, "AndroidManifest.xml");
        String pluginManifestPath = Utils.linkPath(pluginPath, "AndroidManifest.xml");
        try {
            SAXReader saxReader = new SAXReader();
            mWorkDocument = saxReader.read(workManifestPath);
            mPluginDocument = saxReader.read(pluginManifestPath);
            mWorkPackName = mWorkDocument.getRootElement().attributeValue("package");
            mPluginPackName = mPluginDocument.getRootElement().attributeValue("package");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 合并操作
     */
    public void merge() {
        mergeManiFestXml();
        copyPluginSource();
    }

    /**
     * 拷贝插件资源
     */
    public void copyPluginSource() {
        Utils.log("开始处理资源文件");
        File workDir = new File(mWorkPath);
        File pluginDir = new File(mPluginPath);
        File[] files = pluginDir.listFiles((file, s) -> {
            String fileName = s.toLowerCase();
            for (String whiteName : mCopyWhiteList) {
                if (fileName.startsWith(whiteName)) {
                    return true;
                }
            }
            return false;
        });

        for (File file : files) {
            String fileName = file.getName();
            Utils.log(String.format("处理%s文件夹", fileName));
            switch (fileName) {
                case "lib":
                    copyLibs(workDir, file);
                    break;
                case "res":
                    mergeRes(file);
                    break;
                default:
                    if (fileName.startsWith("smali")) {
                        // TODO: 2022/10/11  处理smali
                        // TODO: 2022/10/12 首先需要收集 合并后的xml文件 上面res有，接着，需要收集smali里面R文件的位置,  然后需要遍历文件，替换资源id
//                        依据 mPublicMap， 来纠正插件的Rid
//                        依据 mPluginIdMap， 来纠正插件的Rid
                        // TODO: 2022/10/12 R文件合并， 非R文件覆盖
//                        throw  new RuntimeException("clm");
                        FileUtils.copySmaliOperation(file, new File(file.getPath().replace(mPluginPath, mWorkPath)), mPluginIdMap);
                    } else {
                        Utils.log("替换" + fileName);
                        FileUtils.copyOperation(file, new File(file.getPath().replace(mPluginPath, mWorkPath)));
                    }
                    break;
            }
        }
    }

    /**
     * 合并Res文件夹
     *
     * @param file File
     */
    private void mergeRes(File file) {
        //                    value的需要合并，其他直接覆盖
        File[] mergeValueFiles = file.listFiles((file1, s) -> s.startsWith("value"));
        File[] copyFiles = file.listFiles((file1, s) -> !s.startsWith("value"));
        if (copyFiles != null) {
            Utils.log("替换Res文件");
            for (File tempFile : copyFiles) {
                for (File xmlFile : Objects.requireNonNull(tempFile.listFiles())) {
                    FileUtils.copyOperation(xmlFile, new File(xmlFile.getPath().replace(mPluginPath, mWorkPath)));
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
                        Utils.log("合并: " + xmlFile.getName());
                        if ("public".equals(fileType)) {
                            //  public合并
                            mMergePublicMap = mergePublicXml(saxReader, xmlFile, workXmlFile);
                            continue;
                        }
                        mergeValueXml(saxReader, xmlFile, workXmlFile);
                    } else {
                        FileUtils.copyOperation(xmlFile, new File(xmlFile.getPath().replace(mPluginPath, mWorkPath)));
                    }

                }
            }
        }
    }

    /**
     * 合并Res/value 的xml文件
     *
     * @param saxReader   SAXReader
     * @param xmlFile     File
     * @param workXmlFile File
     */
    private void mergeValueXml(SAXReader saxReader, File xmlFile, File workXmlFile) {
        try {
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
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 合并public.xml文件
     *
     * @param saxReader   SAXReader
     * @param xmlFile     pluginPublicXmlFile
     * @param workXmlFile workPublicXmlFile
     * @return
     */
    private Map<String, Map<String, Integer>> mergePublicXml(SAXReader saxReader, File xmlFile, File workXmlFile) {
        Map<String, String> oldNewIdMap = new HashMap<>(1024);
        Map<String, Map<String, Integer>> workMapTypeName = new HashMap<>(1024);
        try {
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
//                                      更新类型最大值
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
                        Utils.log("移除plugin name: " + name);
                        String newId = "0x" + Integer.toHexString(workMapTypeName.get(type).get(name));
                        oldNewIdMap.put(idStr, newId);
                        pluginRootElement.remove(element);
                    } else {
                        int maxId = typeMaxIdMap.get(type);
                        int newId = maxId + 1;
                        String value = "0x" + Integer.toHexString(newId);
                        element.attribute("id").setValue(value);
                        typeMaxIdMap.put(type, newId);
                        Utils.log(String.format("修改plugin name: %s ,id: 新值: %s", name, newId));
                        Map<String, Integer> map = workMapTypeName.getOrDefault(type, new HashMap<>(1024));
                        map.put(name, newId);
                        workMapTypeName.put(type, map);
                        oldNewIdMap.put(idStr, value);
                    }
                } else {
//                    类型不存在，需要更改所有的id
                    int id = typeMaxIdMap.getOrDefault(type, 0);
                    if (id == 0) {
                        id = ((allTypeMaxId >> 16) + 1) << 16;
                    } else {
                        id++;
                    }
                    String value = "0x" + Integer.toHexString(id);
                    element.attribute("id").setValue(value);
                    typeMaxIdMap.put(type, id);
                    allTypeMaxId = id;
                    Utils.log(String.format("修改plugin name: %s ,id: 新值: %s", name, value));
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
        } catch (
                DocumentException e) {
            e.printStackTrace();
        }

        mPluginIdMap = oldNewIdMap;

        return workMapTypeName;
    }

    private void copyLibs(File workDir, File file) {
        String[] workLibFileName = new File(workDir, "lib").list();

        if (workLibFileName == null || workLibFileName.length == 0) {
            for (File tempFile : Objects.requireNonNull(file.listFiles())) {
                FileUtils.copyOperation(tempFile, new File(tempFile.getPath().replace(mPluginPath, mWorkPath)));
            }
        } else {
            List<String> list = Arrays.asList(workLibFileName);
            File[] pluginLibApiFiles = file.listFiles((file1, s) -> list.contains(s));
            if (pluginLibApiFiles != null) {
                for (File tempFile : pluginLibApiFiles) {
                    Utils.log("拷贝plugin的lib");
                    FileUtils.copyOperation(tempFile, new File(tempFile.getPath().replace(mPluginPath, mWorkPath)));
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
        try (FileWriter fileWriter = new FileWriter(outPutPath)) {
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
    private void mergeManiFestXml() {
        Utils.log("开始合并清单文件");
        Element workManifestElement = mWorkDocument.getRootElement();
        Element pluginManifestElement = mPluginDocument.getRootElement();
//      处理插件包名
        processPluginPackageName(pluginManifestElement);
        //        合并2个xml文件
        workManifestElement.appendContent(pluginManifestElement);
//        去除Manifest重复
        processElementToSole(workManifestElement, true);
//        写文件
        writeXmlFile(Utils.linkPath(mWorkPath, "AndroidManifest.xml"), mWorkDocument);

    }

    /**
     * 将插件的packageName替换成work的packageName
     *
     * @param pluginElement Element
     */
    private void processPluginPackageName(Element pluginElement) {
        Utils.log("处理插件标签: " + pluginElement.getName());
        for (Element element : pluginElement.elements()) {
            if (element.hasContent()) {
                processPluginPackageName(element);
            } else {
                for (Attribute attribute : element.attributes()) {
                    String value = attribute.getValue();
                    if (value.contains(mPluginPackName)) {
                        Utils.log(String.format("插件标签%s,value:%s,将%s,替换成%s", element.getName(), value, mPluginPackName, mWorkPackName));
                        attribute.setValue(value.replace(mPluginPackName, mWorkPackName));
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
                    Utils.log("去除重复元素： " + value);
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
                Utils.log(String.format("合并%s,使其唯一", noneName));
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
}
