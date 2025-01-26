/**
 * @File        : ReflectionUtil.java
 * @Author      : 정재백
 * @Since       : 2024-11-20
 * @Description : Reflection 에 자주 사용되는 메소드 모음
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.StringUtil.camelCase;
import static com.ntiple.commons.StringUtil.capitalize;
import static com.ntiple.commons.StringUtil.cat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
  private static final SimpleLogger log = SimpleLogger.getLogger();

  public static Class<?>[] EMPTY_CLS = new Class<?>[] { };
  public static Object[] EMPTY_OBJ = new Object[] { };

  public static Class<?>[] UNARY_CLS_INT = new Class<?>[] { int.class };
  public static Class<?>[] UNARY_CLS_STRING = new Class<?>[] { String.class };


  public static final Class<?> findClass(String clsname) throws Exception { return Class.forName(clsname.trim()); }
  public static final Method findMethod(Class<?> cls, String name, Class<?>... arg) throws Exception {
    try { return cls.getDeclaredMethod(name.trim(), arg); } catch (Throwable ignore) { };
    try { return cls.getMethod(name.trim(), arg); } catch (Throwable ignore) { };
    return null;
  }
  public static final Field findField(Class<?> cls, String name) throws Exception {
    try { return cls.getDeclaredField(name.trim()); } catch (Throwable ignore) { };
    try { return cls.getField(name.trim()); } catch (Throwable ignore) { };
    return null;
  }
  public static final Object findFieldValue(Class<?> cls, String name) throws Exception { return findFieldValue(cls, name.trim(), null); }
  public static final Object findFieldValue(Class<?> cls, String name, Object inst) throws Exception { return findField(cls, name.trim()).get(inst); }
  public static final Constructor<?> findConstructor(Class<?> cls) throws Exception { return cls.getDeclaredConstructor(EMPTY_CLS); }
  public static final Constructor<?> findConstructor(Class<?> cls, Class<?>... arg) throws Exception { return cls.getDeclaredConstructor(arg); }
  public static final Object newInstance(Class<?> cls) throws Exception { return findConstructor(cls).newInstance(EMPTY_OBJ); }

  public static Method getGetterMethod(Class<?> type, String key) {
    Method ret = null;
    try {
      if (key == null || type == null || "".equals(key)) {
        return ret;
      }
      String mname = cat("get", capitalize(camelCase(key)));
      for (Method m : type.getMethods()) {
        if (mname.equals(m.getName()) && m.getParameterCount() == 0) {
          ret = m;
          break;
        }
      }
    } catch (Exception e) { log.info("E:{}", e); }
    return ret;
  }

  public static Object invokeGetter(Object inst, String key) {
    Object ret = null;
    if (inst == null) { return ret; }
    if (key == null || "".equals(key)) { return ret; }
    Method getter = getGetterMethod(inst.getClass(), key);
    try { ret = getter.invoke(inst); } catch (Exception e) { log.info("E:{}", e); }
    return ret;
  }

  public static Method getSetterMethod(Class<?> type, String key) {
    Method ret = null;
    try {
      if (key == null || type == null || "".equals(key)) { return ret; }
      String mname = cat("set", capitalize(camelCase(key)));
      for (Method m : type.getMethods()) {
        if (mname.equals(m.getName()) && m.getParameterCount() == 1) {
          ret = m;
          break;
        }
      }
    } catch (Exception e) { log.info("E:{}", e); }
    return ret;
  }

  public static void invokeSetter(Object inst, String key, Object val) {
    if (inst == null) { return; }
    if (key == null || "".equals(key)) { return; }
    Method setter = getSetterMethod(inst.getClass(), key);
    try { setter.invoke(inst, val); } catch (Exception e) { log.info("E:{}", e); }
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



  @SuppressWarnings("unchecked")
  public static <T> T cast(Object from, T to) {
    try {
      to = (T) from;
    } catch (ClassCastException ignore) { log.trace("E:{}", ignore); }
    return to;
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Object from, Class<T> clsTo) {
    T ret = null;
    try {
      ret = (T) from;
    } catch (ClassCastException ignore) { log.trace("E:{}", ignore); }
    return ret;
  }
}
