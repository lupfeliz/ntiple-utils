/**
 * @File        : FunctionUtil.java
 * @Author      : 정재백
 * @Since       : 2024-03-22
 * @Description : 함수 형태 정의
 * @Site        : https://devlog.ntiple.com
 **/
package com.ntiple.commons;

public class FunctionUtil {
  @FunctionalInterface public interface Fn0a<R> { public R apply(); }
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

  @FunctionalInterface public interface Fn0av { public void apply(); }
  @FunctionalInterface public interface Fn1av<A1> { public void apply(A1 a1); }
  @FunctionalInterface public interface Fn2av<A1, A2> { public void apply(A1 a1, A2 a2); }
  @FunctionalInterface public interface Fn3av<A1, A2, A3> { public void apply(A1 a1, A2 a2, A3 a3); }
  @FunctionalInterface public interface Fn4av<A1, A2, A3, A4> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4); }
  @FunctionalInterface public interface Fn5av<A1, A2, A3, A4, A5> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A4 a5); }
  @FunctionalInterface public interface Fn6av<A1, A2, A3, A4, A5, A6> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6); }
  @FunctionalInterface public interface Fn7av<A1, A2, A3, A4, A5, A6, A7> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7); }
  @FunctionalInterface public interface Fn8av<A1, A2, A3, A4, A5, A6, A7, A8> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8); }
  @FunctionalInterface public interface Fn9av<A1, A2, A3, A4, A5, A6, A7, A8, A9> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9); }
  @FunctionalInterface public interface Fn10av<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10); }

  @FunctionalInterface public interface Fn0at<R> { public R apply() throws Exception; }
  @FunctionalInterface public interface Fn1at<A1, R> { public R apply(A1 a1) throws Exception; }
  @FunctionalInterface public interface Fn2at<A1, A2, R> { public R apply(A1 a1, A2 a2) throws Exception; }
  @FunctionalInterface public interface Fn3at<A1, A2, A3, R> { public R apply(A1 a1, A2 a2, A3 a3) throws Exception; }
  @FunctionalInterface public interface Fn4at<A1, A2, A3, A4, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4) throws Exception; }
  @FunctionalInterface public interface Fn5at<A1, A2, A3, A4, A5, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A4 a5) throws Exception; }
  @FunctionalInterface public interface Fn6at<A1, A2, A3, A4, A5, A6, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6) throws Exception; }
  @FunctionalInterface public interface Fn7at<A1, A2, A3, A4, A5, A6, A7, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7) throws Exception; }
  @FunctionalInterface public interface Fn8at<A1, A2, A3, A4, A5, A6, A7, A8, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8) throws Exception; }
  @FunctionalInterface public interface Fn9at<A1, A2, A3, A4, A5, A6, A7, A8, A9, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9) throws Exception; }
  @FunctionalInterface public interface Fn10at<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, R> { public R apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10) throws Exception; }

  @FunctionalInterface public interface Fn0avt { public void apply() throws Exception; }
  @FunctionalInterface public interface Fn1avt<A1> { public void apply(A1 a1) throws Exception; }
  @FunctionalInterface public interface Fn2avt<A1, A2> { public void apply(A1 a1, A2 a2) throws Exception; }
  @FunctionalInterface public interface Fn3avt<A1, A2, A3> { public void apply(A1 a1, A2 a2, A3 a3) throws Exception; }
  @FunctionalInterface public interface Fn4avt<A1, A2, A3, A4> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4) throws Exception; }
  @FunctionalInterface public interface Fn5avt<A1, A2, A3, A4, A5> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A4 a5) throws Exception; }
  @FunctionalInterface public interface Fn6avt<A1, A2, A3, A4, A5, A6> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6) throws Exception; }
  @FunctionalInterface public interface Fn7avt<A1, A2, A3, A4, A5, A6, A7> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7) throws Exception; }
  @FunctionalInterface public interface Fn8avt<A1, A2, A3, A4, A5, A6, A7, A8> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8) throws Exception; }
  @FunctionalInterface public interface Fn9avt<A1, A2, A3, A4, A5, A6, A7, A8, A9> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9) throws Exception; }
  @FunctionalInterface public interface Fn10avt<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10> { public void apply(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9, A10 a10) throws Exception; }
}
