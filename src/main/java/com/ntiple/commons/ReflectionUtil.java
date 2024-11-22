/**
 * @File        : ReflectionUtil.java
 * @Author      : 정재백
 * @Since       : 2024-11-20
 * @Description : Reflection 에 자주 사용되는 메소드 모음
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

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
