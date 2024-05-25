/**
 * @File        : ProcUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-12-01 최초 작성
 * @Description : 프로세스유틸
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.IOUtils.safeclose;
import static com.ntiple.commons.IOUtils.writer;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.function.BiFunction;

import com.ntiple.commons.ConvertUtil.TmpLogger;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
public class ProcUtil {

  private static final TmpLogger log = TmpLogger.getLogger();
  
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
}
