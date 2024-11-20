/**
 * @File        : TestUtil.java
 * @Author      : 정재백
 * @Since       : 2024-10-26 
 * @Description : 테스트 레벨링 모듈
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.StringUtil.cat;

import java.io.File;
import java.net.URL;

public class TestUtil {
  private static final SimpleLogger log = SimpleLogger.getLogger();
  public static enum TestLevel {
    NONE(0),
    SIMPLE(1),
    DBCON(2),
    API(3),
    FULL(4),
    MANUAL(99);
    
    private final int value;
    TestLevel(int value) { this.value = value; }
    public int value() { return value; }
  }

  public static boolean isEnabled(String testName, TestLevel lvl) {
    boolean ret = false;
    if (lvl == null) { lvl = TestLevel.NONE; }
    String enabled = "";
    try {
      enabled = System.getProperty("project.build.test");
      if (enabled == null || "".equals(enabled)) { enabled = TestLevel.SIMPLE.name(); }
      TestLevel target = TestLevel.valueOf(enabled);
      if (target.value() >= lvl.value()) { ret = true; }
      log.info("LEVEL CHECK:{} / {}[{}] / {}[{}], {}, {}", testName,
        target, target.name(), lvl, lvl.name(), enabled, ret);
    } catch (Exception ignore) { }
    return ret;
  }

  public static File getResource(Class<?> cls, String uri) throws Exception {
    File ret = null;
    try {
      String[] paths = {
        "/build/classes/java/test/",
        "/build/classes/java/main/",
        "/build/resources/test/",
        "/build/resources/main/",
        "/build/webapps/",
        "/src/main/webapp/"
      };
      URL url = cls.getClassLoader().getResource("");
      if (url == null) { return ret; }

      String buildpath = url.getFile();
      buildpath = buildpath.replaceAll("\\\\", "/");
      if (buildpath.endsWith(paths[0])) {
        buildpath = cat(buildpath.substring(0, buildpath.length() - paths[0].length()));
      } else if (buildpath.endsWith(paths[1])) {
        buildpath = cat(buildpath.substring(0, buildpath.length() - paths[1].length()));
      }
      for (String p : paths) {
        File file = new File(cat(buildpath, "/", p, "/", uri).replaceAll("[/]+", "/"));
        log.debug("FILE:{}", file.getAbsolutePath());
        if (file.exists()) {
          ret = file;
          break;
        }
      }
      log.debug("BUILDPATH:{} / {}", buildpath, ret);
      // ret = new File(path);
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }
}
