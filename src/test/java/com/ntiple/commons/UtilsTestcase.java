/**
 * @File        : UtilsTestcase.java
 * @Author      : 정재백
 * @Since       : 2024-03-09
 * @Description : 테스트 케이스
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
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
    if (!TestUtil.isEnabled("testHttpClient", TestLevel.MANUAL)) { return; }
    SimpleLogger log = SimpleLogger.getLogger();
    // sh gradlew cleanTest test -Dproject.build.test=MANUAL -i --no-watch-fs --tests "com.ntiple.commons.UtilsTestcase.testHttpClient"
    String content = cast(httpWorker("https://gitlab.ntiple.com")
      .work((state, istream, headers) -> {
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
}
