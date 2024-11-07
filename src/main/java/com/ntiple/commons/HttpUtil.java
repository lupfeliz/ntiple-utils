/**
 * @File        : HttpUtil.java
 * @Author      : 정재백
 * @Since       : 2024-03-20
 * @Description : 테스트
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.S_HTTP;
import static com.ntiple.commons.Constants.S_HTTPS;
import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.arr;
import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.ConvertUtil.EMPTY_CLS;
import static com.ntiple.commons.ConvertUtil.EMPTY_OBJ;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;

import java.io.BufferedReader;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;

import com.ntiple.commons.ConvertUtil.TmpLogger;

public class HttpUtil {

  private static final Class<?> getclass(String clsname) throws Exception { return Class.forName(clsname); }
  private static final Method getmethod(Class<?> cls, String name, Class<?>... arg) throws Exception {
    try { return cls.getDeclaredMethod(name, arg); } catch (Throwable t) { };
    try { return cls.getMethod(name, arg); } catch (Throwable t) { };
    return null;
  }
  private static final Field getfield(Class<?> cls, String name) throws Exception {
    try { return cls.getDeclaredField(name); } catch (Throwable t) { };
    try { return cls.getField(name); } catch (Throwable t) { };
    return null;
  }
  private static final Object getfieldv(Class<?> cls, String name) throws Exception { return getfieldv(cls, name, null); }
  private static final Object getfieldv(Class<?> cls, String name, Object inst) throws Exception { return getfield(cls, name).get(inst); }
  private static final Constructor<?> getconstr(Class<?> cls) throws Exception { return cls.getDeclaredConstructor(EMPTY_CLS); }
  private static final Constructor<?> getconstr(Class<?> cls, Class<?>... arg) throws Exception { return cls.getDeclaredConstructor(arg); }
  private static final Object newinstance(Class<?> cls) throws Exception { return getconstr(cls).newInstance(EMPTY_OBJ); }

  private static final TmpLogger log = TmpLogger.getLogger();

  public int defaultConnectionTimeout = 1000;

  public static final Pattern PTN_PARAM = Pattern.compile("^([^=]+)[=](.*)$");

  private static Class<?> HttpServletRequest = null;
  private static Class<?> HttpClient = null;
  private static Class<?> HttpHost = null;
  private static Constructor<?> HttpHostConstr = null;
  private static Method HCBCreate = null;
  private static Method HCBBuild = null;
  private static Method HCBSetSSLContext = null;
  private static Method HCBSetRedirectStrategy = null;
  private static Method HCBSetDefaultCookieStore = null;
  private static Method HCBSetKeepAliveStrategy = null;
  private static Method HCBSetConnectionManager = null;
  private static Method RequestGetHeaderNames = null;
  private static Method RequestGetHeader = null;
  private static Method RequestBaseAddHeader = null;
  private static Method ResponseGetEntity = null;
  private static Method HttpEntityGetContent = null;
  private static Method RequestGetInputStream = null;
  private static Method RequestGetAttributeNames = null;
  private static Method RequestGetAttribute = null;
  private static Method HttpClientExecute1 = null;
  private static Method HttpClientExecute2 = null;

  private static Class<?> LaxRedirectStrategy = null;
  private static Class<?> BasicCookieStore = null;
  private static Class<?> PoolingHttpClientConnectionManager = null;
  private static Class<?> UrlEncodedFormEntity = null;
  private static Constructor<?> UrlEncodedFormEntityConstr = null;
  private static Class<?> BasicNameValuePair = null;
  private static Constructor<?> BasicNameValuePairConstr = null;
  private static Class<?> StringEntity = null;
  private static Constructor<?> StringEntityConstr = null;
  private static Method SESetContentType = null;
  private static Constructor<?> BasicHeaderConstr = null;

  private static Object sslContext = null;
  private static Object connectionKeepAliveStrategy = null;
  private static Object connectionManager = null;
  static {
    if (HttpServletRequest == null) {
    /** for javax.servlet package (JDK 1.8 ver)  */
      try {
        HttpServletRequest = getclass("javax.servlet.http.HttpServletRequest");
      } catch (Throwable ignore) { log.trace("E:{}", ignore); }
    }
    /** for jakarta package (over JDK 1.8 ver)  */
    if (HttpServletRequest == null) {
      try {
        HttpServletRequest = getclass("jakarta.servlet.http.HttpServletRequest");
      } catch (Throwable ignore) { log.trace("E:{}", ignore); }
    }
    try {
      HttpHost = getclass("org.apache.http.HttpHost");
      HttpHostConstr = getconstr(HttpHost, arr(String.class, int.class, String.class));
      HttpClient = getclass("org.apache.http.client.HttpClient");
      Class<?> HttpClientBuilder = getclass("org.apache.http.impl.client.HttpClientBuilder");
      LaxRedirectStrategy = getclass("org.apache.http.impl.client.LaxRedirectStrategy");
      BasicCookieStore = getclass("org.apache.http.impl.client.BasicCookieStore");
      Class<?> RedirectStrategy = getclass("org.apache.http.client.RedirectStrategy");
      Class<?> CookieStore = getclass("org.apache.http.client.CookieStore");
      Class<?> ConnectionKeepAliveStrategy = getclass("org.apache.http.conn.ConnectionKeepAliveStrategy");
      Class<?> Registry = getclass("org.apache.http.config.Registry");
      Class<?> HttpClientConnectionManager = getclass("org.apache.http.conn.HttpClientConnectionManager");
      Class<?> SSLContextBuilder = getclass("org.apache.http.ssl.SSLContextBuilder");
      Class<?> TrustStrategy = getclass("org.apache.http.ssl.TrustStrategy");
      Class<?> RegistryBuilder = getclass("org.apache.http.config.RegistryBuilder");

      Class<?> HttpUriRequest = getclass("org.apache.http.client.methods.HttpUriRequest");
      Class<?> HttpRequest = getclass("org.apache.http.HttpRequest");
      HttpClientExecute1 = getmethod(HttpClient, "execute", arr(HttpUriRequest));
      HttpClientExecute2 = getmethod(HttpClient, "execute", arr(HttpHost, HttpRequest));
      {
        if (HttpServletRequest != null) {
          RequestGetHeaderNames = getmethod(HttpServletRequest, "getHeaderNames", arr());
          RequestGetHeader = getmethod(HttpServletRequest, "getHeader", arr(String.class));
          RequestGetInputStream = getmethod(HttpServletRequest, "getInputStream", EMPTY_CLS);
          RequestGetAttributeNames = getmethod(HttpServletRequest, "getAttributeNames", EMPTY_CLS);
          RequestGetAttribute = getmethod(HttpServletRequest, "getAttribute", arr(String.class));
        }
        Class<?> HttpRequestBase = getclass("org.apache.http.client.methods.HttpRequestBase");
        RequestBaseAddHeader = getmethod(HttpRequestBase, "addHeader", arr(String.class, String.class));
      }
      {
        Class<?> HttpEntity = getclass("org.apache.http.HttpEntity");
        Class<?> HttpResponse = getclass("org.apache.http.HttpResponse");
        ResponseGetEntity = getmethod(HttpResponse, "getEntity", EMPTY_CLS);
        HttpEntityGetContent = getmethod(HttpEntity, "getContent", EMPTY_CLS);
      }
      {
        Class<?> BasicHeader = getclass("org.apache.http.message.BasicHeader");
        BasicHeaderConstr = getconstr(BasicHeader, arr(String.class, String.class));
      }
      UrlEncodedFormEntity = getclass("org.apache.http.client.entity.UrlEncodedFormEntity");
      UrlEncodedFormEntityConstr = getconstr(UrlEncodedFormEntity, arr(List.class, String.class));
      BasicNameValuePair = getclass("org.apache.http.message.BasicNameValuePair");
      BasicNameValuePairConstr = getconstr(BasicNameValuePair, arr(String.class, String.class));

      StringEntity = getclass("org.apache.http.entity.StringEntity");
      StringEntityConstr = getconstr(StringEntity, arr(String.class, String.class));
      SESetContentType = getmethod(StringEntity, "setContentType", arr(String.class));

      PoolingHttpClientConnectionManager = getclass("org.apache.http.impl.conn.PoolingHttpClientConnectionManager");
      HCBCreate = getmethod(HttpClientBuilder, "create");
      HCBBuild = getmethod(HttpClientBuilder, "build");
      HCBSetSSLContext = getmethod(HttpClientBuilder, "setSSLContext", SSLContext.class);
      HCBSetRedirectStrategy = getmethod(HttpClientBuilder, "setRedirectStrategy", RedirectStrategy);
      HCBSetDefaultCookieStore = getmethod(HttpClientBuilder, "setDefaultCookieStore", CookieStore);
      HCBSetKeepAliveStrategy = getmethod(HttpClientBuilder, "setKeepAliveStrategy", ConnectionKeepAliveStrategy);
      HCBSetConnectionManager = getmethod(HttpClientBuilder, "setConnectionManager", HttpClientConnectionManager);
      {
        Method sslLoadTrustMaterial = getmethod(SSLContextBuilder, "loadTrustMaterial", KeyStore.class, TrustStrategy);
        Method sslBuild = getmethod(SSLContextBuilder, "build", EMPTY_CLS);
        Object sslContextBuilder = newinstance(SSLContextBuilder);
        Object trustStrategy = Proxy.newProxyInstance(TrustStrategy.getClassLoader(),
          new Class<?>[] { TrustStrategy }, new InvocationHandler() {
          @Override public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            if ("isTrusted".equals(m.getName()) && a.length == 2) { return true; }
            return null;
            }
        });
        sslContextBuilder = sslLoadTrustMaterial.invoke(sslContextBuilder, arr(null, trustStrategy));
        sslContext = sslBuild.invoke(sslContextBuilder, EMPTY_OBJ);
      }
      {
        connectionKeepAliveStrategy = Proxy.newProxyInstance(ConnectionKeepAliveStrategy.getClassLoader(),
          new Class<?>[] { ConnectionKeepAliveStrategy }, new InvocationHandler() {
            @Override public Object invoke(Object p, Method m, Object[] a) throws Throwable {
              /** 5 seconds */
              if ("getKeepAliveDuration".equals(m.getName()) && a.length == 2) { return Long.valueOf(5 * 1000); }
              return null;
            }
          });
      }
      {
        Class<?> PlainConnectionSocketFactory = getclass("org.apache.http.conn.socket.PlainConnectionSocketFactory");
        Class<?> SSLConnectionSocketFactory = getclass("org.apache.http.conn.ssl.SSLConnectionSocketFactory");
        Class<?> NoopHostnameVerifier = getclass("org.apache.http.conn.ssl.NoopHostnameVerifier");
        Class<?> HostnameVerifier = getclass("javax.net.ssl.HostnameVerifier");
        Method regCreate = getmethod(RegistryBuilder, "create", EMPTY_CLS);
        Method regRegister = getmethod(RegistryBuilder, "register", arr(String.class, Object.class));
        Method sslBuild = getmethod(RegistryBuilder, "build");
        Constructor<?> sslfactory = getconstr(SSLConnectionSocketFactory, arr(SSLContext.class, HostnameVerifier));
        Object reg = null;
        Object verifier = getfieldv(NoopHostnameVerifier, "INSTANCE");
        Object sslcon = sslfactory.newInstance(arr(sslContext, verifier));

        reg = regCreate.invoke(null, EMPTY_OBJ);
        reg = regRegister.invoke(reg, arr(S_HTTP, getfieldv(PlainConnectionSocketFactory, "INSTANCE")));
        reg = regRegister.invoke(reg, arr(S_HTTPS, sslcon));
        connectionManager = getconstr(PoolingHttpClientConnectionManager, arr(Registry)).newInstance(sslBuild.invoke(reg));
      }
    // } catch (Throwable ignore) { log.trace("E:{}", ignore); }
    } catch (Throwable e) { e.printStackTrace(); }
  }

  public static <T> T httpClient(Class<T> cls) throws Exception { T ret = null; return cast(httpClient(), ret); }
  public static Object httpClient() throws Exception {
    if (HttpClient == null) { throw new RuntimeException("package not found (org.apache.http.client)"); }
    Object builder;
    builder = HCBCreate.invoke(null, EMPTY_OBJ);
    builder = HCBSetSSLContext.invoke(builder, arr(sslContext));
    builder = HCBSetRedirectStrategy.invoke(builder, arr(newinstance(LaxRedirectStrategy)));
    builder = HCBSetDefaultCookieStore.invoke(builder, arr(newinstance(BasicCookieStore)));
    builder = HCBSetKeepAliveStrategy.invoke(builder, arr(connectionKeepAliveStrategy));
    builder = HCBSetConnectionManager.invoke(builder, arr(connectionManager));
    return HCBBuild.invoke(builder, EMPTY_OBJ);
  }

  public static <T> T httpHost(String addr, Class<T> cls) throws Exception { T ret = null; return cast(httpHost(addr), ret); }
  public static Object httpHost(String addr) throws Exception {
    Object ret = null;
    try {
      URL url = new URL(addr);
      String host = url.getHost();
      String prot = url.getProtocol();
      Integer port = url.getPort();
      if (port == null || port == -1) {
        switch (prot) {
        case S_HTTPS: port = 443; break;
        default: port = 80; break;
        }
      }
      ret = HttpHostConstr.newInstance(host, port, prot);
    } catch (MalformedURLException e) {
      log.debug("ERROR:{}", e);
    }
    return ret;
  }

  public static <T> T execute(Object client, Object target, Object request, Object proxy, Class<T> cls) throws Exception {
    T ret = null;
    Object obj = null;
    // if (EXTERN_PROXY_IP != null && !"".equals(EXTERN_PROXY_IP)) {
    //   HttpHost proxy = new HttpHost(EXTERN_PROXY_IP, EXTERN_PROXY_PORT, EXTERN_PROXY_PROTO);
    //   RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
    //   request.setConfig(config);
    // }
    if (target != null) {
      // return client.execute(target, request);
      obj = HttpClientExecute2.invoke(client, arr(target, request));
    } else {
      // return client.execute(request);
      obj = HttpClientExecute1.invoke(client, arr(request));
    }
    return cast(obj, ret);
  }

  public static <T> T nameValueEntity(String[][] arg, String enc, Class<T> cls) throws Exception { T ret = null; return cast(nameValueEntity(arg, enc), ret); }
  public static Object nameValueEntity(String[][] arg, String enc) throws Exception {
    Object entity = null;
    List<Object> list = new LinkedList<>();
    for (String[] kv : arg) {
      if (kv.length >= 2 && kv[0] != null && kv[1] != null) {
        list.add(BasicNameValuePairConstr.newInstance(kv[0], kv[1]));
      }
    }
    entity = UrlEncodedFormEntityConstr.newInstance(list, enc);
    return entity;
  }

  public static <T> T nameValueEntity(Map<String, Object> map, String enc, Class<T> cls) throws Exception { T ret = null; return cast(nameValueEntity(map, enc), ret); }
  public static Object nameValueEntity(Map<String, Object> map, String enc) throws Exception {
    Object entity = null;
    List<Object> list = new LinkedList<>();
    for (String key : map.keySet()) {
      list.add(BasicNameValuePairConstr.newInstance(key, String.valueOf(map.get(key))));
    }
    entity = UrlEncodedFormEntityConstr.newInstance(list, enc);
    return entity;
  }

  public static <T> T stringEntity(Object param, String type, String enc, Class<T> cls) throws Exception { T ret = null; return cast(stringEntity(param, type, enc), ret); }
  public static Object stringEntity(Object param, String type, String enc) throws Exception {
    Object entity = StringEntityConstr.newInstance(String.valueOf(param), enc);
    SESetContentType.invoke(entity, cat(type, ";charset=", enc));
    return entity;
  }

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

  public static void copyHeaders(Object requestf, Object requestt) throws Exception {
    Enumeration<String> names = null;
    names = cast(RequestGetHeaderNames.invoke(requestf, EMPTY_OBJ), names);
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      Object value = RequestGetHeader.invoke(requestf, name);
      RequestBaseAddHeader.invoke(requestt, name, value);
    }
  }

  public static <T> T[] headers(Object request, Class<T> cls) throws Exception {
    T[] ret = null;
    List<Object> list = new LinkedList<>();
    Enumeration<String> names = null;
    names = cast(RequestGetHeaderNames.invoke(request, EMPTY_OBJ), names);
    while (names.hasMoreElements()) {
      String name = names.nextElement();
      Object value = RequestGetHeader.invoke(request, name);
      list.add(BasicHeaderConstr.newInstance(arr(name, value)));
    }
    log.debug("HEADERS:{}", list);
    return cast(list.toArray(new Object[list.size()]), ret);
  }

  public static <T> T[] headers(String[][] arg) throws Exception {
    T[] ret = null;
    List<Object> list = new LinkedList<>();
    for (String[] item : arg) {
      if (item != null && item.length >= 2) {
        list.add(BasicHeaderConstr.newInstance(item[0], item[1]));
      }
    }
    log.debug("HEADERS:{}", list);
    return cast(list.toArray(new Object[list.size()]), ret);
  }

  public static BufferedReader respContentReader(Object response, String enc) throws Exception {
    BufferedReader ret = null;
    try {
      Object entity = ResponseGetEntity.invoke(response, EMPTY_OBJ);
      if (entity != null) {
        ret = reader(cast(HttpEntityGetContent.invoke(entity, EMPTY_OBJ), InputStream.class), enc);
      }
    } catch (Exception e) { log.trace("E:", e); }
    return ret;
  }

  public static String respContentStr(Object response) throws Exception { return respContentStr(response, UTF8); }
  public static String respContentStr(Object response, String enc) throws Exception {
    StringBuilder ret = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = respContentReader(response, enc);
      for (String rl; (rl = reader.readLine()) != null;) {
        ret.append(rl).append("\n");
      }
    } finally {
      safeclose(reader);
    }
    return String.valueOf(ret);
  }

  public static String reqContentStr(Object request) throws Exception { return reqContentStr(request, UTF8); }

  public static String reqContentStr(Object request, String enc) throws Exception {
    StringBuilder ret = new StringBuilder();
    BufferedReader reader = null;
    InputStream istream = null;
    try {
      istream = cast(RequestGetInputStream.invoke(request, EMPTY_OBJ), istream = null);
      reader = reader(istream, enc);
      for (String rl; (rl = reader.readLine()) != null;) {
        ret.append(rl).append("\n");
      }
    } finally {
      safeclose(reader);
      safeclose(istream);
    }
    return ret.substring(0, ret.length() - 1);
  }

  public static Map<String, Object> param(Object request) {
    Map<String, Object> ret = new LinkedHashMap<String, Object>();
    try {
      Enumeration<String> keys = cast(RequestGetAttributeNames.invoke(request, EMPTY_OBJ), keys = null);
      while (keys.hasMoreElements()) {
        String key = keys.nextElement();
        Object val = RequestGetAttribute.invoke(request, key);
        ret.put(key, val);
      }
    } catch (Exception e) { log.trace("E:", e); }
    return ret;
  }

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
        } catch (Exception ignore) { log.trace("E:{}", ignore); }
        ret.put(key, val);
      }
    }
    return ret;
  }
}