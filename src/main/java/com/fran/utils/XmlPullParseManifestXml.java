package com.fran.utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.wrapper.XmlPullParserWrapper;
import org.xmlpull.v1.wrapper.XmlPullWrapperFactory;
import org.xmlpull.v1.wrapper.XmlSerializerWrapper;
import org.xmlpull.v1.wrapper.classic.StaticXmlSerializerWrapper;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 程良明
 * @date 2022/5/16
 * * 说明: 解析AndroidManifest.xml文件
 **/
public class XmlPullParseManifestXml {
    private String packageName;

    public static void main(String[] args) {
        XmlPullParseManifestXml xmlPullParseManifestXml = new XmlPullParseManifestXml();
        try {
            xmlPullParseManifestXml.parseAndroidManifest("F:\\Work\\Test\\AndroidManifestA.xml", "F:\\Work\\Test\\AndroidManifestB.xml", "F:\\Work\\Test\\AndroidManifestBC.xml");
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    // TODO: 2022/9/29 将方法修改成合并2个xml

    /**
     * 解析AndroidManifes.xml 尽力转换成arr的形式
     *
     * @param aXml 源A文件
     * @param bXml 源B文件目标
     * @throws XmlPullParserException
     * @throws IOException
     */
    public String parseAndroidManifest(String aXml, String bXml, String outXml) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        XmlPullParser aXmlPullParser = factory.newPullParser();
        aXmlPullParser.setInput(new FileInputStream(aXml), "UTF-8");

        XmlPullParser bXmlPullParser = factory.newPullParser();
        bXmlPullParser.setInput(new FileInputStream(bXml), "UTF-8");

        XmlSerializer xmlSerializer = factory.newSerializer();
        xmlSerializer.setOutput(new FileOutputStream(outXml), "UTF-8");

        XmlPullWrapperFactory xmlPullWrapperFactory = XmlPullWrapperFactory.newInstance();

        XmlPullParserWrapper aXmlPullParserWrapper = xmlPullWrapperFactory.newPullParserWrapper(aXmlPullParser);
        XmlPullParserWrapper bXmlPullParserWrapper = xmlPullWrapperFactory.newPullParserWrapper(bXmlPullParser);

        XmlSerializerWrapper xmlSerializerWrapper = new StaticXmlSerializerWrapper(xmlSerializer, xmlPullWrapperFactory) {
            boolean notWrite = false;
            boolean isB = false;
            List<String> aList = new ArrayList<>();
            String bPackageName;

            /**
             * 处理写入
             * @param pp XmlPullParser
             * @throws XmlPullParserException
             * @throws IOException
             */
            @Override
            public void event(XmlPullParser pp) throws XmlPullParserException, IOException {
                int type = pp.getEventType();
                if (type == XmlPullParser.START_TAG) {
                    String name = pp.getName().toLowerCase();
                    switch (name) {
                        case "manifest":
                            if (packageName == null) {
                                packageName = pp.getAttributeValue(null, "package");
                            } else {
                                bPackageName = pp.getAttributeValue(null, "package");
                                isB = true;
                            }
                            break;
                        case "application":
                            // TODO: 2022/9/29 到这之前， 处理Bxml对应的，当Bxml到这的时候，切换会Axml
                            break;
                        default:
                            if (isB) {
                                if (aList.contains(pp.getText())) {
                                    notWrite = true;
                                    return;
                                }
                                if (pp.getText().contains(bPackageName)) {
                                    writeStartTagReplaceAttributeValue(pp, packageName, bPackageName);
                                    return;
                                }

                            } else {
                                aList.add(pp.getText());
                            }
                            break;
                    }
                }
                super.event(pp);
            }

            //            替换包名
            private void writeStartTagReplaceAttributeValue(XmlPullParser pp, String oldPack, String newPack) throws XmlPullParserException, IOException {
                if (!pp.getFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES)) {
                    int nsStart = pp.getNamespaceCount(pp.getDepth() - 1);
                    int nsEnd = pp.getNamespaceCount(pp.getDepth());
                    for (int i = nsStart; i < nsEnd; i++) {
                        String prefix = pp.getNamespacePrefix(i);
                        String ns = pp.getNamespaceUri(i);
                        setPrefix(prefix, ns);
                    }
                }
                startTag(pp.getNamespace(), pp.getName());

                for (int i = 0; i < pp.getAttributeCount(); i++) {
                    String attributeNamespace = pp.getAttributeNamespace(i);
                    String attributeName = pp.getAttributeName(i);
                    String attributeValue = pp.getAttributeValue(i);
                    if (attributeName.contains("authorities")) {
                        attributeValue = attributeValue.replace(oldPack, newPack);
                    }
                    attribute
                            (attributeNamespace,
                                    attributeName,
                                    attributeValue);
                }
            }

        };


        // TODO: 2022/9/29 要实现  case“application”  需要在这里面进行处理， event处理移动出来
//        先写入A的XML
        do {
            xmlSerializerWrapper.event(aXmlPullParser);
        }
        while (aXmlPullParserWrapper.nextToken() != XmlPullParser.END_DOCUMENT);

//        合并B的XML
        do {
            xmlSerializerWrapper.event(bXmlPullParser);
        } while (bXmlPullParserWrapper.nextToken() != XmlPullParser.END_DOCUMENT);


        xmlSerializerWrapper.flush();


        return packageName;
    }


}
