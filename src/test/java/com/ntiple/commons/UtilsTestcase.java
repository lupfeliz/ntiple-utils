/**
 * @File        : UtilsTestcase.java
 * @Author      : 정재백
 * @Since       : 2024-03-09
 * @Description : 테스트 케이스
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.IOUtils.reader;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.ntiple.commons.HttpUtil.HttpClientWorker;

@SuppressWarnings("unused")
public class UtilsTestcase {
  @Test public void testSimple() throws Exception {
    assertTrue(true);
  }

  // @Test public void testHttpClient() throws Exception {
  //   // sh gradlew cleanTest test -i --no-watch-fs --tests "com.ntiple.commons.UtilsTestcase.testHttpClient"
  //   java.io.InputStream istream = null;
  //   HttpClientWorker worker = new HttpUtil.HttpClientWorker("https://gitlab.ntiple.com");
  //   istream = cast(worker.work(null), istream);
  //   java.io.BufferedReader reader = null;
  //   System.out.println("GET RESULT...");
  //   try {
  //     reader = reader(istream, UTF8);
  //     for (String rl; (rl = reader.readLine()) != null;) {
  //       System.out.println(String.format("%s", rl));
  //     }
  //   } catch (Exception e) {
  //     e.printStackTrace();
  //   }
  // }
}
