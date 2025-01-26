/**
 * @File        : Debouncer.java
 * @Author      : 정재백
 * @Since       : 2024-12-25
 * @Description : Debouncer
 * @Site        : https://devlog.ntiple.com
 * 
 * 출처 : https://stackoverflow.com/questions/4742210/implementing-debounce-in-java
 **/
package com.ntiple.commons;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer {
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private final ConcurrentHashMap<Object, Future<?>> delayedMap = new ConcurrentHashMap<>();

  /**
   * Debounces {@code callable} by {@code delay}, i.e., schedules it to be
   * executed after {@code delay},
   * or cancels its execution if the method is called with the same key within the
   * {@code delay} again.
   */
  public void debounce(final Object key, final Runnable runnable, long delay, TimeUnit unit) {
    final Future<?> prev = delayedMap.put(key, scheduler.schedule(new Runnable() {
      @Override public void run() {
        try {
          runnable.run();
        } finally {
          delayedMap.remove(key);
        }
      }
    }, delay, unit));
    if (prev != null) {
      prev.cancel(true);
    }
  }
  public void debounce(final Object key, final Runnable runnable, long delay) { debounce(key, runnable, delay, TimeUnit.MILLISECONDS); }
  public void shutdown() { scheduler.shutdownNow(); }
}