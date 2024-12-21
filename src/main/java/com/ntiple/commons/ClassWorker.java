/**
 * @File        : ClassWorker.java
 * @Author      : 정재백
 * @Since       : 2024-12-05
 * @Description : class 탐색
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.FunctionUtil.Fn1avt;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.StringUtil.strreplace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class ClassWorker {
  private static final SimpleLogger log = SimpleLogger.getLogger();

  private static final Pattern PTN_WIN32FILEURL = Pattern.compile("^\\/[A-Z][:]\\/");
  private static final String CLASS_EXT = ".class";

  private static final String getResourcePath(ClassLoader loader, String path) {
    String ret = "";
    if (path == "") {
      LOOP: for (int inx = 0; inx < 2; inx++) {
        try {
          switch (inx) { case 0: { path = "."; } break; case 1: { path = "/"; } break; }
          ret = strreplace(String.valueOf(loader.getResource(path).getFile()), "\\", "/").replaceAll("^file:/", "/");
          break LOOP;
        } catch (Exception e) { log.trace("", e); }
      }
    } else {
      ret = strreplace(String.valueOf(loader.getResource(path).getFile()), "\\", "/").replaceAll("^file:/", "/");
    }
    if (PTN_WIN32FILEURL.matcher(ret).find()) { ret = ret.substring(1); }
    return ret;
  }

  static class ClassFileFilter implements FileFilter {
    private String base;
    private Fn1avt<Class<?>> callback;
    public ClassFileFilter(String base, Fn1avt<Class<?>> callback) {
      this.base = base;
      this.callback = callback;
    }

    @Override public boolean accept(File file) {
      if (file != null && file.exists()) {
        if (file.isDirectory()) {
          file.listFiles(this);
        } else {
          String name = strreplace(file.getAbsolutePath(), "\\", "/");
          LOOP: for (int inx = 0; inx < 2; inx++) {
            String prefix = base;
            if (inx == 0 && prefix.endsWith("/test/")) {
              prefix = cat(prefix.substring(0, prefix.length() - "/test/".length()), "/main/");
            }
            log.trace("FILE:{} / {} / {}", file, prefix, inx);
            if (name.startsWith(prefix) && name.endsWith(CLASS_EXT)) {
              name = strreplace(name.substring(prefix.length()).replaceAll("[.]class$", ""), "/", ".");
              log.trace("CLASS:{}", name);
              try {
                callback.apply(Class.forName(name));
                break LOOP;
              } catch (Exception e) {
                log.trace("E:", e);
              }
            }
          }
        }
      }
      return false;
    }
  }

  public static void findClasses(String bpath, String fpath, Fn1avt<Class<?>> callback) {
    String jarext = ".jar";
    try {
      File file = new File(fpath);
      if (file.exists()) {
        log.trace("FILE:{} / {}", bpath, fpath);
        file.listFiles(new ClassFileFilter(bpath, callback));
      } else if (
        fpath.indexOf(cat((jarext = ".jar"), "!/")) != -1 ||
        fpath.indexOf(cat((jarext = ".war"), "!/")) != -1) {
        int st = -1;
        String ext = jarext;
        String base = "WEB-INF/classes";
        if ((st = fpath.indexOf(cat(ext, "!"))) != -1) {
          String jpath= cat(fpath.substring(0, st), ext);
          String ipath = fpath.substring(st + ext.length() + 1)
            .replaceAll("^/", "")
            .replaceAll("WEB-INF[/]classes[!][/]", "/");
          JarFile jfile = new JarFile(new File(jpath));
          log.trace("PATH:{} / {} / {}", jfile, st, ipath);
          Enumeration<JarEntry> enums = jfile.entries();
          for (JarEntry entry = null; enums.hasMoreElements();) {
            entry = enums.nextElement();
            String ename = entry.getName();
            if(
              ename.startsWith(base) &&
              ename.endsWith(CLASS_EXT)) {
              ename = ename.substring(base.length());
              if (ename.startsWith(ipath)) {
                ename = ename.replaceAll("^/", "");
                ename = ename.substring(0, ename.length() - CLASS_EXT.length());
                ename = strreplace(ename, "/", ".");
                log.trace("ENTRY:{}", ename);
                try {
                  callback.apply(Class.forName(ename));
                } catch (Exception e) {
                  log.trace("E:", e);
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.debug("CANNOT FIND CLASSES IN {}", fpath);
    }
  }
  
  public static final void workClasses(ClassLoader loader, Fn1avt<Class<?>> callback, String... pkgs) {
    BufferedReader reader = null;
    try {
      String bpath = getResourcePath(loader, "");
      for (int inx = 0; inx < pkgs.length; inx++) {
        bpath = bpath.replaceAll("[/]resources[/]main[/]$", "/classes/java/main/");
        bpath = bpath.replaceAll("[/]resources[/]test[/]$", "/classes/java/test/");
        findClasses(bpath, getResourcePath(loader, strreplace(pkgs[inx], ".", "/")), callback);
      }
    } catch (Exception e) {
      log.debug("CANNOT ACCESS PACKAGE:{}{} / {}", "", pkgs, e.getMessage());
    } finally {
      safeclose(reader);
    }
  }
}
