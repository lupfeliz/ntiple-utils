/**
 * @File        : Constants.java
 * @Author      : 정재백
 * @Since       : 2024-03-07
 * @Description : 전역상수
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

public interface Constants {
  public static final char[] AVAIL_SYMBOLS = { '!', '#', '$', '%', '^', '*' };
  public static final String ALPHA = "alpha";
  public static final String ALPHANUM = "alphanum";
  public static final String ALPHANUMSYM = "alphanumsym";
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER = "Bearer";
  public static final String CHARSET = "charset";
  public static final String CONTENT_TYPE = "Content-type";
  public static final String CTYPE_FILE = " application/octet-stream";
  public static final String CTYPE_FORM = "application/x-www-form-urlencoded";
  public static final String CTYPE_HTML = "text/html";
  public static final String CTYPE_JSON = "application/json";
  public static final String CTYPE_MULTIPART = "multipart/form-data";
  public static final String CTYPE_TEXT = "text/plain";
  public static final String NUMBER = "number";
  public static final String PASSWORD = "password";
  public static final String REFERER = "Referer";
  public static final String S_HTTP = "http";
  public static final String S_HTTPS = "https";
  public static final String TIMEOUT = "timeout";
  public static final String TLS = "TLS";
  public static final String UTF8 = "UTF-8";
  public static final String ISO88591 = "ISO8859-1";
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";

  public static final String AES_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding";
  public static final String PBKDF2WithHmacSHA1 = "PBKDF2WithHmacSHA1";
  public static final String PBKDF2WithHmacSHA256 = "PBKDF2WithHmacSHA256";
  public static final String AES = "AES";
  public static final String RSA = "RSA";
  public static final String SHA1 = "SHA-1";
}
