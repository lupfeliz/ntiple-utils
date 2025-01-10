/**
 * @File        : LRUCache.java
 * @Author      : 정재백
 * @Since       : 2025-01-05
 * @Description : LRU캐시
 * @Site        : https://devlog.ntiple.com
 * 
 * 원본출처 : https://medium.com/@germainnsibula/implementing-an-lru-cache-in-java-a-comprehensive-guide-94e8884ff17b
 **/
package com.ntiple.commons;

import static com.ntiple.commons.StringUtil.cat;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LRUCache<K, V> {
  private static final SimpleLogger log = SimpleLogger.getLogger();
  private final int capacity;
  private final DoublyLinkedList<K, V> cacheList;
  private final Map<K, Node<K, V>> cacheMap;
  private long expiry;
  public LRUCache(int capacity) {
    this(capacity, 1000 * 10);
  }
  public LRUCache(int capacity, long expiry) {
    this.capacity = capacity;
    this.cacheList = new DoublyLinkedList<>();
    this.cacheMap = new ConcurrentHashMap<>();
    this.expiry = expiry;
  }
  public V get(K key) {
    Node<K, V> node = cacheMap.get(key);
    if (node == null) { return null; }
    if (node.expire < System.currentTimeMillis()) {
      // log.debug("EXPIRED:{}", key, System.currentTimeMillis() - node.expire);
      remove(key);
      return null;
    }
    node.expire = System.currentTimeMillis() + this.expiry;
    moveToHead(node);
    return node.value;
  }

  public void put(K key, V value) {
    Node<K, V> node = cacheMap.get(key);
    if (node != null) {
      node.value = value;
      node.expire = System.currentTimeMillis() + this.expiry;
      moveToHead(node);
      return;
    }
    Node<K, V> newNode = new Node<>(key, value);
    synchronized(cacheList) {
      cacheList.addFirst(newNode);
      newNode.expire = System.currentTimeMillis() + this.expiry;
      // log.debug("NEW-NODE:{}", newNode);
      if (cacheList.size() > capacity) {
        // log.debug("REMOVE-USED");
        removeLeast();
      }
    }
    cacheMap.put(key, newNode);
  }

  public void remove(K key) {
    Node<K, V> node = cacheMap.remove(key);
    if (node == null) { return; }
    synchronized(cacheList) {
      cacheList.remove(node);
    }
  }

  public void dump() {
    cacheList.dump(capacity);
  }

  private void moveToHead(Node<K, V> node) {
    synchronized(cacheList) {
      cacheList.remove(node);
      cacheList.addFirst(node);
    }
  }

  private void removeLeast() {
    synchronized(cacheList) {
      int size = cacheList.size();
      Node<K, V> tail = cacheList.removeLast();
      for (int inx = size; tail != null && inx > capacity; inx--) {
        Node<K, V> prev = tail.prev;
        cacheMap.remove(tail.key);
        cacheList.remove(tail);
        tail = prev;
      }
    }
  }

  public void removeExpired() {
    synchronized(cacheList) {
      cacheList.removeExpired();
    }
  }

  public Set<K> keySet() { return cacheMap.keySet(); }
  public Iterator<K> keyIter() {
    return new Iterator<K>() {
      Node<K, V> node = cacheList.head;
      @Override public boolean hasNext() { return node != null && node.next != null; }
      @Override public K next() {
        if (node == null) { return null; }
        K ret = this.node.key;
        this.node = node.next;
        return ret;
      }
    };
  }

  private static class Node<K, V> {
    final K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;
    long expire;
    public Node(K key, V value) {
      this.key = key;
      this.value = value;
    }
    @Override public String toString() { return cat("K:", String.valueOf(key), "/V:", String.valueOf(value)); }
  }

  private static class DoublyLinkedList<K, V> {
    private Node<K, V> head;
    private Node<K, V> tail;
    public void addFirst(Node<K, V> node) {
      if (isEmpty()) {
        head = tail = node;
      } else {
        node.next = head;
        head.prev = node;
        head = node;
      }
    }

    public void remove(Node<K, V> node) {
      if (node == head) {
        head = head.next;
        if (head != null) { head.prev = null; }
      } else if (node == tail) {
        tail = tail.prev;
        if (tail != null) { tail.next = null; }
      }
      if (node.prev != null) { node.prev.next = node.next; }
      if (node.next != null) { node.next.prev = node.prev; }
      node.next = null;
      node.prev = null;
    }

    public Node<K, V> removeLast() {
      if (isEmpty()) { throw new IllegalStateException("List is empty"); }
      Node<K, V> last = tail;
      remove(last);
      return last;
    }

    public boolean isEmpty() { return head == null; }

    public void removeExpired() {
      Node<K, V> node = tail;
      Node<K, V> prev = null;
      long curtime = System.currentTimeMillis();
      LOOP: while (node != null) {
        if (node.expire < curtime) {
          prev = node.prev;
          remove(node);
          node = prev;
          continue LOOP;
        }
        if (node.prev == null) { break; }
        node = node.prev;
      }
    }

    public int size() {
      int size = 0;
      Node<K, V> node = head;
      while (node != null) {
        size++;
        if (node.next == null) { break; }
        node = node.next;
      }
      // log.debug("SIZE:{}", size);
      return size;
    }

    public void dump(int limit) {
      int size = 0;
      Node<K, V> node = head;
      while (node != null && size < limit) {
        log.debug("NODE:{}", node);
        size++;
        if (node.next == null) { break; }
        node = node.next;
      }
    }
  }
}
