/**
 * @File        : ObjectStore.java
 * @Author      : 정재백
 * @Since       : 2024-12-22
 * @Description : 인스턴스 저장소
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

public class ObjectStore<T> {
  private T v;
  public ObjectStore() { };
  public ObjectStore(T v) { this.v = v; }
  public T get() { return v; }
  public void set(T v) { this.v = v; }
}
