package com.ft.sdk.garble.utils;

import com.ft.sdk.FTApplication;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * author: huangDianHua
 * time: 2020/7/21 14:16:22
 * description: 解析别名配置文件
 */
public class DescXmlParse {
    //配置文件名
    public static final String XML_FILE_NAME = "ft_page_vtp_desc";

    //读取别名配置文件
    public static Map<String, String>[] readXmlBySAX() {
        String rClass = DeviceUtils.getApplicationId(FTApplication.getApplication()) + ".R$raw";
        Field[] fields = null;
        try {
            fields = Class.forName(rClass).getDeclaredFields();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (fields == null) return null;
        int rawId = 0;
        for (Field field : fields) {
            String name = field.getName();
            if (name.equals(XML_FILE_NAME)) {
                try {
                    rawId = field.getInt(android.R.raw.class);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        if (rawId != 0) {
            return readXmlBySAX(FTApplication.getApplication().getResources().openRawResource(rawId));
        }
        return null;
    }

    private static Map<String, String>[] readXmlBySAX(InputStream inputStream) {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser saxParser = spf.newSAXParser();
            XMLContentHandler handler = new XMLContentHandler();
            saxParser.parse(inputStream, handler);
            inputStream.close();
            Map<String, String>[] maps = new HashMap[2];
            maps[0] = handler.pageMap;
            maps[1] = handler.vtpMap;
            return maps;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class XMLContentHandler extends DefaultHandler {
        public final String PAGE_ROOT = "pagedesc";
        public final String VTP_ROOT = "vtpdesc";
        public final String PAGE_ELEMENT = "page";
        public final String VTP_ELEMENT = "vtp";
        public final String NAME = "name";
        public final String PATH = "path";
        public final String DESC = "desc";

        Map<String, String> pageMap = null;
        Map<String, String> vtpMap = null;

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            pageMap = new HashMap<>();
            vtpMap = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (localName.equals(PAGE_ELEMENT)) {
                String name = attributes.getValue(NAME);
                String desc = attributes.getValue(DESC);
                pageMap.put(name, desc);
            } else if (localName.equals(VTP_ELEMENT)) {
                String path = attributes.getValue(PATH);
                String desc = attributes.getValue(DESC);
                vtpMap.put(path, desc);
            }
        }
    }
}
