/**
 * @File        : MybatisUtil.java
 * @Author      : 정재백
 * @Since       : 2025-02-03
 * @Description : mybatis 설정유틸
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.IOUtil.readAsString;
import static com.ntiple.commons.IOUtil.safeclose;
import static com.ntiple.commons.ProcUtil.sleep;
import static com.ntiple.commons.ReflectionUtil.cast;
import static com.ntiple.commons.ReflectionUtil.EMPTY_CLS;
import static com.ntiple.commons.ReflectionUtil.EMPTY_OBJ;
import static com.ntiple.commons.ReflectionUtil.findClass;
import static com.ntiple.commons.ReflectionUtil.findConstructor;
import static com.ntiple.commons.ReflectionUtil.findMethod;
import static com.ntiple.commons.ReflectionUtil.newArray;
import static com.ntiple.commons.ReflectionUtil.toArray;
import static com.ntiple.commons.StringUtil.cat;
import static com.ntiple.commons.XMLWorker.parseXML;
import static com.ntiple.commons.XMLWorker.xmlAttr;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.ntiple.commons.FunctionUtil.Fn1at;

public class MybatisConfigUtil {
  private static final SimpleLogger log = SimpleLogger.getLogger();

  private static Class<?> CLS_SQLSESSION_FACTORY_BEAN;
  private static Class<?> CLS_ALIAS;
  private static Class<?> CLS_TYPE_HANDLER;
  private static Class<?> CLS_RESOURCE;
  private static Class<?> CLS_APPLICATION_CONTEXT;
  private static Class<?> CLS_SQL_SESSION_FACTORY;
  private static Class<?> CLS_PARAM;

  private static Constructor<?> CNS_SQLSESSION_FACTORY_BEAN;
  private static Constructor<?> CNS_RESOURCE_PATTERN_RESOLVER;
  private static Constructor<?> CNS_JNDI_DATA_SOURCE_LOOKUP;
  // private static Constructor<?> CNS_DATA_SOURCE_TRANSACTION_MANAGER;
  private static Constructor<?> CNS_SQL_SESSION_TEMPLATE;

  private static Method MTD_GET_BEAN_FACTORY;
  private static Method MTD_GET_RESOURCE;
  private static Method MTD_GET_RESOURCES;
  private static Method MTD_SET_DATA_SOURCE;
  private static Method MTD_SET_CONFIG_LOCATION;
  private static Method MTD_GET_SQLSESSION_FACTORY;
  // private static Method MTD_REGISTER_SINGLETON;
  private static Method MTD_RESOURCE_GET_INPUT_STREAM;
  private static Method MTD_REGISTER_RESOLVABLE_DEPENDENCY;
  private static Method MTD_GET_JNDI_DATA_SOURCE;
  private static Method MTD_SET_TYPE_ALIASES;
  private static Method MTD_SET_TYPE_HANDLERS;
  private static Method MTD_SET_MAPPER_LOCATIONS;
  private static Method MTD_PARAM_VALUE;
  private static Method MTD_SELECT_LIST;
  private static Method MTD_SELECT_CURSOR;
  private static Method MTD_SELECT_ONE;
  private static Method MTD_UPDATE;
  private static Method MTD_INSERT;
  private static Method MTD_DELETE;

  static {
    try {
      CLS_SQLSESSION_FACTORY_BEAN = findClass("org.mybatis.spring.SqlSessionFactoryBean");
      CLS_ALIAS = findClass("org.apache.ibatis.type.Alias");
      CLS_TYPE_HANDLER = findClass("org.apache.ibatis.type.TypeHandler");
      CLS_RESOURCE = findClass("org.springframework.core.io.Resource");
      CLS_APPLICATION_CONTEXT = findClass("org.springframework.context.ApplicationContext");
      CLS_SQL_SESSION_FACTORY = findClass("org.apache.ibatis.session.SqlSessionFactory");
      CLS_PARAM = findClass("org.apache.ibatis.annotations.Param");

      Class<?> CLS_CONFIGURABLE_LISTABLE_BEAN_FACTORY = findClass("org.springframework.context.support.GenericApplicationContext");
      Class<?> CLS_RESOURCE_PATTERN_RESOLVER = findClass("org.springframework.core.io.support.ResourcePatternResolver");
      Class<?> CLS_PATH_MATCHING_RESOURCE_PATTERN_RESOLVER = findClass("org.springframework.core.io.support.PathMatchingResourcePatternResolver");
      Class<?> CLS_JNDI_DATA_SOURCE_LOOKUP = findClass("org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup");
      // Class<?> CLS_DATA_SOURCE_TRANSACTION_MANAGER = findClass("org.springframework.jdbc.datasource.DataSourceTransactionManager");
      Class<?> CLS_SQL_SESSION_TEMPLATE = findClass("org.mybatis.spring.SqlSessionTemplate");
      Class<?> CLS_DEFAULT_LISTABLE_BEAN_FACTORY = findClass("org.springframework.beans.factory.support.DefaultListableBeanFactory");

      CNS_SQLSESSION_FACTORY_BEAN = findConstructor(CLS_SQLSESSION_FACTORY_BEAN);
      CNS_RESOURCE_PATTERN_RESOLVER = findConstructor(CLS_PATH_MATCHING_RESOURCE_PATTERN_RESOLVER);
      CNS_JNDI_DATA_SOURCE_LOOKUP = findConstructor(CLS_JNDI_DATA_SOURCE_LOOKUP);
      // CNS_DATA_SOURCE_TRANSACTION_MANAGER = findConstructor(CLS_DATA_SOURCE_TRANSACTION_MANAGER, new Class[] { DataSource.class });
      CNS_SQL_SESSION_TEMPLATE = findConstructor(CLS_SQL_SESSION_TEMPLATE, new Class[] { CLS_SQL_SESSION_FACTORY });

      MTD_GET_BEAN_FACTORY = findMethod(CLS_CONFIGURABLE_LISTABLE_BEAN_FACTORY, "getBeanFactory", EMPTY_CLS);
      MTD_GET_RESOURCE = findMethod(CLS_APPLICATION_CONTEXT, "getResource", new Class[] { String.class });
      MTD_GET_RESOURCES = findMethod(CLS_RESOURCE_PATTERN_RESOLVER, "getResources", new Class[] { String.class });
      MTD_RESOURCE_GET_INPUT_STREAM = findMethod(CLS_RESOURCE, "getInputStream", EMPTY_CLS);
      MTD_SET_DATA_SOURCE = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setDataSource", new Class[] { DataSource.class });
      MTD_SET_CONFIG_LOCATION = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setConfigLocation", new Class[] { CLS_RESOURCE });
      MTD_GET_SQLSESSION_FACTORY = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "getObject", EMPTY_CLS);
      // MTD_REGISTER_SINGLETON = findMethod(CLS_DEFAULT_LISTABLE_BEAN_FACTORY, "registerSingleton", new Class[] { String.class, Object.class });
      MTD_REGISTER_RESOLVABLE_DEPENDENCY = findMethod(CLS_DEFAULT_LISTABLE_BEAN_FACTORY, "registerResolvableDependency", new Class[] { Class.class, Object.class });
      MTD_GET_JNDI_DATA_SOURCE = findMethod(CLS_JNDI_DATA_SOURCE_LOOKUP, "getDataSource", new Class[]{ String.class });
      MTD_SET_TYPE_ALIASES = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setTypeAliases");
      MTD_SET_TYPE_HANDLERS = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setTypeHandlers");
      MTD_SET_MAPPER_LOCATIONS = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setMapperLocations", new Class[] { newArray(CLS_RESOURCE, 0).getClass() });
      MTD_PARAM_VALUE = findMethod(CLS_PARAM, "value", EMPTY_CLS);
      MTD_SELECT_LIST = findMethod(CLS_SQL_SESSION_TEMPLATE, "selectList", new Class[] { String.class, Object.class });
      MTD_SELECT_CURSOR = findMethod(CLS_SQL_SESSION_TEMPLATE, "selectCursor", new Class[] { String.class, Object.class });
      MTD_SELECT_ONE = findMethod(CLS_SQL_SESSION_TEMPLATE, "selectOne", new Class[] { String.class, Object.class });
      MTD_UPDATE = findMethod(CLS_SQL_SESSION_TEMPLATE, "update", new Class[] { String.class, Object.class });
      MTD_INSERT = findMethod(CLS_SQL_SESSION_TEMPLATE, "insert", new Class[] { String.class, Object.class });
      MTD_DELETE = findMethod(CLS_SQL_SESSION_TEMPLATE, "delete", new Class[] { String.class, Object.class });
    } catch (Throwable e) {
      log.info("E:{}", e.getMessage());
    }
  }

  private static class MapperInfo {
    private String className;
    private Class<?> cls;
    private Map<String, String> methods = new LinkedHashMap<>();
    private Map<String, String[]> params = new LinkedHashMap<>();
  }

  public static DataSource getJndiDataSource(String jndiName) {
    DataSource ret = null;
    Object lookup = null;
    try {
      lookup = CNS_JNDI_DATA_SOURCE_LOOKUP.newInstance(EMPTY_OBJ);
    } catch (Exception e) {
      log.info("E:{}", e.getMessage());
      ret = null;
    }
    if (ret == null) {
      try {
        ret = cast(MTD_GET_JNDI_DATA_SOURCE.invoke(lookup, new Object[] { cat("java:/comp/env/jdbc/", jndiName) }), ret = null);
      } catch (Throwable e) {
        log.info("E: java:/comp/env/jdbc/{} NOT FOUND", jndiName, e.getMessage());
        ret = null;
      }
    }
    if (ret == null) {
      try {
        ret = cast(MTD_GET_JNDI_DATA_SOURCE.invoke(lookup, new Object[] { cat("java:/jdbc/", jndiName) }), ret = null);
      } catch (Throwable e) {
        log.info("E: java:/jdbc/{} NOT FOUND", jndiName, e.getMessage());
        ret = null;
      }
    }
    return ret;
  }

  public static final void applyTypeProcess(Object fb, ClassLoader loader, String[] pkgs) {
    if (!CLS_SQLSESSION_FACTORY_BEAN.isInstance(fb)) { throw new RuntimeException("SQL-SESSION-FACTORY NOT FOUND"); }
    final List<Class<?>> alsLst = new ArrayList<>();
    final List<Object> hndLst = new ArrayList<>();
    log.debug("APPLY-TYPE-PROCESS..");
    ClassWorker.workClasses(loader, cls -> {
      try {
        Annotation[] ans = cls.getAnnotations();
        for (Annotation an : ans) {
          Class<? extends Annotation> type = an.annotationType();
          if (CLS_ALIAS.isAssignableFrom(type)) {
            log.debug("FOUND TYPE ALIAS:{} / {}", cls, an);
            alsLst.add(cls);
          }
        }
        if (CLS_TYPE_HANDLER.isAssignableFrom(cls)) {
          log.debug("FOUND TYPE HANDLER:{}", cls);
          hndLst.add(cast(findConstructor(cls).newInstance(), CLS_TYPE_HANDLER));
        }
      } catch(Exception e) {
        log.info("E:", e);
      }
    }, pkgs);
    try {
      if (alsLst.size() > 0) {
        // log.debug("REGISTER-TYPE-ALIASES:{}{}", "", alsLst);
        MTD_SET_TYPE_ALIASES.invoke(fb, new Object[] { toArray(alsLst, Class.class) });
      }
    } catch (Exception e) {
      log.info("E:", e);
    }
    try {
      if (hndLst.size() > 0) {
        // log.debug("REGISTER-TYPE-HANDLERS:{}{}", "", hndLst);
        MTD_SET_TYPE_HANDLERS.invoke(fb, new Object[] { toArray(hndLst, CLS_TYPE_HANDLER) });
      }
    } catch (Exception e) {
      log.info("E:", e);
    }
  }

  public static <F, T> MybatisConfig<F, T> configMybatis(
    Class<?> selfCls,
    Map<String, Object> defaultPrm,
    String pthMyaatis, String ptnRsrc,
    String[] pkgs) {
    return configMybatis(selfCls,
      defaultPrm, pthMyaatis, ptnRsrc, pkgs, null);
  }
  public static <F, T> MybatisConfig<F, T> configMybatis(
    Class<?> selfCls,
    Map<String, Object> defaultPrm,
    String pthMyaatis, String ptnRsrc,
    String[] pkgs, Fn1at<String, String> xmltr) {
    MybatisConfig<F, T> ret = null;
    try {
      ClassLoader loader = selfCls.getClassLoader();
      Object qsFactoryBean = CNS_SQLSESSION_FACTORY_BEAN.newInstance(EMPTY_OBJ);
      Object resolver = CNS_RESOURCE_PATTERN_RESOLVER.newInstance(EMPTY_OBJ);
      Object resources = MTD_GET_RESOURCES.invoke(resolver, ptnRsrc);
      Object[] resourcesArray = cast(resources, resourcesArray = null);
      final List<MapperInfo> mapperList = new ArrayList<>();
      MTD_SET_MAPPER_LOCATIONS.invoke(qsFactoryBean, resources);
      for (Object resource : resourcesArray) {
        InputStream istream = null;
        final MapperInfo info = new MapperInfo();
        try {
          istream = cast(MTD_RESOURCE_GET_INPUT_STREAM.invoke(resource, EMPTY_OBJ), istream = null);
          String content = readAsString(istream);
          if (xmltr != null) {
            String tres = null;
            try {
              tres = xmltr.apply(content);
            } catch (Exception e) { log.debug("E:", e); }
            if (tres != null && !"".equals(tres)) { content = tres; }
          }
          parseXML(c -> { },
            content, (uri, lname, qname, depth, attr, ctx) -> {
            switch(cat(depth, qname)) {
            case "1mapper": {
              String clsName = xmlAttr(attr, "namespace");
              info.className = clsName;
            } break;
            case "2select": case "2update": case "2insert": case "2delete": {
              String key = xmlAttr(attr, "id");
              info.methods.put(key, qname);
            } break;
            default: }
          }, (uri, lname, qname, depth, ctx) -> {
          }, (ch, st, len, depth, ctx) -> {
          });
        } catch (Exception e) {
          log.debug("E:", e);
        } finally {
          safeclose(istream);
        }
        mapperList.add(info);
      }
      LOOP1: for (final MapperInfo info : mapperList) {
        try {
          /** SQL맵 생성 */
          Class<?> cls = info.cls = findClass(info.className, loader);
          LOOP2: for (Method method : cls.getMethods()) {
            String mname = method.getName();
            String qtype = info.methods.get(mname);
            if (qtype == null) { continue LOOP2; }
            Class<?> rtype = method.getReturnType();
            Annotation[][] anns = method.getParameterAnnotations();
            String[] params = new String[anns.length];
            for (int ainx = 0; ainx < anns.length; ainx++) {
              for (Annotation a : anns[ainx]) {
                if (CLS_PARAM.isInstance(a)) { params[ainx] = cast(MTD_PARAM_VALUE.invoke(a, EMPTY_OBJ), ""); }
              }
            }
            info.params.put(mname, params);
            if (List.class.isAssignableFrom(rtype) && "select".equals(qtype)) {
              info.methods.put(mname, "selectList");
            } else if (Iterable.class.isAssignableFrom(rtype) && "select".equals(qtype)) {
              info.methods.put(mname, "selectIter");
            }
            continue LOOP2;
          }
        } catch (Exception e) { log.info("E:", e); }
        continue LOOP1;
      }
      applyTypeProcess(qsFactoryBean, loader, pkgs);
      Object[] QSFC = new Object[1];
      Object[] QSTP = new Object[1];
      // Object[] QTRX = new Object[1];
      ret = new MybatisConfig<F, T> () {
        @Override public F getSqlFactory(Object appctx, DataSource source) {
          try {
            MTD_SET_DATA_SOURCE.invoke(qsFactoryBean, source);
            Object beanFactory = MTD_GET_BEAN_FACTORY.invoke(appctx, EMPTY_OBJ);
            log.debug("configSqlSession / {} / {}", appctx.getClass(), beanFactory.getClass());
            MTD_SET_CONFIG_LOCATION.invoke(qsFactoryBean, MTD_GET_RESOURCE.invoke(appctx, new Object[] { pthMyaatis }));
            QSFC[0] = MTD_GET_SQLSESSION_FACTORY.invoke(qsFactoryBean, EMPTY_OBJ);
            // /** SQL팩토리 등록 */
            // if (nameSqlfctr != null && !"".equals(nameSqlfctr)) {
            //   // log.debug("REGISTER-BEAN:{} / {}", nameSqlfctr, QSFC[0]);
            //   MTD_REGISTER_SINGLETON.invoke(beanFactory, new Object[] { nameSqlfctr, QSFC[0] });
            // }
            /** 트랜잭션 매니저 등록 */
            // QTRX[0] = CNS_DATA_SOURCE_TRANSACTION_MANAGER.newInstance(source);
            // if (nameSqltrnx != null && !"".equals(nameSqltrnx)) {
            //   log.debug("REGISTER-BEAN:{} / {}", nameSqltrnx);
            //   MTD_REGISTER_SINGLETON.invoke(beanFactory, new Object[] { nameSqltrnx, CNS_DATA_SOURCE_TRANSACTION_MANAGER.newInstance(source) });
            // }
            // /** SQL 템플릴 생성 */
            QSTP[0] = CNS_SQL_SESSION_TEMPLATE.newInstance(QSFC[0]);
            // /** SQL 템플릿 등록 */
            // if (nameSqltmpl != null && !"".equals(nameSqltmpl)) {
            //   log.debug("REGISTER-BEAN:{} / {}", nameSqltmpl, QSTP[0]);
            //   MTD_REGISTER_SINGLETON.invoke(beanFactory, new Object[] { nameSqltmpl, QSTP[0] });
            // }
          } catch (Exception e) {
            log.debug("E:", e);
          }
          F ret = cast(QSFC[0], ret = null);
          return ret;
        }
        @Override public T getSqlTemplate() {
          T ret = cast(QSTP[0], ret = null);
          return ret;
        }
        // @Override public Object getSqlTransaction() { return QTRX[0]; }
        @Override public void registMappers(Object appctx) {
          Object beanFactory = null;
          LOOP1: for (final MapperInfo info : mapperList) {
            try {
              if (beanFactory == null) { beanFactory = MTD_GET_BEAN_FACTORY.invoke(appctx, EMPTY_OBJ); }
              /** SQL맵 생성 */
              Object inst = new Object();
              Class<?> cls = info.cls;
              Object bean = Proxy.newProxyInstance(cls.getClassLoader(), array(cls), (prx, mtd, arg) -> {
                String mname = mtd.getName();
                switch (mname) {
                case "toString": { return cat(cls.getName(), inst.toString()); }
                case "equals": { return inst.equals(arg[0]); }
                default: }
                String ns = cat(info.className, ".", mname);
                Map<String, Object> pmap = new LinkedHashMap<>();
                String qtype = info.methods.get(mname);
                String[] pnames = info.params.get(mname);
                if (qtype == null) { return null; }
                if (defaultPrm != null) { pmap.putAll(defaultPrm); }
                for (int inx = 0; pnames != null && inx < pnames.length && inx < arg.length; inx++) { pmap.put(pnames[inx], arg[inx]); }
                Object res = null;
                for (int retry = 0; retry < 10; retry++) {
                  if (QSTP[0] != null) { break; }
                  sleep(200);
                }
                switch (qtype) {
                case "selectList": { res = MTD_SELECT_LIST.invoke(QSTP[0], new Object[] { ns, pmap }); } break;
                case "selectIter": { res = MTD_SELECT_CURSOR.invoke(QSTP[0], new Object[] { ns, pmap }); } break;
                case "select": { res = MTD_SELECT_ONE.invoke(QSTP[0], new Object[] { ns, pmap }); } break;
                case "update": { res = MTD_UPDATE.invoke(QSTP[0], new Object[] { ns, pmap }); } break;
                case "insert": { res = MTD_INSERT.invoke(QSTP[0], new Object[] { ns, pmap }); } break;
                case "delete": { res = MTD_DELETE.invoke(QSTP[0], new Object[] { ns, pmap }); } break;
                default: }
                return res;
              });
              /** SQL맵 등록 */
              log.debug("REGISTER-BEAN:{} / {}", cls, bean);
              MTD_REGISTER_RESOLVABLE_DEPENDENCY.invoke(beanFactory, new Object[] { cls, bean });
            } catch (Exception e) { log.info("E:", e); }
            continue LOOP1;
          }
        }
      };
      // ret = (appctx, source) -> { };
    } catch (Exception e) {
      log.debug("E:", e);
    }
    return ret;
  }

  public static interface MybatisConfig<F, T> {
    public F getSqlFactory(Object appctx, DataSource source);
    // public Object getSqlTransaction();
    public T getSqlTemplate();
    public void registMappers(Object appctx);
  }
}
