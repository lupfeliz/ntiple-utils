/**
 * @File        : UtilsTestcase.java
 * @Author      : 정재백
 * @Since       : 2024-03-09
 * @Description : 테스트 케이스
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UtilsTestcase {
  @Test public void testSimple() throws Exception {
    assertTrue(true);
  }

  // @Test public void testHttpClient() throws Exception {
  //   // sh gradlew cleanTest test -i --no-watch-fs --tests "com.ntiple.commons.UtilsTestcase.testHttpClient"
  //   new HttpUtil.HttpClientWorker("https://gitlab.ntiple.com");
  // }
}
