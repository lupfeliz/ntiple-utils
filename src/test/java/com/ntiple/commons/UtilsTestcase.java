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
import static com.ntiple.commons.IOUtil.readAsString;
import static com.ntiple.commons.IOUtil.reader;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.IOUtil.writer;
import static com.ntiple.commons.ReflectionUtil.cast;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.Test;

import com.ntiple.commons.TestUtil.TestLevel;

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
      .provider(p -> p.JDK_11())
      .method(p -> p.GET())
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
      .url("https://devlog.ntiple.com")
      // .url("https://wptest.ntiple.com")
      // .address(new String[] { "203.245.30.232" })
      // .provider(p -> p.JDK_11())
      .provider(p -> p.APACHE_CLIENT_4_5())
      // .method(p -> p.GET())
      // .method(p -> p.POST())
      // .contentType(p -> p.JSON())
      // .contentType(p -> p.URL_ENCODED())
      // .headers(convert(new Object[][] {
      // }, newMap()))
      .agent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0")
      // .contents(convert(new Object[][] {
      //   { "searchType", "" },
      //   { "rowStart", 0 },
      //   { "rowCount", 10 }
      // }, newMap()))
      .work((stat, stream, header, ctx) -> {
        log.debug("CHECK-HEADERS:{}", header);
        // CHECK-HEADERS:{access-control-allow-origin=[https://devlog.ntiple.com], cache-control=[no-cache], content-type=[text/html; charset=UTF-8], date=[Sat, 23 Nov 2024 20:54:25 GMT], last-modified=[Fri, 22 Nov 2024 22:15:27 GMT], server=[Apache/2.4.52 (Ubuntu)], transfer-encoding=[chunked], vary=[Accept-Encoding], wpo-cache-status=[cached]}
        // CHECK-HEADERS:{access-control-allow-origin=[https://devlog.ntiple.com], cache-control=[no-cache], content-type=[text/html; charset=UTF-8], date=[Sat, 23 Nov 2024 20:54:55 GMT], last-modified=[Fri, 22 Nov 2024 22:15:27 GMT], server=[Apache/2.4.52 (Ubuntu)], transfer-encoding=[chunked], vary=[Accept-Encoding], wpo-cache-status=[cached]}
        return content.append(readAsString(stream, UTF8));
      });
    log.debug("CONTENT:{}", content);
  }

  @Test public void testUpload() throws Exception {
    if (!TestUtil.isEnabled("testUpload", TestLevel.MANUAL)) { return; }
    SimpleLogger log = SimpleLogger.getLogger();
    log.setLevel(1);
    final StringBuilder sb = new StringBuilder();
    httpWorker()
      .url("http://devsup.ntiple.com:10002/smp/smp01001p01")
      // .url("http://localhost:8080/smp/smp01001p01")
      .method(p -> p.POST())
      .contentType(p -> p.MULTIPART())
      .contents(convert(new Object[][] {
        { "text", "한글" },
        { "file", new File("./README.md") },
      }, newMap()))
      .work((state, istream, headers, context) -> {
        Object ret = null;
        try {
          sb.append(readAsString(istream, UTF8));
        } catch (Exception e) { log.debug("E:{}", e); }
        log.debug("HEADERS:{}", headers);
        return ret;
      });
    log.debug("CONTENT:{}", sb);
  }

  @Test public void testSocketDump() throws Exception {
    if (!TestUtil.isEnabled("testSocketDump", TestLevel.MANUAL)) { return; }
    SimpleLogger log = SimpleLogger.getLogger();
    log.setLevel(1);
    ServerSocket ss = new ServerSocket(8080);
    Socket sock = null;
    InputStream istream = null;
    OutputStream ostream = null;
    BufferedReader reader = null;
    Writer writer = null;
    while(true) {
      try {
        sock = ss.accept();
        istream = sock.getInputStream();
        reader = reader(istream, UTF8);
        log.debug("READER-CREATED!!!");
        ostream = sock.getOutputStream();
        writer = writer(ostream, UTF8);
        log.debug("WRITER-CREATED!!!");
        for (String rl; (rl = reader.readLine()) != null;) {
          log.debug("LINE:{}", rl);
        }
        break;
      } finally {
        safeclose(reader);
        safeclose(writer);
        safeclose(istream);
        safeclose(ostream);
        safeclose(sock);
      }
    }
    safeclose(ss);
  }
}
