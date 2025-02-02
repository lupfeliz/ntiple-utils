/**
 * @File        : XMLWorker.java
 * @Author      : 정재백
 * @Since       : 2025-02-02
 * @Description : xml 파싱
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ConvertUtil.parseInt;
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
import com.ntiple.commons.FunctionUtil.Fn5av;
import com.ntiple.commons.FunctionUtil.Fn6av;

public class XMLWorker {
  public static SAXParser xmlParser(Fn1av<SAXParserFactory> conf) throws Exception {
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

  public static DefaultHandler xmlHandler(
    Fn6av<String, String, String, Integer, Attributes, Map<String, Object>> startElement,
    Fn5av<String, String, String, Integer, Map<String, Object>> endElement,
    Fn5av<char[], Integer, Integer, Integer, Map<String, Object>> characters) { return xmlHandler(startElement, endElement, characters, null); }
  public static DefaultHandler xmlHandler(
    Fn6av<String, String, String, Integer, Attributes, Map<String, Object>> startElement,
    Fn5av<String, String, String, Integer, Map<String, Object>> endElement,
    Fn5av<char[], Integer, Integer, Integer, Map<String, Object>> characters,
    Map<String, Object> _ctx) {
    if (_ctx == null) { _ctx = new LinkedHashMap<>(); }
    final Map<String, Object> ctx = _ctx;
    DefaultHandler ret = new DefaultHandler() {
      // @Override public void startDocument() throws SAXException { }
      // @Override public void endDocument() throws SAXException { }
      @Override public void startElement(String uri, String lname, String qname, Attributes attr) throws SAXException {
        Integer depth = xmlDepth(ctx, 1);
        startElement.apply(uri, lname, qname, depth, attr, ctx);
      }
      @Override public void endElement(String uri, String lname, String qname) throws SAXException {
        Integer depth = xmlDepth(ctx, -1);
        endElement.apply(uri, lname, qname, depth, ctx);
      }
      @Override public void characters(char ch[], int start, int length) throws SAXException {
        Integer depth = xmlDepth(ctx, 0);
        characters.apply(ch, start, length, depth, ctx);
      }
    };
    return ret;
  }

  public static String xmlAttr(Attributes attrs, String key) {
    String ret = null;
    LOOP: for (int inx = 0; inx < attrs.getLength(); inx++) {
      String name = attrs.getQName(inx);
      String value = attrs.getValue(inx);
      if (name != null && name.equals(key)) {
        ret = value;
        break LOOP;
      }
      continue LOOP;
    }
    return ret;
  }

  public static Map<String, String> xmlAttrs(Attributes attrs) {
    Map<String, String> ret = new LinkedHashMap<>();
    for (int inx = 0; inx < attrs.getLength(); inx++) {
      String name = attrs.getQName(inx);
      String value = attrs.getValue(inx);
      if (name != null) { ret.put(name, value); }
    }
    return ret;
  }

  public static Integer xmlDepth(Map<String, Object> ctx, int inc) { return xmlDepth(ctx, "depth", inc); }
  public static Integer xmlDepth(Map<String, Object> ctx, String key, int inc) {
    Integer ret = 0;
    ret = parseInt(ctx.get(key), 0) + inc;
    ctx.put(key, ret);
    return ret;
  }

  public static void parseXML(
    Fn1av<SAXParserFactory> config,
    Object source,
    Fn6av<String, String, String, Integer, Attributes, Map<String, Object>> startElement,
    Fn5av<String, String, String, Integer, Map<String, Object>> endElement,
    Fn5av<char[], Integer, Integer, Integer, Map<String, Object>> characters) throws Exception { parseXML(config, source, startElement, endElement, characters, null); }
  public static void parseXML(
    Fn1av<SAXParserFactory> config,
    Object source,
    Fn6av<String, String, String, Integer, Attributes, Map<String, Object>> startElement,
    Fn5av<String, String, String, Integer, Map<String, Object>> endElement,
    Fn5av<char[], Integer, Integer, Integer, Map<String, Object>> characters,
    Map<String, Object> ctx) throws Exception {
    SAXParser parser = xmlParser(config);
    DefaultHandler handler = xmlHandler(startElement, endElement, characters, ctx);
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