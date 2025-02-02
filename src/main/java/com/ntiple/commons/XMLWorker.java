/**
 * @File        : XMLWorker.java
 * @Author      : 정재백
 * @Since       : 2025-02-02
 * @Description : xml 파싱
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ReflectionUtil.cast;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ntiple.commons.FunctionUtil.Fn1av;
import com.ntiple.commons.FunctionUtil.Fn4av;
import com.ntiple.commons.FunctionUtil.Fn5av;

public class XMLWorker {
  public static SAXParser parser(Fn1av<SAXParserFactory> conf) throws Exception {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    if (conf != null) { conf.apply(factory); }
    // factory.setNamespaceAware(true);
    factory.setValidating(false);
    factory.setFeature("http://xml.org/sax/features/namespaces", false);
    factory.setFeature("http://xml.org/sax/features/validation", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    return factory.newSAXParser();
  }

  public static DefaultHandler handler(
    Fn5av<String, String, String, Attributes, Map<String, Object>> startElement,
    Fn4av<String, String, String, Map<String, Object>> endElement,
    Fn4av<char[], Integer, Integer, Map<String, Object>> characters) {
    DefaultHandler ret = new DefaultHandler() {
      Map<String, Object> ctx = new LinkedHashMap<>();
      // @Override public void startDocument() throws SAXException { }
      // @Override public void endDocument() throws SAXException { }
      @Override public void startElement(String uri, String lname, String qname, Attributes attr) throws SAXException {
        startElement.apply(uri, lname, qname, attr, ctx);
      }
      @Override public void endElement(String uri, String lname, String qname) throws SAXException {
        endElement.apply(uri, lname, qname, ctx);
      }
      @Override public void characters(char ch[], int start, int length) throws SAXException {
        characters.apply(ch, start, length, ctx);
      }
    };
    return ret;
  }

  public static void parse(
    Fn1av<SAXParserFactory> config,
    Object source,
    Fn5av<String, String, String, Attributes, Map<String, Object>> startElement,
    Fn4av<String, String, String, Map<String, Object>> endElement,
    Fn4av<char[], Integer, Integer, Map<String, Object>> characters) throws Exception {
    SAXParser parser = parser(config);
    DefaultHandler handler = handler(startElement, endElement, characters);
    if (source instanceof File) {
      parser.parse(cast(source, File.class), handler);
    } else if (source instanceof InputStream) {
      parser.parse(cast(source, InputStream.class), handler);
    } else if (source instanceof Reader) {
      parser.parse(new InputSource(cast(source, Reader.class)), handler);
    } else {
      throw new RuntimeException("XML SOURCE NOT RECOGNIZED.");
    }
  }
}