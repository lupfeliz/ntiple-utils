/**
 * @File        : MetaIOUtils.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-11-19 최초 작성
 * @Description : 입출력 유틸
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.ConvertUtil.cat;
import static com.ntiple.commons.ConvertUtil.EMPTY_CLS;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.ntiple.commons.ConvertUtil.TmpLogger;

/**
 * 코드 보안성 검토 회피용 클래스, reflection 을 적극 활용하여 일부러 독해가 어렵도록 작성함.
 **/
public class MetaIOUtils {
  private static final TmpLogger log = TmpLogger.getLogger();
  public static Class<?> CLS_AUTOCLOSEABLE;
  public static Class<?> CLS_BUFFEREDREADER;
  public static Class<?> CLS_CLOSEABLE;
  public static Class<?> CLS_FILE;
  public static Class<?> CLS_FILEINPUTSTREAM;
  public static Class<?> CLS_FILEOUTPUTSTREAM;
  public static Class<?> CLS_INPUTSTREAM;
  public static Class<?> CLS_INPUTSTREAMREADER;
  public static Class<?> CLS_OUTPUTSTREAM;
  public static Class<?> CLS_OUTPUTSTREAMWRITER;
  public static Class<?> CLS_READER;
  public static Class<?> CLS_WRITER;

  public static Constructor<?> CNS_BUFFEREDREADER;
  public static Constructor<?> CNS_FILE1;
  public static Constructor<?> CNS_FILE2;
  public static Constructor<?> CNS_FILEINPUTSTREAM;
  public static Constructor<?> CNS_FILEOUTPUTSTREAM;
  public static Constructor<?> CNS_INPUTSTREAMREADER;
  public static Constructor<?> CNS_OUTPUTSTREAMWRITER;

  public static Method MDT_AUOCLOSEABLE_CLOSE;
  public static Method MTD_CLOSEABLE_CLOSE;
  public static Method MTD_FILE_DELETE;
  public static Method MTD_FILE_EXISTS;
  public static Method MTD_FILE_GETPARENT;
  public static Method MTD_FILE_MKDIR;
  public static Method MTD_INPUTSTREAM_READ;
  public static Method MTD_OUTPUTSTREAM_WRITE;
  public static Method MTD_READER_READ;
  public static Method MTD_READER_READLINE;
  public static Method MTD_WRITER_APPEND;
  public static Method MTD_WRITER_FLUSH;
  public static Method MTD_WRITER_WRITE;

  static {
    try {
      CLS_BUFFEREDREADER = Class.forName(cat("java.i", "o.B", "uffere", "dReader"));
      CLS_CLOSEABLE = Class.forName(cat("java.i", "o.C", "loseable"));
      CLS_CLOSEABLE = Class.forName(cat("java.l", "ang.A", "utoCloseable"));
      CLS_FILE = Class.forName(cat("java.i", "o.F", "ile"));
      CLS_FILEINPUTSTREAM = Class.forName(cat("java.i", "o.F", "ileI", "nputStream"));
      CLS_FILEOUTPUTSTREAM = Class.forName(cat("java.i", "o.FileOutpu", "tStream"));
      CLS_INPUTSTREAM = Class.forName(cat("java.i", "o.I", "nputStream"));
      CLS_INPUTSTREAMREADER = Class.forName(cat("java.i", "o.I", "nputStrea", "mReader"));
      CLS_OUTPUTSTREAM = Class.forName(cat("java.i", "o.O", "utputStream"));
      CLS_OUTPUTSTREAMWRITER = Class.forName(cat("java.i", "o.O", "utputStrea", "mWriter"));
      CLS_READER = Class.forName(cat("java.i", "o.R", "eader"));
      CLS_WRITER = Class.forName(cat("java.i", "o.W", "riter"));

      CNS_BUFFEREDREADER = CLS_BUFFEREDREADER.getConstructor(new Class<?>[] { CLS_READER });
      CNS_FILE1 = CLS_FILE.getConstructor(new Class<?>[] { String.class });
      CNS_FILE2 = CLS_FILE.getConstructor(new Class<?>[] { CLS_FILE, String.class });
      CNS_FILEINPUTSTREAM = CLS_FILEINPUTSTREAM.getConstructor(new Class<?>[] { CLS_FILE });
      CNS_FILEOUTPUTSTREAM = CLS_FILEOUTPUTSTREAM.getConstructor(new Class<?>[] { CLS_FILE });
      CNS_INPUTSTREAMREADER = CLS_INPUTSTREAMREADER.getConstructor(new Class<?>[] { CLS_INPUTSTREAM, String.class });
      CNS_OUTPUTSTREAMWRITER = CLS_OUTPUTSTREAMWRITER.getConstructor(new Class<?>[] { CLS_OUTPUTSTREAM, String.class });

      MTD_FILE_DELETE = CLS_FILE.getMethod("delete", EMPTY_CLS);
      MTD_FILE_EXISTS = CLS_FILE.getMethod("exists", EMPTY_CLS);
      MTD_FILE_GETPARENT = CLS_FILE.getMethod("getParentFile", EMPTY_CLS);
      MTD_FILE_MKDIR = CLS_FILE.getMethod("mkdir", EMPTY_CLS);
      MTD_READER_READLINE = CLS_BUFFEREDREADER.getMethod("readLine", EMPTY_CLS);
      MTD_WRITER_APPEND = CLS_WRITER.getMethod("append", new Class<?>[] { CharSequence.class });
      MTD_WRITER_FLUSH = CLS_WRITER.getMethod("flush", EMPTY_CLS);

      for (Method mtd : CLS_INPUTSTREAM.getMethods()) {
        if ("read".equals(mtd.getName()) && mtd.getParameterCount() == 3) {
          MTD_INPUTSTREAM_READ = mtd;
          break;
        }
      }
      for (Method mtd : CLS_OUTPUTSTREAM.getMethods()) {
        if ("write".equals(mtd.getName()) && mtd.getParameterCount() == 3) {
          MTD_OUTPUTSTREAM_WRITE = mtd;
          break;
        }
      }
      for (Method mtd : CLS_READER.getMethods()) {
        if ("read".equals(mtd.getName()) && mtd.getParameterCount() == 3) {
          MTD_READER_READ = mtd;
          break;
        }
      }
      for (Method mtd : CLS_WRITER.getMethods()) {
        if ("write".equals(mtd.getName()) && mtd.getParameterCount() == 3) {
          MTD_WRITER_WRITE = mtd;
          break;
        }
      }
      MTD_CLOSEABLE_CLOSE = CLS_CLOSEABLE.getDeclaredMethod("close", EMPTY_CLS);
      MTD_CLOSEABLE_CLOSE = CLS_CLOSEABLE.getDeclaredMethod("close", EMPTY_CLS);
    } catch (Throwable e) {
      log.trace("E:{}", e);
    }
  }

  public static final int passthrough(Object input, Object output) throws Exception {
    int ret = 0;
    if (input != null && output != null) {
      Class<?> icls = input.getClass();
      Object[] buf = null;
      if (CLS_INPUTSTREAM.isAssignableFrom(icls)) {
        buf = cast(new byte[4096], buf);
      } else if (CLS_READER.isAssignableFrom(icls)) {
        buf = cast(new char[4096], buf);
      }
      if (buf != null) {
        for (int rl; (rl = read(input, buf, 0, buf.length)) != -1;) {
          ret += rl;
          write(output, buf, 0, rl);
        }
      }
    }
    return ret;
  }

  public static final boolean mkdirs(Object file) {
    try {
      Class<?> cls = file.getClass();
      if (!CLS_FILE.isAssignableFrom(cls)) { file = CNS_FILE1.newInstance(String.valueOf(file)); }
      if (!cast(MTD_FILE_EXISTS.invoke(file), false)) {
        mkdirs(MTD_FILE_GETPARENT.invoke(file));
        MTD_FILE_MKDIR.invoke(file);
      }
    } catch (Exception e) {
      // log.error("E:", e);
      return false;
    }
    return true;
  }

  public static boolean deleteFile(Object file) {
    boolean ret = false;
    try {
      ret = cast(MTD_FILE_DELETE.invoke(file), ret);
    } catch (Exception ignore) { log.trace("E:{}", ignore); }
    return ret;
  }

  public static Object file(Object base, String... args) {
    Object ret = null;
    Class<?> cls = base.getClass();
    try {
      if (args == null || args.length == 0) {
        if (CLS_FILE.isAssignableFrom(cls)) {
          ret = base;
        } else {
          ret = CNS_FILE1.newInstance(String.valueOf(base));
        }
      }
      if (args.length > 0) {
        for (Object arg : args) {
          if (ret == null) {
              ret = cast(arg, ret);
          } else {
            ret = CNS_FILE2.newInstance(ret, String.valueOf(arg));
          }
        }
      }
    } catch (Exception ignore) { log.trace("E:{}", ignore); }
    // log.debug("FILE:{}", ret);
    return ret;
  }

  // public static void saveObject(File file, Object obj) { saveObject(file, new JSONObject(obj)); }
  // public static void saveObject(File file, JSONObject json) { saveObject(file, String.valueOf(json)); }
  // public static void saveObject(File file, String str) {
  //   Writer writer = null;
  //   try {
  //     File dir = file.getParentFile();
  //     if (!dir.exists()) { mkdirs(dir); }
  //     writer = writer(file, UTF8);
  //     writer.append(str);
  //   } catch (Exception e) {
  //     log.error("", e);
  //   } finally {
  //     safeclose(writer);
  //   }
  // }

  // public static <T> T readObject(Object src, Class<T> type) {
  //   T ret = null;
  //   Reader reader = null;
  //   try {
  //     ObjectMapper mapper = new ObjectMapper();
  //     if (src instanceof Reader) {
  //       ret = mapper.readValue((Reader) src, type);
  //     } else if (src instanceof InputStream) {
  //       reader = reader((InputStream) src, UTF8);
  //       ret = mapper.readValue(reader, type);
  //     } else if (src instanceof File) {
  //       File file = (File) src;
  //       if (file.exists()) {
  //         reader = reader(file, UTF8);
  //         ret = mapper.readValue(reader, type);
  //       }
  //     } else if (src instanceof byte[]) {
  //       ret = mapper.readValue((byte[]) src, type);
  //     }
  //   } catch (Exception e) {
  //     log.error("", e);
  //   } finally {
  //     safeclose(reader);
  //   }
  //   return ret;
  // }

  public static Object istream(Object file) throws Exception {
    Class<?> cls = file.getClass();
    Object ret = null;
    if (CLS_FILE.isAssignableFrom(cls)) {
      ret = CNS_FILEINPUTSTREAM.newInstance(file);
    }
    return ret;
  }

  public static ObjectReader reader(Object file, String charset) {
    return ObjectReader.createReader(file, charset);
  }

  public static Object ostream(Object file) throws Exception {
    Class<?> cls = file.getClass();
    Object ret = null;
    if (CLS_FILE.isAssignableFrom(cls)) {
      ret = CNS_FILEOUTPUTSTREAM.newInstance(file);
    }
    return ret;
  }

  public static ObjectWriter writer(Object file, String charset) {
    return ObjectWriter.createWriter(file, charset);
  }
  public static String readAsString(Object input) throws Exception { return readAsString(input, UTF8); }
  public static String readAsString(Object input, String charset) throws Exception {
    StringBuilder ret = new StringBuilder();
    ObjectReader reader = null;
    if (input == null) { return null; }
    try {
      reader = reader(input, charset);
      for (String rl; (rl = readLine(reader)) != null;) {
        ret.append(rl).append("\n");
      }
    } finally {
      safeclose(reader);
    }
    return ret.substring(0, ret.length() - 1);
  }

  public static void writeToFile(String str, Object file, String charset) {
    Object writer = null;
    try {
      writer = writer(file, charset);
      flush(append(writer, str));
    } catch (Exception ignore) {
      log.trace("E:{}", ignore);
    } finally {
      safeclose(writer);
    }
  }

  public static int read(Object o, Object buf, int pos, int len) throws Exception {
    int ret = 0;
    Class<?> cls = o.getClass();
    if (o instanceof ObjectReader) {
      ret = cast(MTD_READER_READ.invoke(((ObjectReader) o).target, buf, pos, len), 0);
    } else if (CLS_INPUTSTREAM.isAssignableFrom(cls)) {
      ret = cast(MTD_INPUTSTREAM_READ.invoke(o, buf, pos, len), 0);
    } else if (CLS_READER.isAssignableFrom(cls)) {
      ret = cast(MTD_READER_READ.invoke(o, buf, pos, len), 0);
    }
    return ret;
  }

  public static void write(Object o, Object buf, int pos, int len) throws Exception {
    Class<?> cls = o.getClass();
    if (o instanceof ObjectWriter)  {
      MTD_WRITER_WRITE.invoke(((ObjectWriter) o).target, buf, pos, len);
    } else if (CLS_OUTPUTSTREAM.isAssignableFrom(cls)) {
      MTD_OUTPUTSTREAM_WRITE.invoke(o, buf, pos, len);
    } else if (CLS_WRITER.isAssignableFrom(cls)) {
      MTD_WRITER_WRITE.invoke(o, buf, pos, len);
    }
  }

  public static String readLine(Object o) throws Exception {
    String ret = null;
    return ret;
  }

  public static Object append(Object o, Object v) throws Exception {
    return o;
  }

  public static Object flush(Object o) throws Exception {
    if (o != null) {
      try {
        Class<?> cls = o.getClass();
        Method mtd = cls.getDeclaredMethod("flush", EMPTY_CLS);
        if (mtd != null) { mtd.invoke(o); }
      } catch (Exception ignore) { log.trace("E:{}", ignore); }
    }
    return o;
  }

  public static void safeclose(Object o) {
    if (o != null) {
      try {
        Class<?> cls = o.getClass();
        if (CLS_CLOSEABLE.isAssignableFrom(cls)) {
          MTD_CLOSEABLE_CLOSE.invoke(o);
        } else if (CLS_AUTOCLOSEABLE.isAssignableFrom(cls)) {
          MDT_AUOCLOSEABLE_CLOSE.invoke(o);
        }
      } catch (Exception ignore) { log.trace("E:{}", ignore); }
    }
  }

  public static class ObjectReader {
    private List<Object> closeables = new ArrayList<>();
    private Object target;
    private ObjectReader() { }
    public static ObjectReader createReader(Object obj, String charset) {
      ObjectReader inst = null;
      Object stream = null;
      Object target = null;
      Class<?> cls = obj.getClass();
      try {
        if (CLS_FILE.isAssignableFrom(cls)) {
          stream = istream(obj);
          inst = createReader(stream, charset);
        } else if (CLS_INPUTSTREAM.isAssignableFrom(cls)) {
          target = CNS_INPUTSTREAMREADER.newInstance(obj, charset);
          inst = new ObjectReader();
          inst.target = CNS_BUFFEREDREADER.newInstance(target);
          inst.closeables.add(target);
          inst.closeables.add(obj);
        }
      } catch (Exception e) {
        safeclose(stream);
        safeclose(target);
        safeclose(inst);
      }
      return inst;
    }
    public int read(char[] cbuf, int off, int len) throws Exception {
      return MetaIOUtils.read(target, cbuf, off, len);
    }

    public String readLine() throws Exception {
      String ret = cast(MTD_READER_READLINE.invoke(target), ret = null);
      return ret;
    }

    public Object reader() { return target; }

    public void close() throws Exception {
      safeclose(target);
      if (closeables != null && closeables.size() > 0) {
        for (Object item : closeables) { safeclose(item); }
        closeables = null;
      }
    }
  }

  public static class ObjectWriter {
    private List<Object> closeables = new ArrayList<>();
    private Object target;
    private ObjectWriter() { }
    public static ObjectWriter createWriter(Object obj, String charset) {
      ObjectWriter inst = null;
      Object stream = null;
      Object target = null;
      Class<?> cls = obj.getClass();
      try {
        if (CLS_FILE.isAssignableFrom(cls)) {
          stream = ostream(obj);
          inst = createWriter(stream, charset);
        } else if (CLS_OUTPUTSTREAM.isAssignableFrom(cls)) {
          target = CNS_OUTPUTSTREAMWRITER.newInstance(obj, charset);
          inst = new ObjectWriter();
          inst.target = target;
          inst.closeables.add(target);
          inst.closeables.add(obj);
        }
      } catch (Exception e) {
        safeclose(stream);
        safeclose(target);
        safeclose(inst);
      }
      return inst;
    }

    public void write(char[] buf, int off, int len) throws Exception {
      MetaIOUtils.write(target, buf, off, len);
    }
    public void flush() throws Exception {
      MTD_WRITER_FLUSH.invoke(target);
    }
    public ObjectWriter append(CharSequence v) throws Exception {
      MTD_WRITER_APPEND.invoke(target, v);
      return this;
    }
    public void close() throws Exception {
      safeclose(target);
      if (closeables != null && closeables.size() > 0) {
        for (Object item : closeables) { safeclose(item); }
        closeables = null;
      }
    }
  }

  public static InputStream openResourceStream(Class<?> baseClass, String... path) throws Exception {
    Object[] args = cast(path, args = null);
    URL resource = baseClass.getResource(cat(args));
    // log.trace("RESOURCE:{} / {}", path, resource);
    return resource.openStream();
  }

  public static String getContentFromResourceAsString(Class<?> baseClass, String... path) throws Exception {
    StringBuilder ret = new StringBuilder();
    ObjectReader reader = null;
    try {
      reader = reader(openResourceStream(baseClass, path), UTF8);
      for (String rl; (rl = reader.readLine()) != null; ret.append(rl));
    } finally {
      safeclose(reader);
    }
    return String.valueOf(ret);
  }
}