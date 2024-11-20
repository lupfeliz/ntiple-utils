/**
 * @File        : FormatUtil.java
 * @Author      : 정재백
 * @Since       : 2024-03-06
 * @Description : 포맷관련 유틸
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.StringUtil.cat;

public class FormatUtil {
  public static String formatPhone(String phone) {
    String ret = phone;
    if (phone == null) { return ret; }
    ret = ret.replaceAll("[^0-9,^*]", "");
    switch (ret.length()) {
    case 9: {
      ret = cat(ret.substring(0, 2), "-", ret.substring(2, 5), "-", ret.substring(5));
    } break;
    case 10: {
      if (ret.startsWith("02")) {
        ret = cat(ret.substring(0, 2), "-", ret.substring(2, 6), "-", ret.substring(6));
      } else {
        ret = cat(ret.substring(0, 3), "-", ret.substring(3, 6), "-", ret.substring(6));
      }
    } break;
    case 11: {
      ret = cat(ret.substring(0, 3), "-", ret.substring(3, 7), "-", ret.substring(7));
    } break;
    }
    return ret;
  }
}
