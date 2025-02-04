/**
 * @File        : MybatisUtil.java
 * @Author      : 정재백
 * @Since       : 2025-02-03
 * @Description : mybatis 설정유틸
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ConvertUtil.array;
import static com.ntiple.commons.IOUtil.safeclose;
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

public class MybatisConfigUtil {
  private static final SimpleLogger log = SimpleLogger.getLogger();

  private static Class<?> CLS_SQLSESSION_FACTORY_BEAN;
  private static Class<?> CLS_ALIAS;
  private static Class<?> CLS_TYPE_HANDLER;
  private static Class<?> CLS_CONFIGURABLE_LISTABLE_BEAN_FACTORY;
  private static Class<?> CLS_RESOURCE;
  private static Class<?> CLS_APPLICATION_CONTEXT;
  private static Class<?> CLS_SQL_SESSION_FACTORY;

  private static Constructor<?> CNS_SQLSESSION_FACTORY_BEAN;
  private static Constructor<?> CNS_RESOURCE_PATTERN_RESOLVER;
  private static Constructor<?> CNS_JNDI_DATA_SOURCE_LOOKUP;

  private static Method MTD_GET_BEAN_FACTORY;
  private static Method MTD_GET_RESOURCE;
  private static Method MTD_GET_RESOURCES;
  private static Method MTD_SET_DATA_SOURCE;
  private static Method MTD_SET_CONFIG_LOCATION;
  private static Method MTD_GET_SQLSESSION_FACTORY;
  private static Method MTD_RESOURCE_GET_INPUT_STREAM;
  private static Method MTD_REGISTER_SINGLETON;
  private static Method MTD_REGISTER_RESOLVABLE_DEPENDENCY;
  private static Method MTD_GET_JNDI_DATA_SOURCE;
  private static Method MTD_SET_TYPE_ALIASES;
  private static Method MTD_SET_TYPE_HANDLERS;
  private static Method MTD_SET_MAPPER_LOCATIONS;

  static {
    try {
      CLS_SQLSESSION_FACTORY_BEAN = findClass("org.mybatis.spring.SqlSessionFactoryBean");
      CLS_ALIAS = findClass("org.apache.ibatis.type.Alias");
      CLS_TYPE_HANDLER = findClass("org.apache.ibatis.type.TypeHandler");
      CLS_CONFIGURABLE_LISTABLE_BEAN_FACTORY = findClass("org.springframework.beans.factory.config.ConfigurableListableBeanFactory");
      CLS_RESOURCE = findClass("org.springframework.core.io.Resource");
      CLS_APPLICATION_CONTEXT = findClass("org.springframework.context.ApplicationContext");
      CLS_SQL_SESSION_FACTORY = findClass("org.apache.ibatis.session.SqlSessionFactory");

      Class<?> CLS_RESOURCE_PATTERN_RESOLVER = findClass("org.springframework.core.io.support.ResourcePatternResolver");
      Class<?> CLS_PATH_MATCHING_RESOURCE_PATTERN_RESOLVER = findClass("org.springframework.core.io.support.PathMatchingResourcePatternResolver");
      Class<?> CLS_JNDI_DATA_SOURCE_LOOKUP = findClass("org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup");

      CNS_SQLSESSION_FACTORY_BEAN = findConstructor(CLS_SQLSESSION_FACTORY_BEAN);
      CNS_RESOURCE_PATTERN_RESOLVER = findConstructor(CLS_PATH_MATCHING_RESOURCE_PATTERN_RESOLVER);
      CNS_JNDI_DATA_SOURCE_LOOKUP = findConstructor(CLS_JNDI_DATA_SOURCE_LOOKUP);

      MTD_GET_BEAN_FACTORY = findMethod(CLS_CONFIGURABLE_LISTABLE_BEAN_FACTORY, "getBeanFactory", EMPTY_CLS);
      MTD_GET_RESOURCE = findMethod(CLS_APPLICATION_CONTEXT, "getResource", new Class[] { String.class });
      MTD_GET_RESOURCES = findMethod(CLS_RESOURCE_PATTERN_RESOLVER, "getResources", new Class[] { String.class });
      MTD_RESOURCE_GET_INPUT_STREAM = findMethod(CLS_RESOURCE, "getInputStream", EMPTY_CLS);
      MTD_SET_DATA_SOURCE = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setDataSource", new Class[] { DataSource.class });
      MTD_SET_CONFIG_LOCATION = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setConfigLocation", new Class[] { CLS_RESOURCE });
      MTD_GET_SQLSESSION_FACTORY = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "getObject", EMPTY_CLS);
      MTD_REGISTER_SINGLETON = findMethod(CLS_CONFIGURABLE_LISTABLE_BEAN_FACTORY, "registerSingleton", new Class[] { String.class, Object.class });
      MTD_REGISTER_RESOLVABLE_DEPENDENCY = findMethod(CLS_CONFIGURABLE_LISTABLE_BEAN_FACTORY, "registerResolvableDependency", new Class[] { Class.class, Object.class });
      MTD_GET_JNDI_DATA_SOURCE = findMethod(CLS_JNDI_DATA_SOURCE_LOOKUP, "getDataSource", new Class[]{ String.class });
      MTD_SET_TYPE_ALIASES = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setTypeAliases");
      MTD_SET_TYPE_HANDLERS = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setTypeHandlers");
      MTD_SET_MAPPER_LOCATIONS = findMethod(CLS_SQLSESSION_FACTORY_BEAN, "setMapperLocations", new Class[] { newArray(CLS_RESOURCE, 0).getClass() });
    } catch (Throwable e) {
      log.info("E:{}", e.getMessage());
    }
  }

  private static class MapperInfo {
    private String className;
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
    }
    if (ret == null) {
      try {
        ret = cast(MTD_GET_JNDI_DATA_SOURCE.invoke(lookup, new Object[] { cat("java:/comp/env/jdbc/", jndiName) }), ret = null);
      } catch (Exception e) {
        log.info("E: java:/comp/env/jdbc/{} NOT FOUND", jndiName, e.getMessage());
      }
    }
    if (ret == null) {
      try {
        ret = cast(MTD_GET_JNDI_DATA_SOURCE.invoke(lookup, new Object[] { cat("java:/jdbc/", jndiName) }), ret = null);
      } catch (Exception e) {
        log.info("E: java:/jdbc/{} NOT FOUND", jndiName, e.getMessage());
      }
    }
    return ret;
  }

  public static final void applyTypeProcess(Object fb, ClassLoader loader, String... pkgs) {
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
        log.debug("REGISTER-TYPE-ALIASES:{}{}", "", alsLst);
        MTD_SET_TYPE_ALIASES.invoke(fb, new Object[] { toArray(alsLst, Class.class) });
      }
    } catch (Exception e) {
      log.info("E:", e);
    }
    try {
      if (hndLst.size() > 0) {
        log.debug("REGISTER-TYPE-HANDLERS:{}{}", "", hndLst);
        MTD_SET_TYPE_HANDLERS.invoke(fb, new Object[] { toArray(hndLst, CLS_TYPE_HANDLER) });
      }
    } catch (Exception e) {
      log.info("E:", e);
    }
  }

  public static Object configSqlSession(DataSource source,
    Object appctx,
    String nameDatasrc,
    String nameSqlfctr,
    String nameSqltmpl,
    String nameSqltrnx,
    String pthMyaatis,
    String ptnRsrc, String... pkgs) throws Exception {
    // log.debug("================================================================================");
    // log.debug("configSqlSession");
    Object beanFactory = MTD_GET_BEAN_FACTORY.invoke(appctx, EMPTY_OBJ);
    Object qsFactoryBean = CNS_SQLSESSION_FACTORY_BEAN.newInstance(EMPTY_OBJ);
    MTD_SET_DATA_SOURCE.invoke(qsFactoryBean, source);
    MTD_SET_CONFIG_LOCATION.invoke(qsFactoryBean, MTD_GET_RESOURCE.invoke(appctx, new Object[] { pthMyaatis }));
    Object resolver = CNS_RESOURCE_PATTERN_RESOLVER.newInstance(EMPTY_OBJ);
    // log.debug("CHECK-RESOURCES:{}{}", "", resolver.getResources("com/ntiple/work/**/*.class"));
    Object resources = MTD_GET_RESOURCES.invoke(resolver, ptnRsrc);
    Object[] resourcesArray = cast(resources, resourcesArray = null);
    final List<MapperInfo> mapperList = new ArrayList<>();
    MTD_SET_MAPPER_LOCATIONS.invoke(qsFactoryBean, resources);
    // qsFactoryBean.setMapperLocations(resources);
    for (Object resource : resourcesArray) {
      InputStream istream = null;
      final MapperInfo info = new MapperInfo();
      try {
        istream = cast(MTD_RESOURCE_GET_INPUT_STREAM.invoke(resource, EMPTY_OBJ), istream = null);
        parseXML(c -> { },
          istream, (uri, lname, qname, depth, attr, ctx) -> {
          switch(cat(depth, qname)) {
          case "1mapper": {
            String clsName = xmlAttr(attr, "namespace");
            info.className = clsName;
            // log.debug("MAPPER FOUND:{}", info.className);
          } break;
          case "2select": case "2update": case "2insert": case "2delete": {
            String key = xmlAttr(attr, "id");
            info.methods.put(key, qname);
            // log.debug("MAPPER-METHOD FOUND:{} / {}", qname, key);
          } break;
          default: }
        }, (uri, lname, qname, depth, ctx) -> {
        }, (ch, st, len, depth, ctx) -> {
        });
      } finally { safeclose(istream); }
      mapperList.add(info);
      // log.debug("INFO:{}", info);
    }
    applyTypeProcess(qsFactoryBean, source.getClass().getClassLoader(), pkgs);
    Object qsfc = MTD_GET_SQLSESSION_FACTORY.invoke(qsFactoryBean, EMPTY_OBJ);
    {
      /** 트랜잭션 매니저 등록 */
    //   beanFactory.registerSingleton(nameSqltrnx, new DataSourceTransactionManager(source));
      /** SQL 템플릴 생성 */
    //   SqlSessionTemplate qstp = new SqlSessionTemplate(qsfc);
      // log.debug("INFO:{}", mapperList);
      LOOP1: for (final MapperInfo info : mapperList) {
        try {
          /** SQL맵 생성 */
          Object bean = null;
          Class<?> cls = findClass(info.className);
          // log.debug("MAPPER-CLASS:{}", cls, bean);
          LOOP2: for (Method method : cls.getMethods()) {
            String mname = method.getName();
            String qtype = info.methods.get(mname);
            if (qtype == null) { continue LOOP2; }
            Class<?> rtype = method.getReturnType();
            Annotation[][] anns = method.getParameterAnnotations();
            String[] params = new String[anns.length];
            for (int ainx = 0; ainx < anns.length; ainx++) {
              for (Annotation a : anns[ainx]) {
                // if (a instanceof Param) { params[ainx] = ((Param) a).value(); }
              }
            }
            info.params.put(mname, params);
            if (List.class.isAssignableFrom(rtype) && "select".equals(qtype)) {
              info.methods.put(mname, "selectList");
            } else if (Iterable.class.isAssignableFrom(rtype) && "select".equals(qtype)) {
              info.methods.put(mname, "selectIter");
            }
            // log.debug("METHOD:{} / {} / {}",
            //   mname,
            //   info.methods.get(mname), 
            //   info.params.get(mname));
            continue LOOP2;
          }
          bean = Proxy.newProxyInstance(cls.getClassLoader(), array(cls), (prx, mtd, arg) -> {
            Object self = Thread.currentThread();
            String mname = mtd.getName();
            // log.debug("EXECUTE:{} / {}", cls, mname);
            switch (mname) {
            case "toString": { return self.toString(); }
            case "equals": { return self.equals(arg[0]); }
            default: }
            String ns = cat(info.className, ".", mname);
            Map<String, Object> pmap = new LinkedHashMap<>();
            String qtype = info.methods.get(mname);
            String[] pnames = info.params.get(mname);
            if (qtype == null) { return null; }
            for (int inx = 0; pnames != null && inx < pnames.length && inx < arg.length; inx++) { pmap.put(pnames[inx], arg[inx]); }
            Object res = null;
            switch (qtype) {
    //         case "selectList": { res = qstp.selectList(ns, pmap); } break;
    //         case "selectIter": { res = qstp.selectCursor(ns, pmap); } break;
    //         case "select": { res = qstp.selectOne(ns, pmap); } break;
    //         case "update": { res = qstp.update(ns, pmap); } break;
    //         case "insert": { res = qstp.insert(ns, pmap); } break;
    //         case "delete": { res = qstp.delete(ns, pmap); } break;
            default: }
            return res;
          });
          // log.debug("CHECK:{} / {} / {}", cls.isInstance(bean), bean, bean.getClass());
          /** SQL맵 등록 */
          log.debug("REGISTER-BEAN:{} / {}", cls, bean);
    //       beanFactory.registerResolvableDependency(cls, bean);
        } catch (Exception e) { log.info("E:", e); }
        continue LOOP1;
      }
      /** SQL 템플릴 등록 */
    //   beanFactory.registerSingleton(nameSqltmpl, qstp);
    }
    return qsfc;
  }
}
