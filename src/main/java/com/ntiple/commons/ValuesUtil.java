/**
 * @File        : ValuesUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-10-25 최초 작성
 * @Description : 각종 데이터 관련 유틸
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.ALPHA;
import static com.ntiple.commons.Constants.ALPHANUM;
import static com.ntiple.commons.Constants.ALPHANUMSYM;
import static com.ntiple.commons.Constants.AVAIL_SYMBOLS;
import static com.ntiple.commons.Constants.NUMBER;
import static com.ntiple.commons.Constants.PASSWORD;
import static java.lang.Math.abs;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import org.htmlcleaner.CleanerProperties;
// import org.htmlcleaner.HtmlCleaner;

public class ValuesUtil {

  static Pattern PTN_QUOT = Pattern.compile("[']");

  public static String random(String type, int length) {
    if (length < 1) { return ""; }
    StringBuilder ret = new StringBuilder();
    SecureRandom seed = new SecureRandom();
    int[] v1 = new int[] {};
    int[] v2 = new int[] {};
    int[] v3 = new int[] {};
    int d;
    boolean addsym = false;
    for (int inx = 0, off = 0; inx < length; inx++, off++) {
      if (off >= v1.length || off >= v2.length || off >= v3.length) {
        off = 0;
        v1 = seed.ints(length).toArray();
        v2 = seed.ints(length).toArray();
        v3 = seed.ints(length).toArray();
        inx--;
        continue;
      }
      switch(type) {
      case NUMBER:
        d = (abs(v1[off]) % 10);
        ret.append(String.valueOf(d));
        break;
      case ALPHA:
        d = (abs(v1[off]) % 26);
        if ((abs(v2[off]) % 2) == 0) {
          ret.append((char)(((int)'A') + d));
        } else {
          ret.append((char)(((int)'a') + d));
        }
        break;
      case ALPHANUMSYM:
      case PASSWORD:
        if (!addsym && inx + 1 == length) {
          /** 반드시 1개 이상의 특수기호가 필요한 경우 */
          d = (abs(v1[off]) % AVAIL_SYMBOLS.length);
          String str = String.valueOf(ret);
          int pos = abs(v3[off]) % str.length();
          ret.setLength(0);
          ret.append(str.substring(0, pos));
          ret.append(AVAIL_SYMBOLS[d]);
          ret.append(str.substring(pos));
          addsym = true;
        } else {
          if ((abs(v2[off]) % 12) < 5) {
            d = (abs(v1[off]) % 10);
            ret.append(String.valueOf(d));
          } else if ((abs(v2[off]) % 12) < 10) {
            d = (abs(v1[off]) % 26);
            if ((abs(v3[off]) % 2) == 0) {
              ret.append((char)(((int)'A') + d));
            } else {
              ret.append((char)(((int)'a') + d));
            }
          } else {
            d = (abs(v1[off]) % AVAIL_SYMBOLS.length);
            ret.append(AVAIL_SYMBOLS[d]);
            addsym = true;
          }
        }
        break;
      case ALPHANUM:
      default:
        if ((abs(v2[off]) % 2) == 0) {
          d = (abs(v1[off]) % 10);
          ret.append(String.valueOf(d));
        } else {
          d = (abs(v1[off]) % 26);
          if ((abs(v3[off]) % 2) == 0) {
            ret.append((char)(((int)'A') + d));
          } else {
            ret.append((char)(((int)'a') + d));
          }
        }
        break;
      }
    }
    return String.valueOf(ret);
  }

  public static String quot(String str) {
    String ret = str;
    Matcher mat = PTN_QUOT.matcher(ret);
    ret = mat.replaceAll("\\\\'");
    return "'" + ret + "'";
  }

  public static boolean isEmpty(String o) {
    return o == null || "".equals(o);
  }

  // public static String removeTags(Object html) {
  //   String ret = String.valueOf(html != null ? html : "");
  //   try {
  //     CleanerProperties props = new CleanerProperties();
  //     props.setPruneTags("script");
  //     ret = new HtmlCleaner(props).clean(String.valueOf(ret)).getText().toString();
  //     ret = ret.replaceAll("[ \r\n\t]+", " ");
  //   } catch (Exception ignore) { log.trace("E:{}", ignore); }
  //   return ret;
  // }
}
