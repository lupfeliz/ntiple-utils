/**
 * @File        : FunctionUtil.java
 * @Version     : $Rev$
 * @Author      : 정재백
 * @History     : 2024-03-22 최초 작성
 * @Description : 함수 형태 정의
 **/
package com.ntiple.commons;

public class FunctionUtil {
  @FunctionalInterface public interface Fn1a<A1, R> { public R apply(A1 a1); }
  @FunctionalInterface public interface Fn2a<A1, A2, R> { public R apply(A1 a1, A2 a2); }
  @FunctionalInterface public interface Fn3a<A1, A2, A3, R> { public R apply(A1 a1, A2 a2, A3 a3); }
  @FunctionalInterface public interface Fn4a<A1, A2, A3, A4, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4); }
  @FunctionalInterface public interface Fn5a<A1, A2, A3, A4, A5, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A4 a5); }
  @FunctionalInterface public interface Fn6a<A1, A2, A3, A4, A5, A6, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6); }
  @FunctionalInterface public interface Fn7a<A1, A2, A3, A4, A5, A6, A7, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7); }
  @FunctionalInterface public interface Fn8a<A1, A2, A3, A4, A5, A6, A7, A8, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8); }
  @FunctionalInterface public interface Fn9a<A1, A2, A3, A4, A5, A6, A7, A8, A9, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9); }
  @FunctionalInterface public interface Fn10a<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10); }
}
