/**
 * @File        : UtilsTestcase.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2024-03-09 최초 작성
 * @Description : 테스트 케이스
 **/
package com.ntiple.commons;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UtilsTestcase {
  @Test public void testSimple() throws Exception {
    assertTrue(true);
  }

  // @Test public void testPublish() throws Exception {
  //   HttpClient client = HttpUtil.httpclient(null, true, 1, null, null)
  //     .build();
  //   HttpRequest request = HttpUtil.request("https://nexus.ntiple.com/#browse/browse:maven-test", null)
  //     .POST(BodyPublishers.ofString(null, null))
  //     .build();
  //   assertTrue(true);
  // }
}
