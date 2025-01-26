/**
 * @File        : ProcUtil.java
 * @Author      : 정재백
 * @Since       : 2023-12-01
 * @Description : 프로세스유틸
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.IOUtil.writer;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class ProcUtil {

  private static final SimpleLogger log = SimpleLogger.getLogger();
  
  public static String execRawCmd(Runtime rtm, String[] cmd, byte[] buf) throws Exception { return execRawCmd(rtm, cmd, buf, true, -1, null); }
  public static String execRawCmd(Runtime rtm, String[] cmd, byte[] buf, boolean errstop, long wait, BiFunction<StringBuilder, StringBuilder, Boolean> fnfailed) throws Exception {
    String ret = null;
    InputStream istream = null;
    InputStream estream = null;
    BufferedWriter writer = null;
    Process prc = null;
    StringBuilder isb = new StringBuilder();
    StringBuilder esb = new StringBuilder();
    RETRY_LOOP: for (int retry = 0; retry < 3; retry ++) {
      try {
        // log.trace("EXECUTE-CMD:{}{}", "", cmd);
        prc = rtm.exec(cmd);
        if (wait != -1) { prc.wait(wait); }
        istream = prc.getInputStream();
        estream = prc.getErrorStream();
        writer = writer(prc.getOutputStream(), UTF8);
        final Writer WR = writer;
        new Thread() {
          @Override public void run() {
            try {
              sleep(1000);
              WR.append("\r\n").flush();
            } catch (Exception ignore) { log.trace("E:{}", ignore); }
          }
        }.start();

        for (int rl; (rl = istream.read(buf, 0, buf.length)) != -1; isb.append(new String(buf, 0, rl, UTF8)));
        for (int rl; (rl = estream.read(buf, 0, buf.length)) != -1; esb.append(new String(buf, 0, rl, UTF8)));
        // log.trace("STD:{}", isb);
        // log.trace("ERR:{}", esb);
        ret = String.valueOf(isb);
        if (esb.length() > 0 && errstop) {
          if (fnfailed != null && fnfailed.apply(isb, esb)) {
            continue RETRY_LOOP;
          }
          // log.error("ERROR:{} / {} / {}", cmd, errstop, esb);
          throw new RuntimeException("ERROR! EXEC CMD");
        } else {
          break RETRY_LOOP;
        }
      } finally {
        safeclose(writer);
        safeclose(istream);
        safeclose(estream);
        if (prc != null) { try { prc.destroy(); } catch (Exception ignore) { log.trace("E:{}", ignore); } }
      }
    }
    return ret;
  }

  public static void sleep(long ms) {
    try { Thread.sleep(ms); } catch (Exception ignore) { log.trace("E:{}", ignore); }
  }

  /**
   * Debounces {@code callable} by {@code delay}, i.e., schedules it to be
   * executed after {@code delay},
   * or cancels its execution if the method is called with the same key within the
   * {@code delay} again.
   * 출처 : https://stackoverflow.com/questions/4742210/implementing-debounce-in-java
   */
  public static class Debouncer {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<Object, Future<?>> delayedMap = new ConcurrentHashMap<>();

    /**
     * Debounces {@code callable} by {@code delay}, i.e., schedules it to be
     * executed after {@code delay},
     * or cancels its execution if the method is called with the same key within the
     * {@code delay} again.
     */
    public void debounce(final Object key, final Runnable runnable, long delay, TimeUnit unit) {
      final Future<?> prev = delayedMap.put(key, scheduler.schedule(new Runnable() {
        @Override public void run() {
          try { runnable.run(); } finally { delayedMap.remove(key); }
        }
      }, delay, unit));
      if (prev != null) { prev.cancel(true); }
    }
    public void debounce(final Object key, final Runnable runnable, long delay) { debounce(key, runnable, delay, TimeUnit.MILLISECONDS); }
    public void shutdown() { scheduler.shutdownNow(); }
  }
}
