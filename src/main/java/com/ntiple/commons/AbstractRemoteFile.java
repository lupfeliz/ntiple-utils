/**
 * @File        : MetaIOUtils.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2024-05-26 최초 작성
 * @Description : 입출력 유틸
 **/
package com.ntiple.commons;

import static com.ntiple.commons.ConvertUtil.cast;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;

public abstract class AbstractRemoteFile<T extends AbstractRemoteFile<?>> {
  public String getName() {
    String ret = "";
    return ret;
  }

  public String getAbsolutePath() {
    String ret = "";
    return ret;
  }

  public String getPath() {
    String ret = "";
    return ret;
  }

  public T getParentFile() {
    T ret = null;
    return ret;
  }

  public T getAbsoluteFile() {
    T ret = null;
    return ret;
  }

  public boolean exists() {
    boolean ret = false;
    return ret;
  }

  public URI toURI() {
    URI ret = null;
    return ret;
  }

  public boolean isFile() {
    boolean ret = false;
    return ret;
  }

  public boolean isDirectory() {
    boolean ret = false;
    return ret;
  }

  public long lastModified() {
    long ret = -1;
    return ret;
  }

  public long length() {
    long ret = -1;
    return ret;
  }

  public T[] listFiles() {
    T[] ret = cast(new Object[] {}, ret = null);
    return ret;
  }

  public T[] listFiles(FilenameFilter filter) {
    T[] ret = cast(new Object[] {}, ret = null);
    return ret;
  }

  public abstract File getRealFile();
  public abstract void saveRemoteFile(File file);
}