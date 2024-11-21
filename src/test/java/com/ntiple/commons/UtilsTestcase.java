/**
 * @File        : UtilsTestcase.java
 * @Author      : 정재백
 * @Since       : 2024-03-09
 * @Description : 테스트 케이스
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.HttpUtil.httpWorker;
import static com.ntiple.commons.IOUtils.readAsString;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;
import static com.ntiple.commons.ReflectionUtil.cast;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.ntiple.commons.FunctionUtil.Fn2a;
import com.ntiple.commons.HttpUtil.HttpClientWorker;
import com.ntiple.commons.TestUtil.TestLevel;

@SuppressWarnings("all")
public class UtilsTestcase {
  @Test public void testSimple() throws Exception {
    assertTrue(true);
  }

  @Test public void testHttpClient() throws Exception {
    // sh gradlew cleanTest test -Dproject.build.test=MANUAL -i --no-watch-fs --tests "com.ntiple.commons.UtilsTestcase.testHttpClient"
    if (!TestUtil.isEnabled("testHttpClient", TestLevel.MANUAL)) { return; }
    SimpleLogger log = SimpleLogger.getLogger();
    // String content = cast(httpWorker("https://gitlab.ntiple.com")
    String content = cast(httpWorker("https://www.naver.com")
      .provider(p -> p.JDK_11)
      .method(p -> p.GET)
      .work((state, istream, headers, context) -> {
        log.setLevel(1);
        Object ret = null;
        try {
          ret = readAsString(istream, UTF8);
        } catch (Exception e) { log.debug("E:{}", e); }
        log.debug("HEADERS:{}", headers);
        return ret;
      }), content = null);
    log.debug("CONTENT:{}", content);
  }

  @Test public void testApacheHttpClient() throws Exception {
    // sh gradlew cleanTest test -Dproject.build.test=MANUAL -i --no-watch-fs --tests "com.ntiple.commons.UtilsTestcase.testApacheHttpClient"
    if (!TestUtil.isEnabled("testApacheHttpClient", TestLevel.MANUAL)) { return; }
    SimpleLogger log = SimpleLogger.getLogger();
    log.setLevel(1);
    StringBuilder content = new StringBuilder();
    // httpWorker("https://devlog.ntiple.com/devwas9998/study202403/api/atc/atc01001")
    // httpWorker("http://devsup.ntiple.com:10002/smp/smp01001p01")
    // httpWorker("https://devlog.ntiple.com")
    // httpWorker("https://wptest.ntiple.com")
    httpWorker()
      .url("https://203.245.30.232")
      // .host("https://devlog.ntiple.com")
      .host("https://wptest.ntiple.com")
      .provider(p -> p.APACHE_CLIENT_4_5)
      .method(p -> p.GET)
      // .method(p -> p.POST)
      // .contentType(p -> p.JSON)
      .contentType(p -> p.URL_ENCODED)
      // .headers(convert(new Object[][] {
      //   { "Host", "devlog.ntiple.com" }
      // }, newMap()))
      .agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0")
      .contents(convert(new Object[][] {
        { "searchType", "" },
        { "rowStart", 0 },
        { "rowCount", 10 }
      }, newMap()))
      .work((stat, stream, header, ctx) -> {
        try {
          content.append(readAsString(stream, UTF8));
        } catch (Exception e) { log.debug("E:{}", e); }
        // log.debug("HEADERS:{}", header);
        return content;
      });
    log.debug("CONTENT:{}", content);
  }
}
