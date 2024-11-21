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
import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.newMap;
import static com.ntiple.commons.IOUtils.reader;
import static com.ntiple.commons.IOUtils.safeclose;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.ReflectionUtil.EMPTY_CLS;
import static com.ntiple.commons.ReflectionUtil.EMPTY_OBJ;
import static com.ntiple.commons.ReflectionUtil.findClass;
import static com.ntiple.commons.ReflectionUtil.findConstructor;
import static com.ntiple.commons.ReflectionUtil.findFieldValue;
import static com.ntiple.commons.ReflectionUtil.findMethod;
import static com.ntiple.commons.ReflectionUtil.newInstance;
import static com.ntiple.commons.StringUtil.cat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import com.ntiple.commons.FunctionUtil.Fn1a;
import com.ntiple.commons.FunctionUtil.Fn4a;

public class HttpUtil {
  private static final SimpleLogger log = SimpleLogger.getLogger();

  public int defaultConnectionTimeout = 1000;

  public static final Pattern PTN_PARAM = Pattern.compile("^([^=]+)[=](.*)$");

  private static Class<?> HttpServletRequest = null;
  private static Class<?> HttpClient = null;
  private static Class<?> HttpHost = null;
  private static Constructor<?> HttpGetConstr = null;
  private static Constructor<?> HttpPostConstr = null;
  private static Constructor<?> HttpDeleteConstr = null;
  private static Constructor<?> HttpHeadConstr = null;
  private static Constructor<?> HttpOptionsConstr = null;
  private static Constructor<?> HttpPatchConstr = null;
  private static Constructor<?> HttpPutConstr = null;
  private static Constructor<?> HttpTraceConstr = null;
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
  private static Method HttpMessageSetHeader = null;
  private static Method HttpRequestSetEntity = null;

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

  private static Class<?> JHttpClient = null;
  private static Class<?> JHttpClientBuilder = null;
  private static Class<?> JHttpRedirect = null;
  private static Class<?> JHttpRequest = null;
  private static Class<?> JHttpResponse = null;
  private static Class<?> JHttpHeaders = null;
  private static Class<?> JHttpVer;
  private static Method JHttpClientSend = null;
  private static Method JHttpResponseInputStream = null;
  static {
    int logLevel = log.getLevel();
    log.setLevel(4);
    if (HttpServletRequest == null) {
    /** for javax.servlet package (JDK 1.8 ver)  */
      try {
        HttpServletRequest = findClass("javax.servlet.http.HttpServletRequest");
      } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    }
    /** for jakarta package (over JDK 1.8 ver)  */
    if (HttpServletRequest == null) {
      try {
        HttpServletRequest = findClass("jakarta.servlet.http.HttpServletRequest");
      } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    }
    try {
      HttpHost = findClass("org.apache.http.HttpHost");
      HttpHostConstr = findConstructor(HttpHost, array(String.class, int.class, String.class));
      HttpClient = findClass("org.apache.http.client.HttpClient");
      Class<?> HttpClientBuilder = findClass("org.apache.http.impl.client.HttpClientBuilder");
      Class<?> HttpGet = findClass("org.apache.http.client.methods.HttpGet");
      HttpGetConstr = findConstructor(HttpGet, array(String.class));
      Class<?> HttpPost = findClass("org.apache.http.client.methods.HttpPost");
      HttpPostConstr = findConstructor(HttpPost, array(String.class));
      Class<?> HttpDelete = findClass("org.apache.http.client.methods.HttpDelete");
      HttpDeleteConstr = findConstructor(HttpDelete, array(String.class));
      Class<?> HttpHead = findClass("org.apache.http.client.methods.HttpHead");
      HttpHeadConstr = findConstructor(HttpHead, array(String.class));
      Class<?> HttpOptions = findClass("org.apache.http.client.methods.HttpOptions");
      HttpOptionsConstr = findConstructor(HttpOptions, array(String.class));
      Class<?> HttpPatch = findClass("org.apache.http.client.methods.HttpPatch");
      HttpPatchConstr = findConstructor(HttpPatch, array(String.class));
      Class<?> HttpPut = findClass("org.apache.http.client.methods.HttpPut");
      HttpPutConstr = findConstructor(HttpPut, array(String.class));
      Class<?> HttpTrace = findClass("org.apache.http.client.methods.HttpTrace");
      HttpTraceConstr = findConstructor(HttpTrace, array(String.class));

      LaxRedirectStrategy = findClass("org.apache.http.impl.client.LaxRedirectStrategy");
      BasicCookieStore = findClass("org.apache.http.impl.client.BasicCookieStore");
      Class<?> RedirectStrategy = findClass("org.apache.http.client.RedirectStrategy");
      Class<?> CookieStore = findClass("org.apache.http.client.CookieStore");
      Class<?> ConnectionKeepAliveStrategy = findClass("org.apache.http.conn.ConnectionKeepAliveStrategy");
      Class<?> Registry = findClass("org.apache.http.config.Registry");
      Class<?> HttpClientConnectionManager = findClass("org.apache.http.conn.HttpClientConnectionManager");
      Class<?> SSLContextBuilder = findClass("org.apache.http.ssl.SSLContextBuilder");
      Class<?> TrustStrategy = findClass("org.apache.http.ssl.TrustStrategy");
      Class<?> RegistryBuilder = findClass("org.apache.http.config.RegistryBuilder");

      Class<?> HttpUriRequest = findClass("org.apache.http.client.methods.HttpUriRequest");
      Class<?> HttpRequest = findClass("org.apache.http.HttpRequest");
      HttpClientExecute1 = findMethod(HttpClient, "execute", array(HttpUriRequest));
      HttpClientExecute2 = findMethod(HttpClient, "execute", array(HttpHost, HttpRequest));
      {
        if (HttpServletRequest != null) {
          RequestGetHeaderNames = findMethod(HttpServletRequest, "getHeaderNames", array());
          RequestGetHeader = findMethod(HttpServletRequest, "getHeader", array(String.class));
          RequestGetInputStream = findMethod(HttpServletRequest, "getInputStream", EMPTY_CLS);
          RequestGetAttributeNames = findMethod(HttpServletRequest, "getAttributeNames", EMPTY_CLS);
          RequestGetAttribute = findMethod(HttpServletRequest, "getAttribute", array(String.class));
        }
        Class<?> HttpRequestBase = findClass("org.apache.http.client.methods.HttpRequestBase");
        RequestBaseAddHeader = findMethod(HttpRequestBase, "addHeader", array(String.class, String.class));
      }
      {
        Class<?> HttpEntity = findClass("org.apache.http.HttpEntity");
        Class<?> HttpResponse = findClass("org.apache.http.HttpResponse");
        ResponseGetEntity = findMethod(HttpResponse, "getEntity", EMPTY_CLS);
        HttpEntityGetContent = findMethod(HttpEntity, "getContent", EMPTY_CLS);
        Class<?> HttpEntityRequestBase = findClass("org.apache.http.client.methods.HttpEntityEnclosingRequestBase");
        HttpRequestSetEntity = findMethod(HttpEntityRequestBase, "setEntity", array(HttpEntity));
      }
      {
        Class<?> BasicHeader = findClass("org.apache.http.message.BasicHeader");
        BasicHeaderConstr = findConstructor(BasicHeader, array(String.class, String.class));
      }
      {
        Class<?> HttpMessage = findClass("org.apache.http.HttpMessage");
        HttpMessageSetHeader = findMethod(HttpMessage, "setHeader", array(String.class, String.class));
      }
      UrlEncodedFormEntity = findClass("org.apache.http.client.entity.UrlEncodedFormEntity");
      UrlEncodedFormEntityConstr = findConstructor(UrlEncodedFormEntity, array(List.class, String.class));
      BasicNameValuePair = findClass("org.apache.http.message.BasicNameValuePair");
      BasicNameValuePairConstr = findConstructor(BasicNameValuePair, array(String.class, String.class));

      StringEntity = findClass("org.apache.http.entity.StringEntity");
      StringEntityConstr = findConstructor(StringEntity, array(String.class, String.class));
      SESetContentType = findMethod(StringEntity, "setContentType", array(String.class));

      PoolingHttpClientConnectionManager = findClass("org.apache.http.impl.conn.PoolingHttpClientConnectionManager");
      HCBCreate = findMethod(HttpClientBuilder, "create");
      HCBBuild = findMethod(HttpClientBuilder, "build");
      HCBSetSSLContext = findMethod(HttpClientBuilder, "setSSLContext", SSLContext.class);
      HCBSetRedirectStrategy = findMethod(HttpClientBuilder, "setRedirectStrategy", RedirectStrategy);
      HCBSetDefaultCookieStore = findMethod(HttpClientBuilder, "setDefaultCookieStore", CookieStore);
      HCBSetKeepAliveStrategy = findMethod(HttpClientBuilder, "setKeepAliveStrategy", ConnectionKeepAliveStrategy);
      HCBSetConnectionManager = findMethod(HttpClientBuilder, "setConnectionManager", HttpClientConnectionManager);
      {
        Method sslLoadTrustMaterial = findMethod(SSLContextBuilder, "loadTrustMaterial", KeyStore.class, TrustStrategy);
        Method sslBuild = findMethod(SSLContextBuilder, "build", EMPTY_CLS);
        Object sslContextBuilder = newInstance(SSLContextBuilder);
        Object trustStrategy = java.lang.reflect.Proxy.newProxyInstance(TrustStrategy.getClassLoader(),
          new Class<?>[] { TrustStrategy }, new InvocationHandler() {
          @Override public Object invoke(Object p, Method m, Object[] a) throws Throwable {
            if ("isTrusted".equals(m.getName()) && a.length == 2) { return true; }
            return null;
            }
        });
        sslContextBuilder = sslLoadTrustMaterial.invoke(sslContextBuilder, array(null, trustStrategy));
        sslContext = sslBuild.invoke(sslContextBuilder, EMPTY_OBJ);
      }
      {
        connectionKeepAliveStrategy = java.lang.reflect.Proxy.newProxyInstance(ConnectionKeepAliveStrategy.getClassLoader(),
          new Class<?>[] { ConnectionKeepAliveStrategy }, new InvocationHandler() {
            @Override public Object invoke(Object p, Method m, Object[] a) throws Throwable {
              /** 5 seconds */
              if ("getKeepAliveDuration".equals(m.getName()) && a.length == 2) { return Long.valueOf(5 * 1000); }
              return null;
            }
          });
      }
      {
        Class<?> PlainConnectionSocketFactory = findClass("org.apache.http.conn.socket.PlainConnectionSocketFactory");
        Class<?> SSLConnectionSocketFactory = findClass("org.apache.http.conn.ssl.SSLConnectionSocketFactory");
        Class<?> NoopHostnameVerifier = findClass("org.apache.http.conn.ssl.NoopHostnameVerifier");
        Class<?> HostnameVerifier = findClass("javax.net.ssl.HostnameVerifier");
        Method regCreate = findMethod(RegistryBuilder, "create", EMPTY_CLS);
        Method regRegister = findMethod(RegistryBuilder, "register", array(String.class, Object.class));
        Method sslBuild = findMethod(RegistryBuilder, "build");
        Constructor<?> sslfactory = findConstructor(SSLConnectionSocketFactory, array(SSLContext.class, HostnameVerifier));
        Object reg = null;
        Object verifier = findFieldValue(NoopHostnameVerifier, "INSTANCE");
        Object sslcon = sslfactory.newInstance(array(sslContext, verifier));

        reg = regCreate.invoke(null, EMPTY_OBJ);
        reg = regRegister.invoke(reg, array(S_HTTP, findFieldValue(PlainConnectionSocketFactory, "INSTANCE")));
        reg = regRegister.invoke(reg, array(S_HTTPS, sslcon));
        connectionManager = findConstructor(PoolingHttpClientConnectionManager, array(Registry)).newInstance(sslBuild.invoke(reg));
      }
    } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    try {
      Class<?> JHttpBodyHandler = findClass("java.net.http.HttpResponse$BodyHandler");
      Class<?> JHttpBodyHandlers = findClass("java.net.http.HttpResponse$BodyHandlers");
      JHttpClient = findClass("java.net.http.HttpClient");
      JHttpClientBuilder = findClass("java.net.http.HttpClient$Builder");
      JHttpRedirect = findClass("java.net.http.HttpClient$Redirect");
      JHttpVer = findClass("java.net.http.HttpClient$Version");
      JHttpRequest = findClass("java.net.http.HttpRequest");
      JHttpResponse = findClass("java.net.http.HttpResponse");
      JHttpHeaders = findClass("java.net.http.HttpHeaders");
      JHttpClientSend = findMethod(JHttpClient, "send", array(JHttpRequest, JHttpBodyHandler));
      JHttpResponseInputStream = findMethod(JHttpBodyHandlers, "ofInputStream", EMPTY_CLS);
      System.setProperty("jdk.httpclient.allowRestrictedHeaders", "connection,content-length,host,upgrade");
    } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    log.setLevel(logLevel);
  }

  public static <T> T httpClient(Class<T> cls) throws Exception { T ret = null; return cast(httpClient(), ret); }
  public static Object httpClient() throws Exception {
    if (HttpClient == null) { throw new RuntimeException("package not found (org.apache.http.client)"); }
    Object builder;
    builder = HCBCreate.invoke(null, EMPTY_OBJ);
    builder = HCBSetSSLContext.invoke(builder, array(sslContext));
    builder = HCBSetRedirectStrategy.invoke(builder, array(newInstance(LaxRedirectStrategy)));
    builder = HCBSetDefaultCookieStore.invoke(builder, array(newInstance(BasicCookieStore)));
    builder = HCBSetKeepAliveStrategy.invoke(builder, array(connectionKeepAliveStrategy));
    builder = HCBSetConnectionManager.invoke(builder, array(connectionManager));
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
      obj = HttpClientExecute2.invoke(client, array(target, request));
    } else {
      // return client.execute(request);
      obj = HttpClientExecute1.invoke(client, array(request));
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
      list.add(BasicHeaderConstr.newInstance(array(name, value)));
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
    } catch (Exception e) { log.debug("E:", e); }
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
    } catch (Exception e) { log.debug("E:", e); }
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
        } catch (Exception ignore) { log.debug("E:{}", ignore); }
        ret.put(key, val);
      }
    }
    return ret;
  }

  private static Object jClient(Map<String, Object> context, String follow, String ver, String paddr, int pport) {
    Object ret = null;
    Object builder = null;
    Method newBuilder = null;
    Method mBuild = null;
    Method mVersion = null;
    Method mProxy = null;
    Method mFollowRedirects = null;
    Method mCookieHandler = null;
    Method mSslContext = null;
    if (JHttpClient != null) {
      try {
        newBuilder = findMethod(JHttpClient, "newBuilder", EMPTY_CLS);
        builder = newBuilder.invoke(null, EMPTY_OBJ);
        mBuild = findMethod(JHttpClientBuilder, "build", EMPTY_CLS);
        mVersion = findMethod(JHttpClientBuilder, "version", array(JHttpVer));
        mProxy = findMethod(JHttpClientBuilder, "proxy", array(ProxySelector.class));
        mFollowRedirects = findMethod(JHttpClientBuilder, "followRedirects", array(JHttpRedirect));
        mCookieHandler = findMethod(JHttpClientBuilder, "cookieHandler", array(CookieHandler.class));
        mSslContext = findMethod(JHttpClientBuilder, "sslContext", array(SSLContext.class));
        {
          Object v = null;
          if (ver == null) { ver = "1.1"; }
          switch (ver) {
          case "2": {
            v = findFieldValue(JHttpVer, "HTTP_2");
          } break;
          default: {
            v = findFieldValue(JHttpVer, "HTTP_1_1");
          } }
          mVersion.invoke(builder, v);
        }
        if (paddr != null && pport != -1) {
          StaticProxySelector proxySelector = cast(context.get(StaticProxySelector.class.getName()), proxySelector = null);
          if (proxySelector == null) {
            proxySelector = new StaticProxySelector(new InetSocketAddress(paddr, pport));
          }
          mProxy.invoke(builder, proxySelector);
        }
        {
          String v = "";
          switch (follow) {
          case "NORMAL": break;
          case "NEVER": break;
          case "ALWAYS": 
          default: v = "ALWAYS";
          }
          mFollowRedirects.invoke(builder, findFieldValue(JHttpRedirect, v));
        }
        {
          CookieHandler ckhnd = cast(context.get(CookieHandler.class.getName()), ckhnd = null);
          if (ckhnd == null) {
            CookieManager ckmng = new CookieManager();
            ckmng.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(ckmng);
            ckhnd = CookieHandler.getDefault();
            mCookieHandler.invoke(builder, ckhnd);
            context.put(CookieHandler.class.getName(), ckhnd);
          }
        }
        {
          SSLContext sslContext = cast(context.get(SSLContext.class.getName()), sslContext = null);
          if (sslContext == null) {
            X509ExtendedTrustManager trustManager = new X509ExtendedTrustManager() {
              @Override public void checkClientTrusted(X509Certificate[] x, String a) throws CertificateException { }
              @Override public void checkServerTrusted(X509Certificate[] x, String a) throws CertificateException { }
              @Override public void checkClientTrusted(X509Certificate[] x, String a, Socket s) throws CertificateException { }
              @Override public void checkClientTrusted(X509Certificate[] x, String a, SSLEngine e) throws CertificateException { }
              @Override public void checkServerTrusted(X509Certificate[] x, String a, Socket s) throws CertificateException { }
              @Override public void checkServerTrusted(X509Certificate[] x, String a, SSLEngine e) throws CertificateException { }
              @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[] {}; }
            };
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { trustManager }, new SecureRandom());
          }
          mSslContext.invoke(builder, sslContext);
        }
        ret = mBuild.invoke(builder, EMPTY_OBJ);
        // System.out.println("================================================================================");
        // System.out.println(String.format("CHECK: %s", ret));
        // System.out.println("================================================================================");
      } catch (Exception ignore) { log.debug("E:{}", ignore); }
    }
    return ret;
  }

  private static Object jRequest(Map<String, Object> client, String ustr, String[][] headers) {
    Object ret = null;
    try {
      Object builder = null;
      URI uri = URI.create(ustr);
      Class<?> JHttpRequestBuilder = findClass("java.net.http.HttpRequest$Builder");
      Method jHttpRequestBuilder = findMethod(JHttpRequest, "newBuilder", EMPTY_CLS);
      Method jRequestUri = findMethod(JHttpRequestBuilder, "uri", array(URI.class));
      Method build = findMethod(JHttpRequestBuilder, "build", EMPTY_CLS);
      builder = jHttpRequestBuilder.invoke(null, EMPTY_OBJ);
      jRequestUri.invoke(builder, uri);
      ret = build.invoke(builder, EMPTY_OBJ);
    } catch (Exception ignore) { log.debug("E:{}", ignore); }
    return ret;
  }

  public static HttpClientWorker httpWorker(String url) {
    HttpClientWorker worker = new HttpClientWorker(url);
    return worker;
  }

  public enum HttpClientProviders {
    APACHE_CLIENT_4_5,
    JDK_11,
    URL_CONNECT
  }

  public enum HttpMethod {
    DELETE,
    GET,
    HEAD,
    OPTIONS,
    PATCH,
    POST,
    PUT,
    TRACE
  }

  public static class HttpClientWorker {
    private String provider;
    private String protocol;
    private String agent;
    private String host;
    private int port;
    private String path;
    private String query;
    private String version;
    private String method;
    private String proxyaddr;
    private int proxyport;
    private Map<String, Object> headers;
    private Object contents;
    private Map<String, Object> context;

    public HttpClientWorker() { this(null, new LinkedHashMap<>()); }
    public HttpClientWorker(String ustr) { this(ustr, new LinkedHashMap<>()); }
    public HttpClientWorker(String ustr, Map<String, Object> context) {
      if (ustr != null && !"".equals(ustr)) {
        try {
          URL url = null;
          url = new URL(ustr);
          /** http, https */
          this.protocol = url.getProtocol();
          /** domain.com */
          this.host = url.getHost();
          /** -1, 80, 443 */
          this.port = url.getPort();
          /** /uri */
          this.path = url.getPath();
          /** name=value&name2=value2 */
          this.query = url.getQuery();
        } catch (MalformedURLException ignore) { }
      }
      this.agent = "HttpClient";
      if (context == null) { context = new LinkedHashMap<>(); }
      this.context = context; 
    }

    public HttpClientWorker provider(Fn1a<HttpClientProviders, HttpClientProviders> calable) {
      HttpClientProviders v = calable.apply(HttpClientProviders.APACHE_CLIENT_4_5);
      String provider = null;
      if (v != null) { provider = v.name(); }
      this.provider = provider;
      return this;
    }

    public HttpClientWorker proxy(String proxy) {
      return this;
    }

    public HttpClientWorker ipAddr(String ipAddr) {
      return this;
    }

    public HttpClientWorker agent(String agent) {
      this.agent = agent;
      return this;
    }

    public HttpClientWorker version(String version) {
      this.version = version;
      return this;
    }

    public HttpClientWorker method(Fn1a<HttpMethod, HttpMethod> calable) {
      HttpMethod v = calable.apply(HttpMethod.GET);
      String method = null;
      if (v != null) { method = v.name(); }
      this.method = method;
      return this;
    }

    public HttpClientWorker headers(Map<String, Object> headers) {
      this.headers = headers;
      return this;
    }

    public HttpClientWorker contents(Object contents) {
      this.contents = contents;
      return this;
    }

    public HttpClientWorker context(Map<String, Object> context) {
      this.context = context;
      return this;
    }
    public Map<String, Object> context() {
      return context;
    }

    public Object work(Fn4a<Integer, InputStream, Map<String, List<String>>, Map<String, Object>, Object> callable) {
      Object ret = null;
      InputStream istream = null;
      Integer state = -1;
      Map<String, List<String>> headerMap = null;
      try {
        StringBuilder urlStr = new StringBuilder();
        urlStr.append(this.protocol)
          .append("://")
          .append(this.host)
          .append(this.port > 0 ? cat(":", this.port) : "")
          .append(this.path != null && !"".equals(this.path) ? this.path : "")
          .append(this.query != null && !"".equals(this.query) ? cat("?", this.query) : "")
          ;
        log.debug("URL:{}", urlStr);
        boolean hasbody = false;
        Constructor<?> constr = null;
        if (this.provider == null || "".equals(this.provider)) { this.provider = HttpClientProviders.APACHE_CLIENT_4_5.name(); }
        if (this.method == null || "".equals(this.method)) { this.method = HttpMethod.GET.name(); }
        SW1: switch (HttpClientProviders.valueOf(this.provider)) {
        case JDK_11: {
          Object client = jClient(this.context, "ALWAYS", null, null, -1);
          Object request = jRequest(this.context, String.valueOf(urlStr), null);
          Object handler = JHttpResponseInputStream.invoke(null, EMPTY_OBJ);
          Method getStatusCode = findMethod(JHttpResponse, "statusCode", EMPTY_CLS);
          Method getBody = findMethod(JHttpResponse, "body", EMPTY_CLS);
          Method getHeaders = findMethod(JHttpResponse, "headers", EMPTY_CLS);
          Method getMap = findMethod(JHttpHeaders, "map", EMPTY_CLS);
          // log.debug("REQUEST:{}", request);
          // log.debug("HANDLER:{}", handler);
          // log.debug("SEND:{}", JHttpClientSend);
          Object result = JHttpClientSend.invoke(client, array(request, handler));
          // log.debug("RESULT:{}", result);
          state = cast(getStatusCode.invoke(result, EMPTY_OBJ), state);
          istream = cast(getBody.invoke(result, EMPTY_OBJ), istream);
          {
            Object obj = getHeaders.invoke(result, EMPTY_OBJ);
            if (obj != null) { obj = getMap.invoke(obj, EMPTY_OBJ); }
            if (obj != null) { headerMap = cast(obj, headerMap); }
          }
          if (headerMap == null) { headerMap = cast(newMap(), headerMap = null); }
          ret = cast(callable.apply(state, istream, headerMap, this.context), ret);
          // log.debug("BODY:{}", ret);
        } break SW1;
        case APACHE_CLIENT_4_5: 
        default: {
          Object request = null;
          SW2: switch (HttpMethod.valueOf(this.method)) {
          case POST:    { constr = HttpPostConstr;    hasbody = true;  } break SW2;
          case DELETE:  { constr = HttpDeleteConstr;  hasbody = false; } break SW2;
          case PUT:     { constr = HttpPutConstr;     hasbody = true;  } break SW2;
          case HEAD:    { constr = HttpHeadConstr;    hasbody = false; } break SW2;
          case OPTIONS: { constr = HttpOptionsConstr; hasbody = false; } break SW2;
          case PATCH:   { constr = HttpPatchConstr;   hasbody = true;  } break SW2;
          case TRACE:   { constr = HttpTraceConstr;   hasbody = false; } break SW2;
          case GET:
          default:      { constr = HttpGetConstr;     hasbody = false; } break SW2;
          }
          String ctype = "";
          String chset = UTF8;
          if (constr != null) {
            request = constr.newInstance(String.valueOf(urlStr));
          }
          final Pattern PTN_CHARSET = Pattern.compile("[ ]*charset[ ]*=[ ]*(?<chset>[a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);
          Matcher mat;
          LOOP: for (String name : this.headers.keySet()) {
            if (name == null || "".equals(name)) { continue LOOP; }
            String value = cast(this.headers.get(name), "");
            HttpMessageSetHeader.invoke(request, name, value);
            String key = name.toLowerCase().trim();
            if ("content-type".equals(key) && value != null && !"".equals(value)) {
              String[] data = value.split(";");
              ctype = data[0].toLowerCase().trim();
              if (data.length > 0 && (mat = PTN_CHARSET.matcher(data[1])) != null && mat.find()) {
                chset = mat.group("chset");
              }
            }
          }
          log.debug("CHECK-TYPE:{} / {}", ctype, chset);
          if (hasbody) {
            Object entity = null;
            SW3: switch (ctype) {
            case "multipart/form-data": {
              /** TODO: multipart 구현 필요 */
            } break SW3;
            case "text/plain": {
              entity = StringEntityConstr.newInstance(String.valueOf(this.contents != null ? this.contents : ""), chset);
            } break SW3;
            case "application/json": {
              entity = StringEntityConstr.newInstance(convert(this.contents, ""), chset);
            } break SW3;
            case "application/x-www-form-urlencoded":
            default: {
              Map<String, Object> map = convert(this.contents, newMap());
              List<Object> list = new ArrayList<>();
              for (String key : map.keySet()) {
                Object val = map.get(key);
                list.add(BasicNameValuePairConstr.newInstance(key, String.valueOf(val != null ? val : "")));
              }
              entity = UrlEncodedFormEntityConstr.newInstance(array(list, chset));
            } break SW3; }
            HttpRequestSetEntity.invoke(request, entity);
          }
          Object client = context.get(HttpClient.getName());
          if (client == null) { context.put(HttpClient.getName(), client = httpClient()); }
          Object result = execute(client, null, request, null, Object.class);
          Object entity = ResponseGetEntity.invoke(result, EMPTY_OBJ);
          istream = cast(HttpEntityGetContent.invoke(entity, EMPTY_OBJ), InputStream.class);
          if (headerMap == null) { headerMap = cast(newMap(), headerMap = null); }
          ret = cast(callable.apply(state, istream, headerMap, this.context), ret);
        } break SW1; }
      } catch (Exception e) {
        log.debug("E:{}", e);
      } finally {
        safeclose(istream);
      }
      return ret;
    }
  }

  private static class StaticProxySelector extends ProxySelector {
    private static final List<java.net.Proxy> NO_PROXY_LIST = Arrays.asList(array(java.net.Proxy.NO_PROXY));
    final List<java.net.Proxy> list;
    StaticProxySelector(InetSocketAddress address) {
      java.net.Proxy p;
      if (address == null) {
        p = java.net.Proxy.NO_PROXY;
      } else {
        p = new java.net.Proxy(java.net.Proxy.Type.HTTP, address);
      }
      list = Arrays.asList(array(p));
    }

    @Override public void connectFailed(URI uri, SocketAddress sa, IOException e) { }
    @Override public synchronized List<java.net.Proxy> select(URI uri) {
      String scheme = uri.getScheme().toLowerCase();
      if (scheme.equals("http") || scheme.equals("https")) {
        return list;
      } else {
        return NO_PROXY_LIST;
      }
    }
  }
}