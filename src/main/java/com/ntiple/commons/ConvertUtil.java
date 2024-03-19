/**
 * @File        : ConvertUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-11-19 최초 작성
 * @Description : 형변환 모듈
 **/
package com.ntiple.commons;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class ConvertUtil {

  // private static final Logger log = LoggerFactory.getLogger(ConvertUtil.class);

  private static Class<?> CLS_ORGJSON = null;
  private static Class<?> CLS_ORSJSON = null;
  private static Class<?> CLS_NSFJSON = null;
  private static Class<?> CLS_ORGJARR = null;

  public static Class<?>[] EMPTY_CLS = new Class<?>[] { };
  public static Object[] EMPTY_OBJ = new Object[] { };

  private static Class<?>[] UNARY_CLS_INT = new Class<?>[] { int.class };
  private static Class<?>[] UNARY_CLS_STRING = new Class<?>[] { String.class };

  private static Method MTD_KEYS_ORGJSON = null;
  private static Method MTD_OPT_ORGJSON = null;
  private static Method MTD_PUT_NSFJSON = null;
  private static Method MTD_PUT_ORGJSON = null;
  private static Method MTD_PUT_ORSJSON = null;
  private static Method MTD_LEN_ORGJARR = null;
  private static Method MTD_OPT_ORGJARR = null;

  private static Constructor<?> CNS_ORGJSON_STRING = null;
  private static final Map<String, Object> TYPE_MAP = null;
  private static final List<Object> TYPE_LIST = null;

  private static SimpleDateFormat FMT_DEFAULT_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  private static Pattern PTN_NUMBER = Pattern.compile("^[0-9]+$");

  static {
    try {
      CLS_ORGJSON = Class.forName("org.json.JSONObject");
      CNS_ORGJSON_STRING = CLS_ORGJSON.getDeclaredConstructor(UNARY_CLS_STRING);
      MTD_KEYS_ORGJSON = CLS_ORGJSON.getMethod("keys", EMPTY_CLS);
      MTD_OPT_ORGJSON = CLS_ORGJSON.getMethod("opt", UNARY_CLS_STRING);
      MTD_PUT_ORGJSON = CLS_ORGJSON.getMethod("put", new Class<?>[] { String.class, Object.class });

      CLS_ORGJARR = Class.forName("org.json.JSONArray");
      MTD_LEN_ORGJARR = CLS_ORGJARR.getMethod("length", EMPTY_CLS);
      MTD_OPT_ORGJARR = CLS_ORGJARR.getMethod("opt", UNARY_CLS_INT);
    } catch (Throwable ignore) { }
    try {
      CLS_ORSJSON = Class.forName("org.json.simple.JSONObject");
      MTD_PUT_ORSJSON = CLS_ORSJSON.getMethod("put", new Class<?>[] { Object.class, Object.class });
    } catch (Throwable ignore) { }
    try {
      CLS_NSFJSON = Class.forName("net.sf.json.JSONObject"); 
      MTD_PUT_NSFJSON = CLS_NSFJSON.getMethod("put", new Class<?>[] { Object.class, Object.class });
    } catch (Throwable ignore) { }
  }

  public static <T> T convert(Object input, Class<T> type) {
    T ret = null;
    try {
      if (Map.class.isAssignableFrom(type)) {
        ret = cast(new LinkedHashMap<String, Object>(), ret);
      } else if (List.class.isAssignableFrom(type)) {
        ret = cast(new LinkedList<Object>(), ret);
      } else {
        ret = type.getDeclaredConstructor(EMPTY_CLS).newInstance(EMPTY_OBJ);
      }
    } catch (Exception ignore) { }
    ret = convert(input, ret);
    return ret;
  }

  public static <T> T convert(Object input, T ret) {
    if (ret == null) { return ret; }
    if (input == null) { return null; }
    Class<?> type = ret.getClass();
    Class<?> vcls = null;
    Object value = null;
    try {
      Map<String, Method> methods = null;
      if (!Map.class.isAssignableFrom(type) &&
          !List.class.isAssignableFrom(type)) {
        methods = new LinkedHashMap<String, Method>();
        for (Method m : type.getMethods()) {
          String name = m.getName();
          if (name.startsWith("set") && name.length() > 3
              && m.getParameterCount() == 1) {
            String fname = decapitalize(name.substring(3));
            Field field = null;
            NamedColumn named = null;
            // log.trace("FIELDNAME:{} / {}", fname, named);
            if (field == null) {
              try { field = type.getDeclaredField(fname); } catch (Exception ignore) { }
            }
            if (field == null) {
              try { field = type.getDeclaredField(capitalize(fname)); } catch (Exception ignore) { }
            }
            if (field != null) {
              if ((named = field.getAnnotation(NamedColumn.class)) != null) {
                String[] names = named.value();
                for (String n : names) {
                  methods.put(n, m);
                }
              }
              fname = field.getName();
            }
            methods.put(fname, m);
          }
        }
      }
      if (input instanceof Map) {
        Map<String, Object> map = cast(input, TYPE_MAP);
        ret = convertMap(map, methods, ret);
      } else if (input instanceof List && ret instanceof List) {
        ret = cast(input, ret);
      } else if (input instanceof String) {
        if (CLS_ORGJSON != null) {
          Object json = CNS_ORGJSON_STRING.newInstance(String.valueOf(input));
          Map<String, Object> map = new LinkedHashMap<String, Object>();
          Iterator<String> keys = cast(MTD_KEYS_ORGJSON.invoke(json, EMPTY_OBJ), keys = null);
          while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, MTD_OPT_ORGJSON.invoke(json, new Object[] { key }));
          }
          ret = convertMap(map, methods, ret);
        }
      } else if (input instanceof String && ret instanceof String) {
        ret = cast(input, ret);
      } else if (CLS_ORGJSON != null && CLS_ORGJSON.isInstance(input)) {
        Iterator<String> keys = cast(MTD_KEYS_ORGJSON.invoke(input, EMPTY_OBJ), keys = null);
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        while (keys.hasNext()) {
          String key = keys.next();
          value = MTD_OPT_ORGJSON.invoke(input, String.valueOf(key));
          vcls = value.getClass();
          if (vcls == CLS_ORGJSON) {
            value = convert(value, Map.class);
          } else if (vcls == CLS_ORGJARR) {
            value = convert(value, List.class);
          }
          map.put(String.valueOf(key), value);
        }
        ret = convertMap(map, methods, ret);
      } else if (CLS_ORGJARR != null && CLS_ORGJARR.isInstance(input)) {
        List<Object> list = new LinkedList<Object>();
        for (int ainx = 0; ainx < cast(MTD_LEN_ORGJARR.invoke(input, EMPTY_OBJ), 0); ainx++) {
          value = MTD_OPT_ORGJARR.invoke(input, ainx);
          vcls = value.getClass();
          if (vcls == CLS_ORGJSON) {
            value = convert(value, Map.class);
          } else if (vcls == CLS_ORGJARR) {
            value = convert(value, List.class);
          }
          list.add(value);
        }
        ret = convert(list, ret);
      } else if (input instanceof Object[][]) {
        Object[][] array = cast(input, array = null);
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (Object[] item : array) {
          if (item != null && item.length >= 2 && item[0] != null) {
            String key = String.valueOf(item[0]);
            map.put(key, item[1]);
          }
        }
        ret = convertMap(map, methods, ret);
      } else {
        Class<?> itype = input.getClass();
        for (Method m : itype.getMethods()) {
          String name = m.getName();
          int pcnt = m.getParameterCount();
          Class<?> rtype = m.getReturnType();
          if ((name.startsWith("is") && name.length() > 2
              && pcnt == 0 && (rtype == boolean.class || rtype == Boolean.class)) ||
            (name.startsWith("get") && name.length() > 3
              && pcnt == 0 && rtype != null)) {
            String key = decapitalize(name.replaceAll("(^get)|(^is)", ""));
            Object val = m.invoke(input, EMPTY_OBJ );
            if (val != null && !"class".equals(key)) {
              if (methods != null && methods.size() > 0) {
                Method method = methods.get(key);
                if (method != null) {
                  method.invoke(ret, new Object[] { val });
                }
              } else if (Map.class.isAssignableFrom(type)) {
                if (!Map.class.isAssignableFrom(itype)) {
                  Field field = null;
                  if (field == null) {
                    try { field = itype.getDeclaredField(key); } catch (Exception ignore) { }
                  }
                  if (field == null) {
                    try { field = itype.getDeclaredField(capitalize(key)); } catch (Exception ignore) { }
                  }
                  if (field != null) {
                    key = field.getName();
                  }
                }
                if (isPrimeType(rtype)) {
                  putMap(ret, key, val);
                } else if (Date.class.isAssignableFrom(rtype)) {
                  Date date = cast(val, date = null);
                  val = format(date);
                  putMap(ret, key, val);
                } else if (List.class.isAssignableFrom(rtype)) {
                  List<Object> list = cast(val, list = null);
                  for (int inx = 0; inx < list.size(); inx++) {
                    list.set(inx, convert(list.get(inx), new LinkedHashMap<>()));
                  }
                  putMap(ret, key, list);
                } else if (rtype == Object.class) {
                  putMap(ret, key, val);
                } else {
                  putMap(ret, key, convert(val, new LinkedHashMap<>()));
                }
              } else if (CLS_ORGJSON != null && CLS_ORGJSON.equals(type)) {
                MTD_PUT_ORGJSON.invoke(ret, new Object[] { key, val });
              } else if (CLS_ORSJSON != null && CLS_ORSJSON.equals(type)) {
                MTD_PUT_ORSJSON.invoke(ret, new Object[] { key, val });
              } else if (CLS_NSFJSON != null && CLS_NSFJSON.equals(type)) {
                MTD_PUT_NSFJSON.invoke(ret, new Object[] { key, val });
              } else {
                putMap(ret, key, convert(val, rtype));
              }
            }
          }
        }
      }
    } catch (Exception ignore) { }
    return ret;
  }

  public static Map<String, Object> tomap(Object input) {
    return convert(input, new LinkedHashMap<>());
  }

  public static Method getGetterMethod(Class<?> type, String key) {
    Method ret = null;
    try {
      if (key == null || type == null || "".equals(key)) { return ret; }
      String mname = cat ("get", capitalize(camelCase(key)));
      for (Method m : type.getMethods()) {
        if (mname.equals(m.getName()) && m.getParameterCount() == 0) {
          ret = m;
          break;
        }
      }
    } catch (Exception ignore) { }
    return ret;
  }

  public static Object invokeGetter(Object inst, String key) {
    Object ret = null;
    if (inst == null) { return ret; }
    if (key == null || "".equals(key)) { return ret; }
    Method getter = getGetterMethod(inst.getClass(), key);
    try { ret = getter.invoke(inst); } catch (Exception ignore) { }
    return ret;
  }

  public static Method getSetterMethod(Class<?> type, String key) {
    Method ret = null;
    try {
      if (key == null || type == null || "".equals(key)) { return ret; }
      String mname = cat ("set", capitalize(camelCase(key)));
      for (Method m : type.getMethods()) {
        if (mname.equals(m.getName()) && m.getParameterCount() == 1) {
          ret = m;
          break;
        }
      }
    } catch (Exception ignore) { }
    return ret;
  }

  public static void invokeSetter(Object inst, String key, Object val) {
    if (inst == null) { return; }
    if (key == null || "".equals(key)) { return; }
    Method setter = getSetterMethod(inst.getClass(), key);
    try { setter.invoke(inst, val); } catch (Exception ignore) { }
  }

  public static boolean isAssignable(Class<?> target, Class<?>... classes) {
    if (target == null) { return false; }
    for (Class<?> cls : classes) {
      if (cls != null && cls.isAssignableFrom(target)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPrimeType(Class<?> type) {
    boolean ret = false;
    if (type == String.class ||
      type == int.class || type == Integer.class ||
      type == long.class || type == Long.class ||
      type == short.class || type == Short.class ||
      type == byte.class || type == Byte.class ||
      type == float.class || type == Float.class ||
      type == double.class || type == Double.class ||
      type == boolean.class || type == Boolean.class) {
      ret = true;
    }
    return ret;
  }

  public static boolean isPrimeType(Object v) {
    return v == null ? false : isPrimeType(v.getClass());
  }

  private static void putMap(Object ret, String key, Object value) {
    if (ret != null && ret instanceof Map) {
      Map<Object, Object> map = cast(ret, map = null);
      map.put(key, value);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object from, Class<T> clsTo) {
    T ret = null;
    try {
      ret = (T) from;
    } catch (ClassCastException ignore) { }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object from, T to) {
    try {
      to = (T) from;
    } catch (ClassCastException ignore) { }
    return to;
  }

  public static Map<String, Object> castmap(Object from) {
    return cast(from, new LinkedHashMap<>());
  }

  public static <T> T convertMap(Map<String, Object> map, Map<String, Method> methods, T ret) {
    Object item = null;
    Class<?> type = ret.getClass();
    Class<?> ptype = null;
    if (ret instanceof Map) { return cast(map, ret); }
    for (String key : map.keySet()) {
      item = map.get(key);
      if (methods != null) {
        Method method = methods.get(key);
        if (method != null) {
          ptype = method.getParameterTypes()[0];
          if ( (ptype == String.class && (item = parseStr(item)) != null)) {
            String fname = decapitalize(method.getName().substring(3));
            Field field = null;
            Class<?> clss = ret.getClass();
            try {
              field = clss.getDeclaredField(fname);
              if (field.isAnnotationPresent(DateColumn.class)) {
                item = format(date(item)).substring(0, 8);
              } else if (field.isAnnotationPresent(TimeColumn.class)) {
                item = format(date(item)).substring(8);
              } else if (field.isAnnotationPresent(DateTimeColumn.class)) {
                item = format(date(item));
              }
            } catch (Exception ignore) { }
            try {
              method.invoke(ret, item);
            } catch (Exception e) {
              // log.debug("ERROR ON CONVERT:{} / {}", method, e.getMessage());
            }
          } else if (
            ((ptype == int.class | ptype == Integer.class) 
              && (item = parseInt(item, null)) != null) ||
            ((ptype == long.class || ptype == Long.class)
              && (item = parseLong(item, null)) != null) ||
            ((ptype == short.class || ptype == Short.class)
              && (item = parseShort(item, null)) != null) ||
            ((ptype == byte.class || ptype == Byte.class)
              && (item = parseByte(item, null)) != null) ||
            ((ptype == float.class || ptype == Float.class)
              && (item = parseFloat(item, null)) != null) ||
            ((ptype == double.class || ptype == Double.class)
              && (item = parseDouble(item, null)) != null) ||
            ((ptype == boolean.class || ptype == Boolean.class)
              && (item = parseBoolean(item, null)) != null) ) {
            try {
              method.invoke(ret, item);
            } catch (Exception ignore) { }
          } else if (List.class.isAssignableFrom(ptype)) {
            if (item instanceof List) {
              try {
                Class<?> gtyp = null;
                Type[] gtyps = method.getGenericParameterTypes();
                if (gtyps != null && gtyps.length > 0) {
                  Matcher m = Pattern.compile("[<]([a-zA-Z0-9$._]+)[>]").matcher(gtyps[0].getTypeName());
                  if (m.find() && (gtyp = Class.forName(m.group(1))) != null) {
                    List<Object> list1 = cast(item, list1 = null);
                    List<Object> list2 = new LinkedList<>();
                    for (int inx = 0; inx < list1.size(); inx++) {
                      Object iobj = list1.get(inx);
                      if (iobj != null && iobj.getClass().isAssignableFrom(gtyp)) {
                        list2.add(iobj);
                      } else {
                        list2.add(convert(list1.get(inx), gtyp));
                      }
                    }
                    item = list2;
                  }
                }
              } catch (Exception ignore) { }
              try {
                method.invoke(ret, item);
              } catch (Exception ignore) { }
            } else if (item != null && CLS_ORSJSON.isAssignableFrom(item.getClass())) {
            }
          } else if (Object.class.equals(ptype)) {
            try {
              method.invoke(ret, item);
            } catch (Exception ignore) { }
          } else {
            /* item is not basic type */
            try {
              method.invoke(ret, convert(item, ptype));
            } catch (Exception ignore) { }
          }
        }
      } else if (CLS_ORGJSON != null && CLS_ORGJSON.equals(type)) {
        try { MTD_PUT_ORGJSON.invoke(ret, new Object[] { key, item }); } catch (Exception ignore) { }
      } else if (CLS_ORSJSON != null && CLS_ORSJSON.equals(type)) {
        try { MTD_PUT_ORSJSON.invoke(ret, new Object[] { key, item }); } catch (Exception ignore) { }
      } else if (CLS_NSFJSON != null && CLS_NSFJSON.equals(type)) {
        try { MTD_PUT_NSFJSON.invoke(ret, new Object[] { key, item }); } catch (Exception ignore) { }
      }
    }
    return ret;
  }
  public static String decapitalize(String str) {
    char c = str.charAt(0);
    if (c >= 'A' && c <= 'Z') {
      c = (char) ((int) c + 32);
      str = c + str.substring(1);
    }
    return str;
  }
  public static String capitalize(String str) {
    char c = str.charAt(0);
    if (c >= 'a' && c <= 'z') {
      c = (char) ((int) c - 32);
      str = c + str.substring(1);
    }
    return str;
  }
  public static String parseStr(Object o) {
    if (o != null) {
      return String.valueOf(o);
    }
    return null;
  }
  public static String parseStr(Object o, String def) {
    String ret = def;
    if (o == null) { return def; }
    ret = parseStr(o);
    if (ret == null) { ret = def; }
    return ret;
  }
  public static Integer parseInt(Object o) { return parseInt(o, null); }
  public static Integer parseInt(Object o, Integer def) {
    Integer ret = def;
    if (o == null) { return def; }
    try { ret = Integer.parseInt(String.valueOf(o)); } catch (Exception ignore) { }
    if (ret == null) { ret = def; }
    return ret;
  }
  public static Long parseLong(Object o) { return parseLong(o, null); }
  public static Long parseLong(Object o, Long def) {
    Long ret = def;
    if (o == null) { return def; }
    try { ret = Long.parseLong(String.valueOf(o)); } catch (Exception ignore) { }
    if (ret == null) { ret = def; }
    return ret;
  }
  public static Float parseFloat(Object o) { return parseFloat(o, null); }
  public static Float parseFloat(Object o, Float def) {
    Float ret = def;
    if (o == null) { return def; }
    try { ret = Float.parseFloat(String.valueOf(o)); } catch (Exception ignore) { }
    if (ret == null) { ret = def; }
    return ret;
  }
  public static Double parseDouble(Object o) { return parseDouble(o, null); }
  public static Double parseDouble(Object o, Double def) {
    Double ret = def;
    if (o == null) { return def; }
    try { ret = Double.parseDouble(String.valueOf(o)); } catch (Exception ignore) { }
    if (ret == null) { ret = def; }
    return ret;
  }
  public static Short parseShort(Object o) { return parseShort(o, null); }
  public static Short parseShort(Object o, Short def) {
    Short ret = def;
    if (o == null) { return def; }
    try { ret = Short.parseShort(String.valueOf(o)); } catch (Exception ignore) { }
    if (ret == null) { ret = def; }
    return ret;
  }
  public static Byte parseByte(Object o) { return parseByte(o, null); }
  public static Byte parseByte(Object o, Byte def) {
    Byte ret = def;
    if (o == null) { return def; }
    try { ret = Byte.parseByte(String.valueOf(o)); } catch (Exception ignore) { }
    if (ret == null) { ret = def; }
    return ret;
  }
  public static Boolean parseBoolean(Object o) { return parseBoolean(o, null); }
  public static Boolean parseBoolean(Object o, Boolean def) {
    Boolean ret = def;
    if (o == null) { return def; }
    try { ret = Boolean.parseBoolean(String.valueOf(o)); } catch (Exception ignore) { }
    if (ret == null) { ret = def; }
    return ret;
  }

  public static String trim(String str) {
    String ret = str;
    if (ret == null) { return ret; }
    ret = ret.trim();
    return ret;
  }

  public static String substring(String str, int st, Integer ed) {
    String ret = str;
    if (str == null) { return ret; }
    if (str.length() < st) { return ret; }
    if (ed != null && str.length() <= ed) { ed = str.length() - 1; }
    if (ed != null) {
      ret = str.substring(st, ed);
    } else {
      ret = str.substring(st);
    }
    return ret;
  }
  public static Date parseDate(String str) { return parseCalendar(str).getTime(); }
  public static Calendar parseCalendar(String str) {
    Pattern[] PTN_DATE = {
      Pattern.compile("^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2})[:]([0-9]{2})[:]([0-9]{2})([.]([0-9]{1,3}{0,1})){0,1}([+-])([0-9]{2})[:]([0-9]{2})$"),
      Pattern.compile("^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2})[:]([0-9]{2})([.]([0-9]{1,3}{0,1})){0,1}([+-])([0-9]{2})[:]([0-9]{2})$"),
      Pattern.compile("^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2})[:]([0-9]{2})[:]([0-9]{2})([.])([0-9]{1,3}{0,1}){0,1}[Z]{0,1}$"),
      Pattern.compile("^([0-9]{4})[^0-9]{0,1}([0-9]{2})[^0-9]{0,1}([0-9]{2})[^0-9]{0,1}([0-9]{2})[^0-9]{0,1}([0-9]{2})[^0-9]{0,1}([0-9]{2})([.][0-9]{1,3}{0,1}){0,1}[Z]{0,1}$"),
      Pattern.compile("^([0-9]{4})[^0-9]{0,1}([0-9]{2})([^0-9]{0,1}([0-9]{2})){0,1}([^0-9]{0,1}([0-9]{2})){0,1}([^0-9]{0,1}([0-9]{2})){0,1}$"),
    };

    Matcher mat = null;
    int yr, mt, dt, hr, mi, sc, ms, ah, am;
    long td = 0;
    String pp;
    yr = mt = dt = 1;
    hr = mi = sc = ms = ah = am = 0;
    long to = TimeZone.getDefault().getRawOffset();
    long ct = 0;

    Calendar cld = Calendar.getInstance();
    cld.setTimeInMillis(0);
    // log.debug("PARSECALENDAR:{}", str);
    LOOP: for (int inx = 0; inx < PTN_DATE.length; inx++) {
      Pattern ptn = PTN_DATE[inx];
      if ((mat = ptn.matcher(str)) != null && mat.find()) {
        // log.trace("MATCH-{}:{} / {}", inx, mat);
        SW: switch (inx) {
        case 0: {
          yr = parseInt(mat.group(1), 1);
          mt = parseInt(mat.group(2), 1);
          dt = parseInt(mat.group(3), 1);
          hr = parseInt(mat.group(4), 0);
          mi = parseInt(mat.group(5), 0);
          sc = parseInt(mat.group(6), 0);
          ms = parseInt(mat.group(8), 0);
          pp = mat.group(9);
          ah = parseInt(mat.group(10), 0);
          am = parseInt(mat.group(11), 0);
          td = (ah * 1000 * 60 * 60) + (am * 1000 * 60);
          if ("-".equals(pp)) { td = td * -1; }
          // log.trace("PARSED[{}]: {}-{}-{} {}:{}:{}.{} {}{}:{} / {}:{} // {}", inx, yr, mt, dt, hr, mi, sc, ms, pp, ah, am, td, to, str);
        } break SW;
        case 1: {
          yr = parseInt(mat.group(1), 1);
          mt = parseInt(mat.group(2), 1);
          dt = parseInt(mat.group(3), 1);
          hr = parseInt(mat.group(4), 0);
          mi = parseInt(mat.group(5), 0);
          sc = 0;
          ms = parseInt(mat.group(7), 0);
          pp = mat.group(8);
          ah = parseInt(mat.group(9), 0);
          am = parseInt(mat.group(10), 0);
          td = (ah * 1000 * 60 * 60) + (am * 1000 * 60);
          if ("-".equals(pp)) { td = td * -1; }
          // log.trace("PARSED[{}]: {}-{}-{} {}:{}:{}.{} {}{}:{} / {}:{} // {}", inx, yr, mt, dt, hr, mi, sc, ms, pp, ah, am, td, to, str);
        } break SW;
        case 2: {
          yr = parseInt(mat.group(1), 1);
          mt = parseInt(mat.group(2), 1);
          dt = parseInt(mat.group(3), 1);
          hr = parseInt(mat.group(4), 0);
          mi = parseInt(mat.group(5), 0);
          sc = parseInt(mat.group(6), 0);

          pp = mat.groupCount() > 7 ? mat.group(7) : "";
          ms = parseInt(mat.groupCount() > 8 ? mat.group(8) : "0", 0);
          // log.trace("PARSED[{}]: {}-{}-{} {}:{}:{}.{} // {}", inx, yr, mt, dt, hr, mi, sc, ms, str);
        } break SW;
        case 3: {
          yr = parseInt(mat.group(1), 1);
          mt = parseInt(mat.group(2), 1);
          dt = parseInt(mat.group(3), 1);
          hr = parseInt(mat.group(4), 0);
          mi = parseInt(mat.group(5), 0);
          sc = parseInt(mat.group(6), 0);

          // log.trace("PARSED[{}]: {}-{}-{} {}:{}:{} // {}", inx, yr, mt, dt, hr, mi, sc, str);
        } break SW;
        case 4: {
          yr = parseInt(mat.group(1), 1);
          mt = parseInt(mat.group(2), 1);
          dt = parseInt(mat.group(4), 1);

          hr = parseInt(mat.groupCount() > 6 ? mat.group(6) : "0", 0);
          mi = parseInt(mat.groupCount() > 8 ? mat.group(8) : "0", 0);
          sc = parseInt(mat.groupCount() > 10 ? mat.group(10) : "0", 0);

          // log.trace("PARSED[{}]: {}-{}-{} {}:{}:{} // {}", inx, yr, mt, dt, hr, mi, sc, str);
        } break SW;
        }
        break LOOP;
      }
      continue LOOP;
    }

    cld.set(Calendar.YEAR, yr);
    cld.set(Calendar.MONTH, mt - 1);
    cld.set(Calendar.DATE, dt);
    cld.set(Calendar.HOUR_OF_DAY, hr);
    cld.set(Calendar.MINUTE, mi);
    cld.set(Calendar.SECOND, sc);
    cld.set(Calendar.MILLISECOND, ms);

    // log.debug("CALENDAR:{} / {}", cld.getTime(), hr);
    if (td != 0) {
      ct = cld.getTime().getTime();
      ct = ct - td;
      ct = ct + to;
      cld.setTimeInMillis(ct);
    }
    return cld;
  }

  public static String string(Object o) { return string(o, ""); }
  public static String string(Object o, String def) {
    String ret = def;
    if (o != null) { ret = String.valueOf(o); }
    return ret;
  }

  public static class DateTime {
    long time;
    Calendar cal;
    SimpleDateFormat format;
    public DateTime() { }

    public DateTime add(int field, int val) {
      return this;
    }
    public DateTime set(int field, int val) {
      return this;
    }
    public DateTime setTimeInMillis(long timeInMillis) {
      return this;
    }
    public DateTime setFormat(String format) {
      this.format = new SimpleDateFormat(format);
      return this;
    }
    public DateTime setFormat(SimpleDateFormat format) {
      this.format = format;
      return this;
    }
    public DateTime setTime(Date date) {
      return this;
    }
    public DateTime reset() {
      return this;
    }
    public boolean isAfter(Object o) {
      boolean ret = false;
      return ret;
    }
    public boolean isBefore(Object o) {
      boolean ret = false;
      return ret;
    }
    public Calendar calendar() {
      return cal;
    }
    public Date date() {
      Date ret = null;
      return ret;
    }
    public long timeInMillis() {
      long ret = 0;
      return ret;
    }
    @Override public String toString() {
      String ret = null;
      if (cal != null) {
        
      }
      return ret;
    }
  }

  public static Date date(Object o) {
    Date ret = null;
    if (o == null) { return null; }
    try {
      if (o instanceof String) {
        String str = String.valueOf(o).trim().replaceAll("[^0-9]+", "");
        String ptn = "yyyyMMddHHmmssSSS";
        SimpleDateFormat df = null;
        int len = str.length();
        switch(len) {
        case 4: case 6: case 8:
        case 10: case 12: case 14:
        case 16:
          df = new SimpleDateFormat(ptn.substring(0, len));
          break;
        }
        if (df != null) {
          ret = df.parse(str);
        }
      }
    } catch (Exception ignore) { }
    return ret;
  }

  public static Calendar cal(Object o) {
    return null;
  }

  public static String format(Calendar cal) { return format(cal, FMT_DEFAULT_DATE); }
  public static String format(Date date) { return format(date, FMT_DEFAULT_DATE); }
  public static String format(Calendar cal, SimpleDateFormat fmt) { if (cal != null) { return format(cal.getTime(), fmt); } return null; }
  public static String format(Date date, SimpleDateFormat fmt) {
    String ret = null;
    if (date == null) { return ret; }
    if (fmt == null) { fmt = FMT_DEFAULT_DATE; }
    ret = fmt.format(date);
    return ret;
  }

  public static class Collection {
    private Collection parent;
    private String key;
    private Object item;

    public Collection() { }

    public static Collection create() {
      return new Collection();
    }

    private Collection(Collection parent, Object item) {
      this.parent = parent;
      this.item = item;
    }

    public Object item() {
      return item;
    }

    public Collection k(Object key) {
      this.key = String.valueOf(key);
      return this;
    }

    public Collection v(Object value) {
      if (item != null) {
        if (item instanceof Map && key != null) {
          cast(item, TYPE_MAP).put(key, value);
        } else if (item instanceof List) {
          cast(item, TYPE_LIST).add(value);
        }
      }
      return this;
    }

    public Collection v(Object... values) {
      if (item != null && item instanceof List) {
        for (Object value : values) {
          v(value);
        }
      } else if (item != null && item instanceof Map) {
        for (int inx = 0; inx < values.length; inx+=2) {
          Object key = values[inx];
          Object val = null;
          if (inx + 1 < values.length) {
            val = values[inx + 1];
          }
          k(key).v(val);
        }
      }
      return this;
    }

    public Collection obj() {
      Collection ret = new Collection(this, new LinkedHashMap<>());
      if (parent == null) {
        this.item = ret.item;
      }
      return ret;
    }

    public Collection obj(String key) {
      k(key);
      return obj();
    }

    public Collection arr() {
      Collection ret = new Collection(this, new LinkedList<>());
      if (parent == null) {
        this.item = ret.item;
      }
      return ret;
    }

    public Collection arr(String key) {
      k(key);
      return arr();
    }

    public Collection end() {
      if (parent != null && parent.item != null) {
        if (parent.item instanceof Map && parent.item != this.item &&
            parent.key != null) {
          cast(parent.item, TYPE_MAP).put(parent.key, this.item);
        } else if (parent.item instanceof List && parent.item != this.item) {
          cast(parent.item, TYPE_LIST).add(this.item);
        }
      }
      return parent;
    }

    @Override
    public String toString() {
      return String.valueOf(item);
    }
  }

  public static Map<String, Object> extractHierarchyProp(Map<String, Object> prop) {
    Map<String, Object> ret = new LinkedHashMap<>();
    // log.trace("PROP:{}", prop);
    for (String key : prop.keySet()) {
      Object val = prop.get(key);
      if (val instanceof Map) {
        Map<String, Object> item = extractHierarchyProp(cast(val, prop));
        for (String ikey : item.keySet()) {
          val = item.get(ikey);
          // log.trace("PROP[{}.{}] = {}", key, ikey, val);
          ret.put(key + "." + ikey, item.get(ikey));
        }
      } else {
        // log.trace("PROP[{}] = {}", key, val);
        ret.put(key, String.valueOf(val));
      }
    }
    return ret;
  }

  public static String camelCase(String str) {
    String ret = "";
    String[] words = str.split("_");
    for (String word : words) {
      if (ret.length() == 0) {
        ret = word.toLowerCase();
      } else {
        ret += capitalize(word.toLowerCase());
      }
    }
    return ret;
  }

  public static String snakeCase(String str) {
    String ret = "";
    for (int inx = 0; inx < str.length(); inx++) {
      char c = str.charAt(inx);
      if (c >= 'a' && c <= 'z') {
        ret += (char)(c - 32);
      } else {
        ret += "_" + (char)c;
      }
    }
    return ret;
  }

  public static Map<String, Object> camelCase(Map<String, Object> map) {
    if (map == null) { return null; }
    Map<String, Object> ret = new LinkedHashMap<>();
    for (String key : map.keySet()) {
      Object value = map.get(key);
      if (value instanceof Map) {
        Map<String, Object> item = cast(value, item = null);
        value = camelCase(item);
      } else if (value instanceof List) {
        List<Object> item = cast(value, item = null);
        value = camelCase(item);
      }
      if (value instanceof String) {
        value = cast(value, "").trim();
      }
      ret.put(camelCase(key), value);
    }
    return ret;
  }

  public static List<Object> camelCase(List<Object> list) {
    if (list == null) { return null; }
    List<Object> ret = new LinkedList<>();
    for (Object value : list) {
      if (value instanceof Map) {
        Map<String, Object> item = cast(value, item = null);
        value = camelCase(item);
      } else if (value instanceof List) {
        List<Object> item = cast(value, item = null);
        value = camelCase(item);
      }
      ret.add(value);
    }
    return ret;
  }

  public static Map<String, Object> decapitalize(Map<String, Object> map) {
    if (map == null) { return null; }
    Map<String, Object> ret = new LinkedHashMap<>();
    for (String key : map.keySet()) {
      Object value = map.get(key);
      if (value instanceof Map) {
        Map<String, Object> item = cast(value, item = null);
        value = decapitalize(item);
      } else if (value instanceof List) {
        List<Object> item = cast(value, item = null);
        value = decapitalize(item);
      }
      if (value instanceof String) {
        value = cast(value, "").trim();
      }
      ret.put(decapitalize(key), value);
    }
    return ret;
  }

  public static List<Object> decapitalize(List<Object> list) {
    if (list == null) { return null; }
    List<Object> ret = new LinkedList<>();
    for (Object value : list) {
      if (value instanceof Map) {
        Map<String, Object> item = cast(value, item = null);
        value = decapitalize(item);
      } else if (value instanceof List) {
        List<Object> item = cast(value, item = null);
        value = decapitalize(item);
      }
      ret.add(value);
    }
    return ret;
  }

  public static Object getCascade(Map<String, Object> map, String... keys) {
    Object ret = null;
    if (map == null) { return ret; }
    if (keys == null) { return ret; }
    Object t = null;
    for (String key : keys) {
      if (key == null) { return ret; }
      t = map.get(key);
      if (t != null && t instanceof Map) {
        map = cast(t, map);
      }
    }
    if (t != null) { ret = t; }
    return ret;
  }

  public static String[] varr(String... arg) {
    String[] ret = arg;
    return ret;
  }

  public static String cat(Object... arg) {
    String ret = "";
    for (Object a : arg) {
      if (a instanceof String) {
        ret = ret + a;
      } else {
        ret = ret + String.valueOf(a);
      }
    }
    return ret;
  }

  @Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface DateTimeColumn { }

  @Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface DateColumn { }

  @Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface TimeColumn { }

  @Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface NamedColumn {
    String[] value() default{};
  }

  public static List<String> codeSplit(Object v, String delim) {
    List<String> ret = null;
    if (v != null) {
      String[] split = String.valueOf(v).replaceAll("\\\s*", "").split(delim);
      ret = Arrays.asList(split);
    }
    return ret;
  }

  public static Map<String, Object> newMap() { return new LinkedHashMap<>(); }

  @SafeVarargs
  public static <T extends Object> Map<String, T> mergeMap(T... maps) {
    Map<String, T> ret = new LinkedHashMap<>();
    for (Object item : maps) {
      if (item instanceof Map) {
        Map<String, T> map = cast(item, map = null);
        for (String key : map.keySet()) {
          ret.put(key, map.get(key));
        }
      }
    }
    return ret;
  }

  @SafeVarargs
  public static <T extends Object> List<T> asList(T... arr) {
    List<T> ret = null;
    if (arr != null && arr.length > 0) {
      ret = new ArrayList<>();
      for (T itm : arr) { ret.add(itm); }
    }
    return ret;
  }

  @SafeVarargs
  public static <T> List<T> mergeList(List<T>... lst) {
    List<T> ret = new ArrayList<>();
    for (List<T> itm : lst) { ret.addAll(itm); }
    return ret;
  }

  @SafeVarargs
  public static <T> T[] arr(T... arr) { return arr; }

  @SafeVarargs
  public static <T> T[] mergeArr(T[]... arr) {
    T[] ret = null;
    int ginx = 0, len = 0;
    for (T[] itm : arr) { len += itm.length; }
    ret = Arrays.copyOf(arr[0], len);
    for (int inx = 0; inx < arr.length; inx++) {
      T[] itm = arr[inx];
      for (int sinx = 0; sinx < itm.length; sinx++, ginx++) {
        ret[ginx] = itm[sinx];
      }
    }
    return ret;
  }

  public static <T> T av(T[] arr, int inx) { return av(arr, inx, null); }
  public static <T> T av(T[] arr, int inx, T def) {
    T ret = def;
    if (arr == null || inx < 0 || arr.length == 0 || arr.length <= inx) { return ret; }
    ret = arr[inx];
    return ret;
  }

  public static String join(Object obj, String delim) {
    StringBuilder ret = new StringBuilder();
    if (obj instanceof List) {
      for (Object item : (List<?>)obj) {
        if (ret.length() > 0) { ret.append(delim); }
        ret.append(String.valueOf(item));
      }
    } else if (obj instanceof String[]) {
      for (String item : (String[])obj) {
        if (ret.length() > 0) { ret.append(delim); }
        ret.append(String.valueOf(item));
      }
    }
    return String.valueOf(ret);
  }

  public static List<String> attrAsList(List<?> lst, String colname) { return attrAsList(lst, colname, String.class); }

  public static <O> List<O> attrAsList(List<?> lst, String colname, Class<O> cls) {
    List<O> ret = new ArrayList<>();
    if (lst == null || lst.size() == 0) { return ret; }
    for (Object item : lst) {
      Object o = null;
      if (item != null) {
        Class<?> tcls = (Class<?>)item.getClass();
        if (!isPrimeType(tcls)) {
          o = invokeGetter(item, colname);
          if (isAssignable(cls, String.class)) {
            o = parseStr(o);
          } else if (isAssignable(cls, int.class, Integer.class)) {
            o = parseInt(o, 0);
          } else if (isAssignable(cls, long.class, Long.class)) {
            o = parseLong(o, 0L);
          } else if (isAssignable(cls, float.class, Float.class)) {
            o = parseFloat(o, 0F);
          } else if (isAssignable(cls, double.class, Double.class)) {
            o = parseDouble(o, 0D);
          }
        } else {
          o = item;
        }
      }
      try {
        ret.add(cast(o, cls));
      } catch (ClassCastException e) {
        ret.add(null);
      }
    }
    return ret;
  }

  /**
   *  list1 의 모든 원소가 list2 에 존재하는지 확인, list2 에 없는 원소만 리턴한다.
   **/
  public static <T> List<T> listDiff(List<T> list1, List<T> list2) { return listDiff(list1, list2, null); }
  public static <T> List<T> listDiff(List<T> list1, List<T> list2, Comparator<T> comp) {
    if (list1 == null) { list1 = new ArrayList<>(); }
    if (list2 == null) { list2 = new ArrayList<>(); }
    List<T> ret = new ArrayList<>();
    T a1, a2;
    boolean flg = false;
    try {
      LOOP1: for (int inx1 = 0; inx1 < list1.size(); inx1++) {
        a1 = list1.get(inx1);
        flg = false;
        LOOP2: for (int inx2 = 0; inx2 < list2.size(); inx2++) {
          a2 = list2.get(inx2);
          if (comp != null) {
            if (comp.compare(a1, a2) == 0) {
              flg = true;
              break LOOP2;
            }
          } else {
            if (a1.equals(a2)) {
              flg = true;
              break LOOP2;
            }
          }
        }
        if (!flg) { ret.add(a1); }
        continue LOOP1;
      }
    } catch (Exception ignore) { }
    return ret;
  }

  // public static String tojsonstr(Object o) {
  //   String ret = "";
  //   ret = String.valueOf(new JSONObject(o));
  //   return ret;
  // }

  public static boolean isNumPtn(String str) {
    boolean ret = false;
    if (str == null) { return ret; }
    if (PTN_NUMBER.matcher(str).find()) {
      ret = true;
    }
    return ret;
  }

  public static String repeatStr(String s, int len) {
    String ret = "";
    for (int inx = 0; inx < len; inx++) { ret += s; }
    return ret;
  }

  public static String strreplace(String src, String find, String replace) {
    String ret = src;
    if (src == null) { return ret; }
    if (find == null) { return ret; }
    if (replace == null) { return ret; }
    int st;
    while(true) {
      if ((st = src.indexOf(find)) == -1) {
        ret = src;
        break;
      }
      src = src.substring(0, st) + replace + src.substring(st + find.length());
    }
    return ret;
  }

  private static final Pattern PTN_SPRING_PLACEHOLDER = Pattern.compile("\"\\$\\{(?<name>[a-zA-Z0-9_.]+)(:(?<defv>.*)){0,1}\\}\"");
  private static final String CLS_NAME_SPRING_ANNOTATION_VALUE = "org.springframework.beans.factory.annotation.Value";

  public static <T> T mappingSpringValues(Map<String, Object> map, T target) {
    T ret = target;
    if (target == null) { return ret; }
    Class<?> cls = target.getClass();
    Field[] fields = cls.getDeclaredFields();
    LOOP_FLD: for (Field field : fields) {
      Annotation[] anons = field.getAnnotations();
      LOOP_ANO: for (Annotation anon : anons) {
        String annm = anon.annotationType().getName();
        if (CLS_NAME_SPRING_ANNOTATION_VALUE.equals(annm)) {
          // log.debug("ANON:{} / {}", anon.annotationType().getName(), anon);
          // Value value = cast(anon, value = null);
          String stptn = anon.toString();
          Matcher mat = PTN_SPRING_PLACEHOLDER.matcher(stptn);
          if (mat.find()) {
            String msm = cat("set", capitalize(field.getName()));
            String mgm = cat("get", capitalize(field.getName()));
            String vnm = mat.group("name");
            String dfv = mat.group("defv");
            Method mts = null;
            Method mtg = null;
            Object itm = null;
            Class<?> fty = field.getType();
            // log.debug("FIELD[{}] = {} / {}:{}", mnm, fty, vnm, dfv);
            try {
              mts = cls.getMethod(msm, new Class<?>[] { fty });
              mtg = cls.getMethod(mgm, EMPTY_CLS);
              itm = map.get(vnm);
              if (itm == null) { itm = getCascade(map, vnm.split("[.]")); }
              /** 입력할 값이 없고 기존값이 있다면 기존값 유지 */
              if (itm == null && mtg.invoke(target, EMPTY_OBJ) != null) { continue LOOP_FLD; } 
              /** 입력할 값이 없고 기존값도 없다면 기본입력값 대입 */
              if (itm == null && dfv != null) { itm = dfv; }
              if (mts != null) {
                if (
                  ((fty == int.class | fty == Integer.class) 
                    && (itm = parseInt(itm, null)) != null) ||
                  ((fty == long.class || fty == Long.class)
                    && (itm = parseLong(itm, null)) != null) ||
                  ((fty == short.class || fty == Short.class)
                    && (itm = parseShort(itm, null)) != null) ||
                  ((fty == byte.class || fty == Byte.class)
                    && (itm = parseByte(itm, null)) != null) ||
                  ((fty == float.class || fty == Float.class)
                    && (itm = parseFloat(itm, null)) != null) ||
                  ((fty == double.class || fty == Double.class)
                    && (itm = parseDouble(itm, null)) != null) ||
                  ((fty == boolean.class || fty == Boolean.class)
                    && (itm = parseBoolean(itm, null)) != null)
                  ) {
                  /** NO-OP */
                } else {
                  itm = parseStr(itm, null);
                }
                mts.invoke(target, new Object[] { itm });
              }
            } catch (Exception e) {
            }
          }
        }
        continue LOOP_ANO;
      }
      continue LOOP_FLD;
    }
    return ret;
  }
}
