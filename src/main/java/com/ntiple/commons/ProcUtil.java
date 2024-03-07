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

import java.io.InputStream;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j
public class ProcUtil {
  
  public static String execRawCmd(Runtime rtm, String[] cmd, byte[] buf) throws Exception {
    String ret = null;
    InputStream istream = null;
    InputStream estream = null;
    Process prc;
    StringBuilder isb = new StringBuilder();
    StringBuilder esb = new StringBuilder();
    try {
      // log.info("EXECUTE-CMD:{}{}", "", cmd);
      prc = rtm.exec(cmd);
      istream = prc.getInputStream();
      estream = prc.getErrorStream();
      for (int rl; (rl = istream.read(buf, 0, buf.length)) != -1; isb.append(new String(buf, 0, rl, UTF8)));
      for (int rl; (rl = estream.read(buf, 0, buf.length)) != -1; esb.append(new String(buf, 0, rl, UTF8)));
      // log.trace("STD:{}", isb);
      // log.trace("ERR:{}", esb);
      ret = String.valueOf(isb);
      if (esb.length() > 0) {
        // log.error("ERROR:{}", esb);
        throw new RuntimeException("");
      }
    } finally {
      safeclose(istream);
      safeclose(estream);
    }
    return ret;
  }
}
