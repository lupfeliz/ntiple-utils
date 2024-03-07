/**
 * @File        : IOUtils.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2023-11-19 최초 작성
 * @Description : 입출력 유틸
 **/
package com.ntiple.commons;

import static com.ntiple.commons.Constants.UTF8;
import static com.ntiple.commons.ConvertUtil.cast;
import static com.ntiple.commons.ConvertUtil.cat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// import org.json.JSONObject;

// import com.fasterxml.jackson.databind.ObjectMapper;

// import lombok.extern.slf4j.Slf4j;

public class IOUtils {
  public static final int passthrough(InputStream istream, OutputStream ostream) throws Exception {
    int ret = 0;
    if (istream != null && ostream != null) {
      byte[] buf = new byte[4096];
      for (int rl; (rl = istream.read(buf, 0, buf.length)) != -1;) {
        ret += rl;
        ostream.write(buf, 0, rl);
      }
    }
    return ret;
  }

  public static final boolean mkdirs(File file) {
    try {
      if (!file.exists()) {
        mkdirs(file.getParentFile());
        file.mkdir();
      }
    } catch (Exception e) {
      // log.error("E:", e);
      return false;
    }
    return true;
  }

  public static boolean deleteFile(File file) {
    boolean ret = false;
    try {
      ret = file.delete();
    } catch (Exception ignore) { }
    return ret;
  }

  public static File file(Object base, String... args) {
    File ret = null;
    if (ret == null) {
      if (base instanceof File) {
        ret = cast(base, ret);
      } else {
        ret = new File(String.valueOf(base));
      }
    }
    if (args.length > 0) {
      for (Object arg : args) {
        if (ret == null) {
            ret = cast(arg, ret);
        } else {
          ret = new File(ret, String.valueOf(arg));
        }
      }
    }
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

  public static InputStream istream(File file) throws Exception {
    return new FileInputStream(file);
  }

  public static BufferedReader reader(File file, String charset) {
    return BufferedReaderWrapper.createReader(file, charset);
  }

  public static BufferedReader reader(InputStream istream, String charset) {
    return BufferedReaderWrapper.createReader(istream, charset);
  }

  public static OutputStream ostream(File file) throws Exception {
    return new FileOutputStream(file);
  }

  public static BufferedWriter writer(File file, String charset) {
    return BufferedWriterWrapper.createWriter(file, charset);
  }

  public static BufferedWriter writer(OutputStream ostream, String charset) {
    return BufferedWriterWrapper.createWriter(ostream, charset);
  }

  public static String readAsString(File file, String charset) {
    StringBuilder ret = new StringBuilder();
    BufferedReader reader = null;
    try {
      reader = reader(file, charset);
      for (String rl; (rl = reader.readLine()) != null;) { ret.append(rl); }
    } catch (Exception ignore) {
    } finally {
      safeclose(reader);
    }
    return String.valueOf(ret);
  }

  public static void writeToFile(String str, File file, String charset) {
    Writer writer = null;
    try {
      writer = writer(file, charset);
      writer.append(str).flush();
    } catch (Exception ignore) {
    } finally {
      safeclose(writer);
    }
  }

  public static void safeclose(Closeable o) {
    if (o != null) {
      try {
        o.close();
      } catch (Exception ignore) { }
    }
  }

  public static void safeclose(AutoCloseable o) {
    if (o != null) {
      try {
        o.close();
      } catch (Exception ignore) { }
    }
  }

  public static class BufferedReaderWrapper extends BufferedReader {
    private List<Closeable> closeables = new ArrayList<>();
    public BufferedReaderWrapper(Reader in) { super(in); }
    public static BufferedReaderWrapper createReader(File file, String charset) {
      BufferedReaderWrapper inst = null;
      InputStream istream = null;
      try {
        istream = new FileInputStream(file);
        inst = createReader(istream, charset);
      } catch (Exception e) {
        safeclose(istream);
      }
      return inst;
    }
    public static BufferedReaderWrapper createReader(InputStream istream, String charset) {
      BufferedReaderWrapper inst = null;
      Reader reader = null;
      try {
        reader = new InputStreamReader(istream, charset);
        inst = new BufferedReaderWrapper(reader);
        inst.reader = new BufferedReader(reader);
        inst.closeables.add(reader);
        inst.closeables.add(istream);
      } catch (Exception e) {
        safeclose(istream);
        safeclose(reader);
        safeclose(inst);
      }
      return inst;
    }
    private BufferedReader reader;
    public int hashCode() { return reader.hashCode(); }
    public boolean equals(Object obj) { return reader.equals(obj); }
    public int read(CharBuffer target) throws IOException { return reader.read(target); }
    public int read() throws IOException { return reader.read(); }
    public int read(char[] cbuf) throws IOException { return reader.read(cbuf); }
    public int read(char[] cbuf, int off, int len) throws IOException { return reader.read(cbuf, off, len); }
    public String toString() { return reader.toString(); }
    public String readLine() throws IOException { return reader.readLine(); }
    public long skip(long n) throws IOException { return reader.skip(n); }
    public long transferTo(Writer out) throws IOException { return reader.transferTo(out); }
    public boolean ready() throws IOException { return reader.ready(); }
    public boolean markSupported() { return reader.markSupported(); }
    public void mark(int readAheadLimit) throws IOException { reader.mark(readAheadLimit); }
    public void reset() throws IOException { reader.reset(); }
    public Stream<String> lines() { return reader.lines(); }
    public void close() throws IOException {
      reader.close();
      if (closeables != null && closeables.size() > 0) {
        for (Closeable item : closeables) { safeclose(item); }
      }
    }
  }

  public static class BufferedWriterWrapper extends BufferedWriter {
    private List<Closeable> closeables = new ArrayList<>();
    public BufferedWriterWrapper(Writer out) { super(out); }
    public static BufferedWriterWrapper createWriter(File file, String charset) {
      BufferedWriterWrapper inst = null;
      OutputStream ostream = null;
      try {
        ostream = new FileOutputStream(file);
        inst = createWriter(ostream, charset);
      } catch (Exception e) {
        safeclose(ostream);
      }
      return inst;
    }
    public static BufferedWriterWrapper createWriter(OutputStream ostream, String charset) {
      BufferedWriterWrapper inst = null;
      Writer writer = null;
      try {
        writer = new OutputStreamWriter(ostream, charset);
        inst = new BufferedWriterWrapper(writer);
        inst.writer = new BufferedWriter(writer);
        inst.closeables.add(writer);
        inst.closeables.add(ostream);
      } catch (Exception e) {
        safeclose(ostream);
        safeclose(writer);
        safeclose(inst);
      }
      return inst;
    }
    private BufferedWriter writer;

    public int hashCode() { return writer.hashCode(); }
    public boolean equals(Object obj) { return writer.equals(obj); }
    public void write(int c) throws IOException { writer.write(c); }
    public void write(char[] cbuf, int off, int len) throws IOException { writer.write(cbuf, off, len); }
    public void write(char[] cbuf) throws IOException { writer.write(cbuf); }
    public void write(String s, int off, int len) throws IOException { writer.write(s, off, len); }
    public void write(String str) throws IOException { writer.write(str); }
    public void newLine() throws IOException { writer.newLine(); }
    public void flush() throws IOException { writer.flush(); }
    public Writer append(CharSequence csq) throws IOException { return writer.append(csq); }
    public Writer append(CharSequence csq, int start, int end) throws IOException { return writer.append(csq, start, end); }
    public String toString() { return writer.toString(); }
    public Writer append(char c) throws IOException { return writer.append(c); }
    public void close() throws IOException {
      writer.close();
      if (closeables != null && closeables.size() > 0) {
        for (Closeable item : closeables) { safeclose(item); }
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
    BufferedReader reader = null;
    try {
      reader = reader(openResourceStream(baseClass, path), UTF8);
      for (String rl; (rl = reader.readLine()) != null; ret.append(rl));
    } finally {
      safeclose(reader);
    }
    return String.valueOf(ret);
  }
}