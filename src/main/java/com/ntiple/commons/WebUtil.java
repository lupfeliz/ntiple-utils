/**
 * @File        : WebUtil.java
 * @Author      : 정재백
 * @Since       : 2023-11-22
 * @Description : 웹유틸
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.REFERER;
import static com.ntiple.commons.Constants.X_FORWARDED_FOR;
import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.ConvertUtil.asList;
import static com.ntiple.commons.ConvertUtil.convert;
import static com.ntiple.commons.ConvertUtil.parseInt;
import static com.ntiple.commons.ReflectionUtil.EMPTY_CLS;
import static com.ntiple.commons.ReflectionUtil.EMPTY_OBJ;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.ReflectionUtil.findClass;
import static com.ntiple.commons.ReflectionUtil.findMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WebUtil {

  private static final SimpleLogger log = SimpleLogger.getLogger();
  public static final String PTN_SCHEM_HTTP = "^http[s]{0,1}[:][/][/]";

  private static Class<?> ServletRequest = null;
  private static Class<?> HttpServletRequest = null;
  private static Class<?> HttpServletResponse = null;
  private static Class<?> RequestContextHolder = null;
  private static Class<?> ServletRequestAttributes = null;

  private static Method ServletRequestGetAttribute = null;
  private static Method HttpServletRequestGetHeader = null;
  private static Method HttpServletRequestGetRemoteAddr = null;
  private static Method HttpServletRequestGetParameterMap = null;

  private static Method RequestContextHolderGetRequestAttributes = null;
  private static Method ServletRequestAttributesGetRequest = null;


  static {
    int logLevel = log.getLevel();
    log.setLevel(4);
    if (HttpServletRequest == null) {
    /** for javax.servlet package (JDK 1.8 ver)  */
      try {
        ServletRequest = findClass("javax.servlet.ServletRequest");
        HttpServletRequest = findClass("javax.servlet.http.HttpServletRequest");
        HttpServletResponse = findClass("javax.servlet.http.HttpServletResponse");
        ServletRequestGetAttribute = findMethod(ServletRequest, "getAttribute", array(String.class));
        HttpServletRequestGetHeader = findMethod(HttpServletRequest, "getHeader", array(String.class));
        HttpServletRequestGetRemoteAddr = findMethod(HttpServletRequest, "getRemoteAddr", EMPTY_CLS);
        HttpServletRequestGetParameterMap = findMethod(HttpServletRequest, "getParameterMap", EMPTY_CLS);
        RequestContextHolder = findClass("org.springframework.web.context.request.RequestContextHolder");
        ServletRequestAttributes = findClass("org.springframework.web.context.request.ServletRequestAttributes");
      } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    }
    /** for jakarta package (over JDK 1.8 ver)  */
    if (HttpServletRequest == null) {
      try {
        ServletRequest = findClass("jakarta.servlet.ServletRequest");
        HttpServletRequest = findClass("jakarta.servlet.http.HttpServletRequest");
        HttpServletResponse = findClass("jakarta.servlet.http.HttpServletResponse");
        ServletRequestGetAttribute = findMethod(ServletRequest, "getAttribute", array(String.class));
        HttpServletRequestGetHeader = findMethod(HttpServletRequest, "getHeader", array(String.class));
        HttpServletRequestGetRemoteAddr = findMethod(HttpServletRequest, "getRemoteAddr", EMPTY_CLS);
        HttpServletRequestGetParameterMap = findMethod(HttpServletRequest, "getParameterMap", EMPTY_CLS);
        RequestContextHolder = findClass("org.springframework.web.context.request.RequestContextHolder");
        ServletRequestAttributes = findClass("org.springframework.web.context.request.ServletRequestAttributes");
      } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    }
    if (RequestContextHolder != null) {
      try {
        RequestContextHolderGetRequestAttributes = findMethod(RequestContextHolder, "getRequestAttributes", EMPTY_CLS);
        ServletRequestAttributesGetRequest = findMethod(ServletRequestAttributes, "getRequest", EMPTY_CLS);
      } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    }
    try {
    } catch (Throwable ignore) { log.debug("E:{}", ignore); }
    log.setLevel(logLevel);
  }
  
  public static <T> T curRequest(Class<T> cls) { return cast(curRequest(), cls); }
  public static <T> T curRequest(T obj) { return cast(curRequest(), obj); }
  public static Object curRequest() {
    Object ret = null;
    if (RequestContextHolder == null) { return ret; }
    try {
      Object attr = RequestContextHolderGetRequestAttributes.invoke(null, EMPTY_OBJ);
      ret = ServletRequestAttributesGetRequest.invoke(attr, EMPTY_OBJ);
    } catch (Exception ignore) { log.debug("E:{}", ignore); }
    return ret;
  }

  public static <T> T curResponse(Class<T> cls) { return curResponse(curRequest(), cls); }
  public static <T> T curResponse(Object req, Class<T> cls) { return cast(curResponse(req), cls); }
  public static <T> T curResponse(Object req, T obj) { return cast(curResponse(req), obj); }
  public static Object curResponse(Object request) {
    if (request != null && HttpServletRequest != null && HttpServletRequest != null) {
      try {
        Object obj = ServletRequestGetAttribute.invoke(request, HttpServletResponse.getName());
        if (obj != null && HttpServletResponse.isAssignableFrom(obj.getClass())) {
          return obj;
        }
      } catch (Exception ignore) { log.debug("E:{}", ignore); }
    }
    return null;
  }

  public static String remoteAddr() { return remoteAddr(curRequest()); }
  public static String remoteAddr(Object req) {
    Object ret = null;
    if (req == null || HttpServletRequestGetHeader == null) { return cast(ret, ""); }
    try {
      ret = HttpServletRequestGetHeader.invoke(req, X_FORWARDED_FOR);
      if (ret == null) { ret = HttpServletRequestGetHeader.invoke(req, "Proxy-Client-IP"); }
      if (ret == null) { ret = HttpServletRequestGetHeader.invoke(req, "WL-Proxy-Client-IP"); }
      if (ret == null) { ret = HttpServletRequestGetHeader.invoke(req, "HTTP_CLIENT_IP"); }
      if (ret == null) { ret = HttpServletRequestGetHeader.invoke(req, "HTTP_X_FORWARDED_FOR"); }
      if (ret == null) { ret = HttpServletRequestGetRemoteAddr.invoke(req, EMPTY_OBJ); }
    } catch (Exception ignore) { }
    return cast(ret, "");
  }

  public static String referer() { return referer(curRequest()); }
  public static String referer(Object req) {
    Object ret = null;
    if (req == null || HttpServletRequestGetHeader == null) { return cast(ret, ""); }
    try {
      ret = HttpServletRequestGetHeader.invoke(req, REFERER);
    } catch (Exception ignore) { }
    return cast(ret, "");
  }

  public static String getUri(String urlStr, List<String> hostNames) {
    String ret = "";
    if (urlStr == null || "".equals(urlStr)) { return ret; }
    urlStr = urlStr.trim().replaceAll(PTN_SCHEM_HTTP, "").trim();
    if (hostNames != null) {
      LOOP:
      for (String hostName : hostNames) {
        if (urlStr.startsWith(hostName)) {
          ret = urlStr.substring(hostName.length());
          break LOOP;
        }
      }
    }
    return ret;
  }

  // public static class XSSInputStream extends ServletInputStream {
  //   private InputStream delegator;
  //   private boolean finished = false;
  //   public XSSInputStream(InputStream delegator) { this.delegator = delegator; }
  //   public XSSInputStream(String str, String enc) {
  //     try {
  //       delegator = new ByteArrayInputStream(str.getBytes(enc));
  //     } catch (Exception e) {
  //       log.debug("ERROR:{}", e);
  //     }
  //   }
  //   @Override public int read() throws IOException {
  //     int ret = this.delegator.read();
  //     if (!finished && ret == -1) { this.finished = true; }
  //     return ret;
  //   }
  //   @Override public int read(byte[] b) throws IOException {
  //     int ret = delegator.read(b);
  //     if (!finished && ret == -1) { this.finished = true; }
  //     return ret;
  //   }
  //   @Override public int read(byte[] b, int off, int len) throws IOException {
  //     int ret = delegator.read(b, off, len);
  //     if (!finished && ret == -1) { this.finished = true; }
  //     return ret;
  //   }
  //   @Override public int hashCode() { return delegator.hashCode(); }
  //   @Override public boolean equals(Object obj) { return delegator.equals(obj); }
  //   @Override public long skip(long n) throws IOException { return delegator.skip(n); }
  //   @Override public String toString() { return delegator.toString(); }
  //   @Override public int available() throws IOException { return delegator.available(); }
  //   @Override public void close() throws IOException { delegator.close(); }
  //   @Override public void mark(int readlimit) { delegator.mark(readlimit); }
  //   @Override public void reset() throws IOException { delegator.reset(); }
  //   @Override public boolean markSupported() { return delegator.markSupported(); }
  //   @Override public boolean isFinished() { return finished; }
  //   @Override public boolean isReady() { return true; }
  //   @Override public void setReadListener(ReadListener listener) {
  //     log.debug("================================================================================");
  //     log.debug("UNSUPPORTED OPERATION setReadListener");
  //     log.debug("================================================================================");
  //   }
  // }
  
  // public static class XSSFilteredRequest extends HttpServletRequestWrapper {
  //   public XSSFilteredRequest(HttpServletRequest delegate) { super(delegate); }
  //   @Override public ServletInputStream getInputStream() throws IOException {
  //     InputStream istream = null;
  //     ByteArrayOutputStream bstream = null;
  //     XSSInputStream xstream = null;
  //     String str = "";
  //     try {
  //       istream = super.getInputStream();
  //       bstream = new ByteArrayOutputStream();
  //       passthrough(istream, bstream);
  //       str = new String(bstream.toByteArray());
  //       str = cleanXSS(str);
  //       log.debug("XSS-FILTERED:{}", str);
  //       xstream = new XSSInputStream(str, UTF8);
  //     } catch (Exception e) {
  //       log.debug("ERROR:{}", e);
  //     } finally {
  //       safeclose(istream);
  //       safeclose(bstream);
  //     }
  //     return xstream;
  //   }
  //   public void test() throws Exception {
  //     this.getReader();
  //   }
  // }

  public static String cleanXSS(String value) {
    String ret = value;
    log.trace("VALUE:{}", value);
    try {
      ret = ret.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
      ret = ret.replaceAll("'", "&#39;");
      ret = ret.replaceAll("eval\\((.*)\\)", "");
      ret = ret.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
      ret = ret.replaceAll("<script", "&lt;script");
      ret = ret.replaceAll("</script", "&lt;/script");
      ret = ret.replaceAll("<([^>]+)on[a-zA-Z]+[=]", "<$1");
    } catch (Exception e) {
      log.debug("E:{}", e);
    }
    return ret;
  }

  public static boolean checkIpMatch(String ipAddr, String filter) {
    boolean ret = false;
    List<String> frgdata = asList(ipAddr.split("[.]"));
    List<String> fltdata = asList(filter.split("[.]"));
    for (int inx = fltdata.size(); inx < frgdata.size(); inx++) {
      fltdata.add("*");
    }
    int inx = 0;
    LOOP:
    for (; inx < fltdata.size(); inx++) {
      String frg = String.valueOf(frgdata.get(inx)).trim();
      String flt = String.valueOf(fltdata.get(inx)).trim();
      log.trace("FRG:{} / FLT:{}", frg, flt);
      if (frg.equals(flt)) { continue LOOP; }
      if (flt.equals("*")) { continue LOOP; }
      if (flt.contains("-")) {
        int num = parseInt(frg, -1);
        if (num == -1) { break LOOP; }
        String[] tmp = flt.split("[-]");
        if (tmp.length != 2) { break LOOP; }
        int[] rng = new int[tmp.length];
        rng[0] = parseInt(tmp[0]);
        rng[1] = parseInt(tmp[1]);
        log.trace("CHECK:{} : {}~{}", num, rng[0], rng[1]);
        if (num >= rng[0] && num <= rng[1]) { continue LOOP; }
      }
      break LOOP;
    }
    if (inx >= fltdata.size()) { ret = true; }
    log.trace("CHECK:{} / {} / {}", inx, fltdata.size(), ret);
    return ret;
  }

  public static RequestParameter params() { return params(curRequest()); }
  public static RequestParameter params(Object req) { return new RequestParameter(req); }
  public static class RequestParameter {
    private Map<String, String[]> pmap;

    public RequestParameter(Object req) {
      try {
        pmap = cast(HttpServletRequestGetParameterMap.invoke(req, EMPTY_OBJ), pmap = null);
      } catch (Exception e) { throw new RuntimeException(e); }
    }

    public String get(String name) { return get(name, String.class, 0); }
    public String get(String name, Integer seq) { return get(name, String.class, seq); }
    public <T> T get(String name, Class<T> cls) { return get(name, cls, 0); }
    public <T> T get(String name, Class<T> cls, Integer seq) {
      T ret = null;
      String[] v = pmap.get(name);
      Object o = null;
      if (v == null) { return null; }
      if (seq == null) { seq = 0; }
      if (cls == String.class || cls.isAssignableFrom(String.class)) {
        if (v.length > seq && (o = v[seq]) != null) { ret = cast(o, ret); }
      } else if (cls == int.class || cls.isAssignableFrom(int.class)) {
        if (v.length > seq && (o = v[seq]) != null) { ret = cast(parseInt(o), ret); }
      } else if (cls == Integer.class || cls.isAssignableFrom(Integer.class)) {
        if (v.length > seq && (o = v[seq]) != null) { ret = cast(parseInt(o), ret); }
      } else if (cls == List.class || cls.isAssignableFrom(List.class)) {
        List<String> list = new ArrayList<>();
        for (String itm : v) { list.add(itm); }
        ret = cast(list, ret);
      } else if (cls == String[].class || cls.isAssignableFrom(String[].class)) {
        ret = cast(v, ret);
      } else if (cls == int[].class || cls.isAssignableFrom(int[].class)) {
        int[] list = new int[v.length];
        for (int inx = 0; inx < v.length; inx++) { list[inx] = parseInt(v[inx]); }
        ret = cast(list, ret);
      } else if (cls == Integer[].class || cls.isAssignableFrom(Integer[].class)) {
        Integer[] list = new Integer[v.length];
        for (int inx = 0; inx < v.length; inx++) { list[inx] = parseInt(v[inx]); }
        ret = cast(list, ret);
      }
      return ret;
    }

    public List<String> keys() {
      return new ArrayList<String>(pmap.keySet());
    }

    public Map<String, String[]> toMap() {
      return this.pmap;
    }

    @Override public String toString() {
      return  convert(this.pmap, "");
    }
  }
}