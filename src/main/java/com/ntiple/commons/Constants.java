/**
 * @File        : Constants.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2024-03-07 최초 작성
 * @Description : 전역상수
 **/
package com.ntiple.commons;

public interface Constants {
  static final char[] AVAIL_SYMBOLS = { '!', '#', '$', '%', '^', '*' };
  static final String ALPHA = "alpha";
  static final String ALPHANUM = "alphanum";
  static final String ALPHANUMSYM = "alphanumsym";
  static final String AUTHORIZATION = "Authorization";
  static final String BEARER = "Bearer";
  static final String CHARSET = "charset";
  static final String CONTENT_TYPE = "Content-type";
  static final String CTYPE_FILE = " application/octet-stream";
  static final String CTYPE_FORM = "application/x-www-form-urlencoded";
  static final String CTYPE_HTML = "text/html";
  static final String CTYPE_JSON = "application/json";
  static final String CTYPE_MULTIPART = "multipart/form-data";
  static final String CTYPE_TEXT = "text/plain";
  static final String NUMBER = "number";
  static final String PASSWORD = "password";
  static final String REFERER = "Referer";
  static final String S_HTTP = "http";
  static final String S_HTTPS = "https";
  static final String TIMEOUT = "timeout";
  static final String TLS = "TLS";
  static final String UTF8 = "UTF-8";
  static final String ISO88591 = "ISO8859-1";
  static final String X_FORWARDED_FOR = "X-Forwarded-For";

  static final String AES_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding";
  static final String PBKDF2WithHmacSHA1 = "PBKDF2WithHmacSHA1";
  static final String PBKDF2WithHmacSHA256 = "PBKDF2WithHmacSHA256";
  static final String AES = "AES";
  static final String RSA = "RSA";
  static final String SHA1 = "SHA-1";
}
