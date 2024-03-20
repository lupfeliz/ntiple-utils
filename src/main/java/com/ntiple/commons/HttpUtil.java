/**
 * @File        : HttpUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2024-03-20 최초 작성
 * @Description : 테스트
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.CHARSET;
import static com.ntiple.commons.Constants.CONTENT_TYPE;
import static com.ntiple.commons.Constants.CTYPE_FILE;
import static com.ntiple.commons.Constants.CTYPE_FORM;
import static com.ntiple.commons.Constants.S_HTTP;
import static com.ntiple.commons.Constants.S_HTTPS;
import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.ConvertUtil.parseStr;
import static com.ntiple.commons.IOUtils.passthrough;
import static com.ntiple.commons.IOUtils.readAsString;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * 미완성 클래스 (현시점 사용불가)
 */
public class HttpUtil {

  private static HttpUtil instance;

  public static final String PTN_SCHEM_HTTP = "^http[s]{0,1}[:][/][/]";
  public static final Pattern PTN_PARAM = Pattern.compile("^([^=]+)[=](.*)$");
  public static String EXTERN_PROXY_IP;
  public static int EXTERN_PROXY_PORT;
  public static String EXTERN_PROXY_PROTO;

  public int defaultConnectionTimeout = 1000;

  public HttpUtil() {
    if (instance == null) { instance = this; }
  }

  /**
   * HTTPClient 를 사용하지 않고 직접 request 수행
   */
  public static String getUrlString(String targetUrl, String param, String srcCharset,
    String contentCharset, int connTimeout) throws IOException {
    URL url = null;
    URLConnection con = null;
    BufferedReader br = null;
    StringBuffer sb = new StringBuffer();
    String output = "";

    try {
      url = new URL(targetUrl);
      con = url.openConnection();
      if (connTimeout > 0) {
        con.setConnectTimeout(connTimeout);
      } else {
        con.setConnectTimeout(instance.defaultConnectionTimeout);
      }

      con.setDoOutput(true);

      if (!"".equals(parseStr(srcCharset, ""))) {
        con.setRequestProperty(CONTENT_TYPE, cat(CTYPE_FORM, ";", CHARSET, "=", srcCharset, ";"));
      } else {
        con.setRequestProperty(CONTENT_TYPE, cat(CTYPE_FORM, ";"));
      }

      OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
      if (!"".equals(parseStr(param, ""))) osw.write(param);
      osw.close();

      br = new BufferedReader(new InputStreamReader(con.getInputStream()));

      String inputLine;
      while ((inputLine = br.readLine()) != null) {
        sb.append(inputLine);
      }

      if (!"".equals(parseStr(contentCharset, ""))) {
        output = new String(sb.toString().getBytes(srcCharset), contentCharset);
      } else {
        output = sb.toString();
      }

      output = output.trim();

    } catch (Exception e) {
      // log.error("", e);
    } finally {
      if (br != null) { try { br.close(); } catch (Exception ignore) { } }
    }
    return output;
  }

  /**
   * URL 중 scheme 부분을 제외한 URI 부분의 double slash 를 삭제한다.
   * 예 :
   * http://localhost:8080/api///main =>
   * http://localhost:8080/api/main
   * ※ http* 이외의 scheme 은 무시
   */
  public static String cleanURL(String url) {
    final String SCHEME_HTTP = cat(S_HTTP, "://");
    final String SCHEME_HTTPS = cat(S_HTTPS, "://");
    if (url == null) { return url; }
    if (url.startsWith(SCHEME_HTTP)) {
      url = url.substring(SCHEME_HTTP.length());
      url = url.replaceAll("[/]+", "/");
      url = SCHEME_HTTP + url;
    } else if (url.startsWith(SCHEME_HTTPS)) {
      url = url.substring(SCHEME_HTTPS.length());
      url = url.replaceAll("[/]+", "/");
      url = SCHEME_HTTPS + url;
    } else {
      url = url.replaceAll("[/]+", "/");
    }
    return url;
  }

  public static HttpClient.Builder httpclient(CookieHandler ckhnd, boolean ignoreSSL, int httpver, String proxyAddr, Integer proxyPort) throws Exception {
    HttpClient.Builder ret = null;
    System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection,content-length,host");

    if (ckhnd == null) {
      CookieManager ckmng = new CookieManager();
      // CookieManager ckmng = new CookieManager(null, java.net.CookiePolicy.ACCEPT_NONE);
      CookieHandler.setDefault(ckmng);
      ckhnd = CookieHandler.getDefault();
      // log.debug("COOKIE-HANDLER:{}", ckhnd);
    }

    Version version = null;
    switch (httpver) {
    case 2: { version = Version.HTTP_2; } break;
    default: { version = Version.HTTP_1_1; }
    }

    ProxySelector proxy = null;
    if (proxyAddr != null && proxyPort != null) {
      proxy = ProxySelector.of(new InetSocketAddress(proxyAddr, proxyPort));
    } else {
      proxy = ProxySelector.getDefault();
    }

    ret = HttpClient.newBuilder()
      .version(version)
      // .sslContext(sslContext)
      .proxy(proxy)
      // .followRedirects(HttpClient.Redirect.ALWAYS)
      .followRedirects(HttpClient.Redirect.NORMAL)
      .cookieHandler(ckhnd);

    if (ignoreSSL) {
      X509ExtendedTrustManager trustManager = new X509ExtendedTrustManager() {
        @Override public void checkClientTrusted(X509Certificate[] x, String a) throws CertificateException { }
        @Override public void checkServerTrusted(X509Certificate[] x, String a) throws CertificateException { }
        @Override public void checkClientTrusted(X509Certificate[] x, String a, Socket s) throws CertificateException { }
        @Override public void checkClientTrusted(X509Certificate[] x, String a, SSLEngine e) throws CertificateException { }
        @Override public void checkServerTrusted(X509Certificate[] x, String a, Socket s) throws CertificateException { }
        @Override public void checkServerTrusted(X509Certificate[] x, String a, SSLEngine e) throws CertificateException { }
        @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[] {}; }
      };
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
      ret.sslContext(sslContext);
    }

    return ret;
  }

  public static HttpRequest.Builder request(String uri, String[][] headers) {
    HttpRequest.Builder ret = null;
    ret = HttpRequest.newBuilder();
    ret.uri(URI.create(uri));
    if (headers != null) {
      for (String[] header : headers) {
        if (header != null && header.length > 1) {
          ret.header(header[0], header[1]);
        }
      }
    }
    return ret;
  }

  public static HttpResponse<InputStream> httpExecute(HttpClient client, HttpRequest request) throws Exception {
    HttpResponse<InputStream> ret = client.send(request, BodyHandlers.ofInputStream());
      // .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
      // .thenApply(HttpResponse::body)
      // .thenAccept(istream -> {
      // });
    return ret;
  }

  // public static Header[] headers(HttpServletRequest request) throws Exception {
  //   List<Header> ret = new LinkedList<>();
  //   Enumeration<String> names = cast(request.getHeaderNames(), names = null);
  //   while (names.hasMoreElements()) {
  //     String name = names.nextElement();
  //     String value = request.getHeader(name);
  //     ret.add(new BasicHeader(name, value));
  //   }
  //   log.debug("HEADERS:{}", ret);
  //   return ret.toArray(new Header[ret.size()]);
  // }

  // public static Header[] headers(String[][] arg) throws Exception {
  //   List<Header> ret = new LinkedList<>();
  //   for (String[] item : arg) {
  //     if (item != null && item.length >= 2) {
  //       ret.add(new BasicHeader(item[0], item[1]));
  //     }
  //   }
  //   log.debug("HEADERS:{}", ret);
  //   return ret.toArray(new Header[ret.size()]);
  // }

  public static HttpResponse<InputStream> response(HttpClient client, HttpRequest req) throws Exception {
    HttpResponse<InputStream> ret = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
    return ret;
  }

  public static BufferedReader httpContentReader(HttpResponse<InputStream> response, String enc) throws Exception {
    return reader(response.body(), enc);
  }
  public static String httpContentStr(HttpResponse<InputStream> response) throws Exception { return httpContentStr(response, UTF8); }
  public static String httpContentStr(HttpResponse<InputStream> response, String enc) throws Exception {
    return readAsString(response.body(), enc);
  }

  // public static BodyPublisher nameValueBody(String[][] arg, String enc) throws Exception {
  //   BodyPublisher ret = null;
  //   ret = BodyPublishers.ofString("", Charset.forName(enc));
  //   // List<NameValuePair> list = new LinkedList<>();
  //   // for (String[] kv : arg) {
  //   //   if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
  //   //     list.add(new BasicNameValuePair(kv[0], kv[1]));
  //   //   }
  //   // }
  //   // entity = new UrlEncodedFormEntity(list, enc);
  //   return ret;
  // }

  public static class MultipartBuilder {
    // WebKitFormBoundaryQGvWeNAiOE4g2VM5
    private String boundary;
    private List<Object[]> data;
    public MultipartBuilder(String boundary) {
      this.boundary = boundary;
      data = new ArrayList<>();
    }
    public MultipartBuilder add(String name, Object value) {
      data.add(new Object[] { name, value });
      return this;
    }
    public MultipartBuilder add(String name, Object value, String fname, String ftype) {
      data.add(new Object[] { name, value, fname, ftype });
      return this;
    }
    public BodyPublisher build() throws Exception {
      /** Result request body */
      List<byte[]> raw = new ArrayList<>();
      String fname, ftype;
      /** Separator with boundary */
      byte[] separator = cat("--", boundary, "\r\nContent-Disposition: form-data; name=").getBytes(UTF8);
      /** Iterating over data parts */
      for (Object[] entry : data) {
        if (entry == null || entry.length < 2) { continue; }
        /** Opening boundary */
        raw.add(separator);
        /**
         * If value is type of Path (file) append content type with file name and file
         * binaries, otherwise simply append key=value
         **/
        if (entry[1] instanceof Path) {
          Path path = (Path) entry[1];
          fname = path.getFileName().toString();
          ftype = Files.probeContentType(path);
          raw.add(cat("\"", entry[0], "\"; filename=\"", fname,
            "\"\r\nContent-Type: ", ftype, "\r\n\r\n").getBytes(UTF8));
          raw.add(Files.readAllBytes(path));
          raw.add("\r\n".getBytes(UTF8));
        } else if (entry[1] instanceof InputStream) {
          fname = "file.bin";
          ftype = CTYPE_FILE;
          if (entry.length > 2) { fname = String.valueOf(entry[2]); }
          if (entry.length > 3) { ftype = String.valueOf(entry[3]); }
          ByteArrayOutputStream ostream = new ByteArrayOutputStream();
          try {
            passthrough((InputStream) entry[1], ostream);
            raw.add(ostream.toByteArray());
          } finally { safeclose(ostream); }
          raw.add(cat("\"", entry[0], "\"; filename=\"", fname,
            "\"\r\nContent-Type: ", ftype, "\r\n\r\n").getBytes(UTF8));
          raw.add("\r\n".getBytes(UTF8));
        } else if (entry[1] instanceof byte[]) {
          fname = "file.bin";
          ftype = CTYPE_FILE;
          if (entry.length > 2) { fname = String.valueOf(entry[2]); }
          if (entry.length > 3) { ftype = String.valueOf(entry[3]); }
          raw.add(cat("\"", entry[0], "\"; filename=\"", fname,
            "\"\r\nContent-Type: ", ftype, "\r\n\r\n").getBytes(UTF8));
          raw.add((byte[]) entry[1]);
          raw.add("\r\n".getBytes(UTF8));
        } else {
          raw.add(cat("\"", entry[0], "\"\r\n\r\n", entry[1], "\r\n")
            .getBytes(UTF8));
        }
      }
      /** Closing boundary */
      raw.add(cat("--", boundary, "--").getBytes(UTF8));

      // for (byte[] buf : raw) { System.out.print(new String(buf, UTF8)); }
      /** Serializing as byte array */
      return BodyPublishers.ofByteArrays(raw);
    }
  }

  public static MultipartBuilder multipart(String boundary) {
    return new MultipartBuilder(boundary);
  }
  /** Multipart 를 사용하기 위한 테스트코드 */
  // @Test public void testPublish() throws Exception {
  //   String boundary = "WebKitFormBoundaryQGvWeNAiOE4g2VM5";
  //   String str = HttpUtil.httpContentStr(
  //     HttpUtil.httpExecute(
  //       HttpUtil.httpclient(null, true, 1, null, null)
  //       .build(),
  //       // HttpUtil.request("https://nexus.ntiple.com/#browse/browse:maven-test", null)
  //       HttpUtil.request("http://localhost:8080/api/tst01001001", new String[][] {
  //           { CONTENT_TYPE, cat(CTYPE_MULTIPART, "; boundary=", boundary) },
  //         })
  //         .POST(HttpUtil.multipart(boundary)
  //           .add("test", "1234")
  //           .add("file", new byte[] { 1, 2, 3, 4 })
  //         .build())
  //       .build())
  //   );
  //   System.out.println(cat("CHECK:", str));
  //   assertTrue(true);
  // }


  // public static UrlEncodedFormEntity nameValueEntity(Map<String, Object> map, String enc) throws Exception {
  //   UrlEncodedFormEntity entity = null;
  //   List<NameValuePair> list = new LinkedList<>();
  //   for (String key : map.keySet()) {
  //     list.add(new BasicNameValuePair(key, String.valueOf(map.get(key))));
  //   }
  //   entity = new UrlEncodedFormEntity(list, enc);
  //   return entity;
  // }

  // public static HttpEntity stringEntity(Object param, String type, String enc) throws Exception {
  //   StringEntity entity = new StringEntity(String.valueOf(param), enc);
  //   entity.setContentType(cat(type, ";charset=", enc));
  //   return entity;
  // }

  public static String urlParamString(String[][] arg, String enc) throws Exception {
    StringBuilder ret = new StringBuilder();
    for (String[] kv : arg) {
      if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
        if (ret.length() == 0) {
          ret.append("?");
        } else {
          ret.append("&");
        }
        ret.append(URLEncoder.encode(kv[0], enc))
          .append("=").append(URLEncoder.encode(kv[1], enc));
      }
    }
    return String.valueOf(ret);
  }

  // public static void copyHeaders(HttpServletRequest requestf, HttpRequestBase requestt) throws Exception {
  //   Enumeration<String> names = cast(requestf.getHeaderNames(), names = null);
  //   while (names.hasMoreElements()) {
  //     String name = names.nextElement();
  //     String value = requestf.getHeader(name);
  //     requestt.addHeader(name, value);
  //   }
  // }

  // public static Map<String, Object> param(HttpServletRequest request) {
  //   Map<String, Object> ret = new LinkedHashMap<String, Object>();
  //   Enumeration<String> keys = cast(request.getAttributeNames(), keys = null);
  //   while (keys.hasMoreElements()) {
  //     String key = keys.nextElement();
  //     Object val = request.getAttribute(key);
  //     ret.put(key, val);
  //   }
  //   return ret;
  // }

  // public static Map<String, Object> param(HttpSession session) {
  //   Map<String, Object> ret = new LinkedHashMap<String, Object>();
  //   Enumeration<String> keys = cast(session.getAttributeNames(), keys = null);
  //   while (keys.hasMoreElements()) {
  //     String key = keys.nextElement();
  //     Object val = session.getAttribute(key);
  //     ret.put(key, val);
  //   }
  //   return ret;
  // }

  // public static Map<String, Object> param(ServletContext context) {
  //   Map<String, Object> ret = new LinkedHashMap<String, Object>();
  //   Enumeration<String> keys = cast(context.getAttributeNames(), keys = null);
  //   while (keys.hasMoreElements()) {
  //     String key = keys.nextElement();
  //     Object val = context.getAttribute(key);
  //     ret.put(key, val);
  //   }
  //   return ret;
  // }

  public static Map<String, Object> param(String str, String enc) {
    Map<String, Object> ret = new LinkedHashMap<String, Object>();
    str = str.trim();
    str = str.replaceAll("^[?]", "");
    String[] split = str.split("[&]");
    Matcher mat = null;
    for (String kvstr : split) {
      mat = PTN_PARAM.matcher(kvstr);
      if (mat.find() && mat.groupCount() == 2) {
        String key = mat.group(1);
        String val = mat.group(2);
        try {
          val = URLDecoder.decode(val, enc);
        } catch (Exception ignore) { }
        ret.put(key, val);
      }
    }
    return ret;
  }
}
