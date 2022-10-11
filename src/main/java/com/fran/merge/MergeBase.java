package com.fran.merge;

import com.fran.util.Utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 程良明
 * @date 2022/10/10
 * * 说明:合并2个apk
 **/
public class MergeBase {
    protected String mWorkPackName;
    protected String mPluginPackName;
    private final String mWorkPath;
    private final String mPluginPath;
    protected Document mWorkDocument;
    protected Document mPluginDocument;

    public static void main(String[] args) {
        MergeBase mergeBase = new MergeBase("F:\\Work\\Test\\A", "F:\\Work\\Test\\B");
        mergeBase.merge();
    }

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

    public void merge() {
        commonMergeManiFestXml();
        writeManifestXml();
    }

    public void writeManifestXml() {
        XMLWriter writer = null;
        try (FileWriter fileWriter = new FileWriter("F:\\Work\\Test\\AndroidManifestBC.xml");) {
            writer = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
            writer.write(mWorkDocument);
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
     * 共同的合并AndroidManiFestXml文件
     */
    private void commonMergeManiFestXml() {
        Element workManifestElement = mWorkDocument.getRootElement();
        Element pluginManifestElement = mPluginDocument.getRootElement();
//      处理插件包名
        processPluginPackageName(pluginManifestElement);
        //        合并2个xml文件
        workManifestElement.appendContent(pluginManifestElement);
//        去除Manifest重复
        processElementToSole(workManifestElement, true);

    }

    /**
     * 将插件的packageName替换成work的packageName
     *
     * @param pluginManifestElement Element
     */
    private void processPluginPackageName(Element pluginManifestElement) {
        for (Element element : pluginManifestElement.elements()) {
            if (element.hasContent()) {
                processPluginPackageName(element);
            } else {
                for (Attribute attribute : element.attributes()) {
                    String value = attribute.getValue();
                    if (value.contains(mPluginPackName)) {
                        Utils.log(String.format("标签%s,value:%s,将%s,替换成%s", element.getName(), value, mPluginPackName, mWorkPackName));
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
                    Utils.log("注意: 因为name为null，所以判断attribute ==>" + element.getName());
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

    private void processApplication(Element workManifestElement, Element pluginManifestElement) {
        Element workApplicationElement = workManifestElement.element("application");
        Element pluginApplicationElement = pluginManifestElement.element("application");

        Map<String, List<String>> map = new HashMap<>();
        for (Element element : workApplicationElement.elements()) {
            String name = element.getName();
            String attributeName = element.attributeValue("name");
            List<String> list = map.get(name);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(attributeName);
            map.put(name, list);
        }

        for (Element element : pluginApplicationElement.elements()) {
            String name = element.getName();
            String attributeName = element.attributeValue("name");
            List<String> list = map.get(name);
            if (list == null) {
                list = new ArrayList<>();
            }
            if (list.contains(attributeName)) {
                Utils.log("移除插件application 元素： " + element);
                pluginApplicationElement.remove(element);
            }
        }
    }

    /**
     * 合并queries
     *
     * @param workManifestElement   母包manifest元素
     * @param pluginManifestElement 插件manifest元素
     */
    private void mergeQueriesElement(Element workManifestElement, Element pluginManifestElement) {
        mergeElementFromName(workManifestElement, "queries");
        mergeElementFromName(pluginManifestElement, "queries");
        mergeElement(workManifestElement.element("queries"), pluginManifestElement.element("queries"), "package");
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
     * 合并AndroidManifest Application 元素
     *
     * @param workRootElement   母包Root元素
     * @param pluginRootElement 插件Root元素
     */
    private void mergeManiXmlApplicationTag(Element workRootElement, Element pluginRootElement) {
        List<String> applicationList = new ArrayList<>();
        List<Element> workApplicationEle = workRootElement.elements("application");
        List<Element> pluginApplicationEle = pluginRootElement.elements("application");
        for (Element element : workApplicationEle) {
            String value = element.attributeValue("name");
            if (applicationList.contains(value)) {
                Utils.log("去除重复Element value： " + value);
                workRootElement.remove(element);
            } else {
                applicationList.add(value);
            }
        }

        for (Element element : pluginApplicationEle) {

        }
    }

    /**
     * 合并AndroidManifest 元素
     *
     * @param workElement   母包元素
     * @param pluginElement 插件元素
     * @param name          元素标签名
     */
    private void mergeElement(Element workElement, Element pluginElement, String name) {

        List<Element> list = workElement.elements(name);
        List<String> nameList = new ArrayList<>();
        for (Element element : list) {
            String value = element.attributeValue("name");
            if (nameList.contains(value)) {
                Utils.log("去除母包重复Element： " + element);
                workElement.remove(element);
            } else {
                nameList.add(value);
            }
        }
        List<Element> pluginList = pluginElement.elements(name);
        for (Element element : pluginList) {
            String value = element.attributeValue("name");
            if (nameList.contains(value)) {
                Utils.log("去除插件重复Element： " + element);
                pluginElement.remove(element);
            } else {
                nameList.add(value);
            }
        }
    }
}
