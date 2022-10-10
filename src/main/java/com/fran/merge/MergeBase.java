package com.fran.merge;

import com.fran.util.Utils;

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
    private final String mWorkPath;
    private final String mPluginPath;
    private Document mWorkDocument;
    private Document mPluginDocument;

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
//        合并权限
        mergeElement(workManifestElement, pluginManifestElement, "uses-permission");
//        合并feature
        mergeElement(workManifestElement, pluginManifestElement, "uses-feature");
//        处理queries
        mergeQueriesElement(workManifestElement, pluginManifestElement);
//        处理application
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


//        合并2个xml文件
        workManifestElement.appendContent(pluginManifestElement);
//        合并成一个
        processElementToSole(workManifestElement, "queries");
        // TODO: 2022/10/10 插件需要替换部分name为当前包名，待处理
    }

    /**
     * 合并queries
     *
     * @param workManifestElement   母包manifest元素
     * @param pluginManifestElement 插件manifest元素
     */
    private void mergeQueriesElement(Element workManifestElement, Element pluginManifestElement) {
        processElementToSole(workManifestElement, "queries");
        processElementToSole(pluginManifestElement, "queries");
        mergeElement(workManifestElement.element("queries"), pluginManifestElement.element("queries"), "package");
    }

    /**
     * 将Element下的 name 合并成一个
     *
     * @param manifestElement Element
     */
    private void processElementToSole(Element manifestElement, String name) {
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
