/**
 * @File        : SimpleLogger.java
 * @Author      : 정재백
 * @Since       : 2024-11-20
 * @Description : 단순 콘솔로그 출력기
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.ReflectionUtil.findMethod;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLogger {
  private static SimpleLogger inst;
  private static Object srcLogger;
  private static Method[] mtdSrcLogger;
  private int level = 2;
  private PrintStream appender;
  public static SimpleLogger getLogger() {
    if (inst == null) {
      inst = new SimpleLogger();
      inst.appender = System.out;
    }
    return inst;
  }
  public static void setSrcLogger(Object source) {
    getLogger();
    SimpleLogger.srcLogger = null;
    SimpleLogger.mtdSrcLogger = null;
    try {
      Method[] mtdSrcLogger = new Method[4];
      for (int level = 0; level < 4; level++) {
        String name = "";
        switch (level) {
        case 0: { name = "trace"; } break;
        case 1: { name = "debug"; } break;
        case 2: { name = "info"; } break;
        case 3: { name = "warn"; } break;
        case 4: { name = "error"; } break;
        }
        Method mtd = findMethod(source.getClass(), name, new Class[] { String.class, Object[].class });
        mtdSrcLogger[level] = mtd;
      }
      SimpleLogger.srcLogger = source;
      SimpleLogger.mtdSrcLogger = mtdSrcLogger;
    } catch (Exception e) {
      e.printStackTrace();
      SimpleLogger.srcLogger = null;
      SimpleLogger.mtdSrcLogger = null;
    }
  }
  public int getLevel() { return this.level; }
  public void setLevel(int level) { this.level = level; }
  public void setAppender(PrintStream appender) { this.appender = appender; }
  public void trace(String fmt, Object... args) { _print(0, fmt, args); }
  public void debug(String fmt, Object... args) { _print(1, fmt, args); }
  public void info(String fmt, Object... args) { _print(2, fmt, args); }
  public void warn(String fmt, Object... args) { _print(3, fmt, args); }
  public void error(String fmt, Object... args) { _print(4, fmt, args); }
  private void _print(int level, String fmt, Object... args) {
    if (srcLogger != null && 
      mtdSrcLogger != null &&
      mtdSrcLogger.length > level &&
      mtdSrcLogger[level] != null) {
      try {
        mtdSrcLogger[level].invoke(srcLogger, new Object[] { fmt, args });
      } catch (Exception e) {
        SimpleLogger.srcLogger = null;
        SimpleLogger.mtdSrcLogger = null;
        _print(level, fmt, args);
      }
    } else {
      if (level >= this.level && appender != null) {
        Pattern ptn = Pattern.compile("[{][}]");
        Matcher mat = ptn.matcher(fmt);
        appender.println(String.format(mat.replaceAll("%s"), args));
        if (args != null) {
          for (int inx = 0; inx < args.length; inx++) {
            if (args[inx] instanceof Throwable) { cast(args[inx], Throwable.class).printStackTrace(appender); }
          }
        }
      }
    }
  }
}
