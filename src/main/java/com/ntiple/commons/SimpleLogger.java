/**
 * @File        : SimpleLogger.java
 * @Author      : 정재백
 * @Since       : 2024-11-20
 * @Description : 테스트
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ReflectionUtil.cast;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLogger {
  private static SimpleLogger inst;
  private int level = 2;
  private PrintStream appender;
  public static SimpleLogger getLogger() {
    if (inst == null) {
      inst = new SimpleLogger();
      inst.appender = System.out;
    }
    return inst;
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
