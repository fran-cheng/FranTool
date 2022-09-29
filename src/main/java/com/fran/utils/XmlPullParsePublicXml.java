package com.fran.utils;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 程良明
 * @date 2022/3/31
 * * 说明:解析public.xml 文件
 **/
public class XmlPullParsePublicXml {
    /**
     * 解析public.xml
     * 以这个  Map<String, Map<String, String>> 为基准
     *
     * @param publicFile public.xml文件
     * @return Map<String, Map < String, String>>  ==>  Map<type, Map<name, id>>
     * @throws XmlPullParserException,IOException
     */
    public static Map<String, Map<String, String>> parsePublicXml(File publicFile) throws XmlPullParserException, IOException {
        Map<String, Map<String, String>> typeNameMap = new HashMap<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xmlPullParser = factory.newPullParser();
        xmlPullParser.setInput(new FileInputStream(publicFile), "UTF-8");
        int eventType = xmlPullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tagName = xmlPullParser.getName();
                if (tagName.equals("public")) {
                    String type = xmlPullParser.getAttributeValue(0);
                    String name = xmlPullParser.getAttributeValue(1);
                    String id = xmlPullParser.getAttributeValue(2);
                    if (!typeNameMap.containsKey(type)) {
                        Map<String, String> nameIdMap = new HashMap<>();
                        typeNameMap.put(type, nameIdMap);
                    }
                    typeNameMap.get(type).put(name, id);
                }
            }
            eventType = xmlPullParser.next();
        }
        return typeNameMap;
    }
}
