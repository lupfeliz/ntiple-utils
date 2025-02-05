/**
 * @File        : ObjectStore.java
 * @Author      : 정재백
 * @Since       : 2024-12-22
 * @Description : 인스턴스 저장소
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

import com.ntiple.commons.FunctionUtil.Fn0a;

public class ObjectStore<T> {
  private T v;
  public ObjectStore() { };
  public ObjectStore(T v) { this.v = v; }
  public T get() { return v; }
  public T set(T v) { return (this.v = v); }
  public T getAsync(Fn0a<T> cb) {
    T ret = null;
    if (v != null) {
      ret = v;
    } else {
      ret = v = cb.apply();
    }
    return ret;
  }
}
