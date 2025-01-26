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

import static com.ntiple.commons.FunctionUtil.Fn0a;
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
  private Debouncer debouncer;
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

  public V getAsync(K key, Fn0a<V> callback, long delay) { return getAsync(key, callback, delay, -1); }
  public V getAsync(K key, Fn0a<V> callback, long delay, long expiry) {
    V ret = null;
    if (debouncer == null) { debouncer = new Debouncer(); }
    if ((ret = this.get(key)) != null) {
      debouncer.debounce(key, () -> put(key, callback.apply(), expiry), delay);
    } else {
      put(key, ret = callback.apply());
    }
    return ret;
  }

  public void put(K key, V value) { this.put(key, value, -1); }
  public void put(K key, V value, long expiry) {
    Node<K, V> node = cacheMap.get(key);
    if (node != null) {
      node.value = value;
      if (expiry <= 0) { expiry = this.expiry; }
      node.expire = System.currentTimeMillis() + expiry;
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

  public void clear() {
    cacheMap.clear();
    cacheList.clear();
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

    public void clear() {
      Node<K, V> node = head;
      Node<K, V> tmp = null;
      while (node != null) {
        if (node.next == null) { break; }
        tmp = node;
        node = node.next;
        tmp.next = null;
        tmp.prev = null;
      }
      head = null;
      tail = null;
    }
  }
}

/** JAVASCRIPT IMPLEMENTATION */
//   class LRUCache {
//     capacity = 0;
//     cacheList = [];
//     cacheMap = {};
//     handleMap = {};
//     expiry = 0;
//     constructor(capacity, expiry = 1000 * 10) {
//       this.capacity = capacity;
//       this.cacheList = new DoublyLinkedList();
//       this.cacheMap = {};
//       this.expiry = expiry;
//     };
//     get(key) {
//       let node = this.cacheMap[key];
//       if (node === undefined) { return undefined; };
//       if (node.expire < new Date().getTime()) {
//         this.remove(key);
//         return undefined;
//       };
//       node.expire = new Date().getTime() + this.expiry;
//       this.moveToHead(node);
//       return node.value;
//     };
//     async getAsync(key, callback, delay, expiry = -1) {
//       let ret = undefined;
//       const self = this;
//       if ((ret = self.get(key)) !== undefined) {
//         if (self.handleMap[key]) { clearTimeout(self.handleMap[key]); };
//         self.handleMap[key] = setTimeout(async function() { self.put(key, await callback(), expiry); }, delay);
//       } else {
//         self.put(key, ret = await callback());
//       };
//       return ret;
//     };
//     put(key, value, expiry = -1) {
//       let node = this.cacheMap[key];
//       if (node !== undefined) {
//         node.value = value;
//         node.expire - new Date().getTime() + this.expiry;
//         this.moveToHead(node);
//         return;
//       };
//       let newNode = new DoublyLinkedNode(key, value);
//       this.cacheList.addFirst(newNode);
//       if (!expiry) { expiry = this.expiry; };
//       newNode.expire = new Date().getTime() + expiry;
//       if (this.cacheList.size() > this.capacity) {
//         this.removeLeast();
//       }
//       this.cacheMap[key] = newNode;
//     };
//     remove(key) {
//       let node = this.cacheMap[key];
//       delete this.cacheMap[key];
//       if (node === undefined) { return; };
//       this.cacheList.remove(node);
//     };
//     size() { return this.cacheList.size(); };
//     // dump() { this.cacheList.dump(this.capacity); };
//     moveToHead(node) {
//       this.cacheList.remove(node);
//       this.cacheList.addFirst(node);
//     };
//     removeLeast() {
//       let size = this.cacheList.size();
//       let tail = this.cacheList.removeLast();
//       for (let inx = size; tail !== undefined && inx > this.capacity; inx--) {
//         let prev = tail.prev;
//         delete this.cacheMap[tail.key];
//         this.cacheList.remove(tail);
//         tail = prev;
//       };
//     };
//     removeExpired() { this.cacheList.removeExpired(); };
//     keySet() { return Object.keys(this.cacheMap); };
//     keyIter() {
//       let node = this.cacheList.head;
//       return {
//         hasNext() { return node !== undefined && node.next !== undefined; },
//         next() {
//           if (node === undefined) { return undefined; };
//           let ret = node.key;
//           node = node.next;
//           return ret;
//         }
//       };
//     };
//     stringify() {
//       return JSON.stringify(this.cacheList.dump());
//     };
//     parse(str) {
//       let list =JSON.parse(str);
//       let prev = undefined;
//       let node = this.cacheList.head = this.cacheList.tail = undefined;
//       for (const itm of list) {
//         node = new DoublyLinkedNode(itm.k, itm.v);
//         node.expire = Number(itm.t);
//         if (prev === undefined) {
//           this.cacheList.head = node;
//         } else {
//           prev.next = node;
//         }
//         node.prev = prev;
//         this.cacheList.tail = node;
//         this.cacheMap[itm.k] = node;
//         prev = node;
//       };
//     };
//   };
//   class DoublyLinkedNode {
//     key;
//     value;
//     prev;
//     next;
//     expire;
//     constructor(key, value) {
//       this.key = key;
//       this.value = value;
//     };
//   };
//   class DoublyLinkedList {
//     head = undefined;
//     tail = undefined;
//     addFirst(node) {
//       if (this.isEmpty()) {
//         this.head = this.tail = node;
//       } else {
//         node.next = this.head;
//         this.head.prev = node;
//         this.head = node;
//       };
//     };
//     remove(node) {
//       if (node === this.head) {
//         this.head = this.head.next;
//         if (this.heead !== undefined) { this.head.prev = undefined; };
//       } else if (node === this.tail) {
//         this.tail = this.tail.prev;
//         if (this.tail !== undefined) { this.tail.next = undefined; };
//       };
//       if (node.prev !== undefined) { node.prev.next = node.next; };
//       if (node.next !== undefined) { node.next.prev = node.prev; };
//       node.next = undefined;
//       node.prev = undefined;
//     };
//     removeLast() {
//       if (this.isEmpty()) { return; };
//       let last = this.tail;
//       this.remove(last);
//       return last;
//     };
//     isEmpty() {
//       return this.head === undefined;
//     };
//     removeExpired() {
//       let node = this.tail;
//       let prev = undefined;
//       let curtime = new Date().getTime();
//       LOOP: while (node !== undefined) {
//         if (node.expire < curtime) {
//           prev = node.prev;
//           this.remove(node);
//           node = prev;
//           continue LOOP;
//         }
//         if (node.prev !== undefined) { break LOOP; }
//         node = node.prev;
//       };
//     };
//     size() {
//       let size = 0;
//       let node = this.head;
//       while (node !== undefined) {
//         size += 1;
//         if (node.next === undefined) { break; };
//         node = node.next;
//       };
//       return size;
//     };
//     // dump(limit) {
//     //   let size = 0;
//     //   let node = this.head;
//     //   while (node !== undefined && size < limit) {
//     //     log.debug("NODE:", node);
//     //     size += 1;
//     //     if (node.next === undefined) { break; };
//     //     node = node.next;
//     //   };
//     // };
//     dump() {
//       let ret = "";
//       // let size = 0;
//       let node = this.head;
//       let list = [];
//       while (node !== undefined) {
//         if (ret) { ret = `${ret},`; };
//         let value = node.value;
//         // if (typeof value === "string") {
//         //   value = `s:${value}`;
//         // } else if (typeof value === "number") {
//         //   value = `n:${value}`;
//         // } else {
//         //   value = `o:${JSON.stringify(value)}`;
//         // }
//         // ret = `${ret}{"k":"${node.key}","v":"${value}","t":${node.expire}}`;
//         list.push({ k: node.key, v: value, t: node.expire });
//         // size += 1;
//         if (node.next === undefined) { break; };
//         node = node.next;
//       };
//       // if (ret) { ret = `[${ret}]`; };
//       // if (list.length > 0) { ret = JSON.stringify(list); };
//       // return ret;
//       return list;
//     };
//   };