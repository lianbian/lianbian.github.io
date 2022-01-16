---
title: LRU完整代码
date: 2021-10-29
---

```java
package net.lianbian.tpc.customer;

import java.util.HashMap;
import java.util.Map;

public class LRUDemo<K, V> {
    private int cacheSize;
    Map<Integer, Node<Integer, String>> map;
    DoubleLinkedList<Integer, String> doubleLinkedList;

    public LRUDemo(int cacheSize) {
        this.cacheSize = cacheSize;
        map = new HashMap<>();
        doubleLinkedList = new DoubleLinkedList<>();
    }

    public void refreshNode(Node node) {
        doubleLinkedList.removeNode(node);
        doubleLinkedList.addHead(node);
    }

    public String get(int key) {
        if (!map.containsKey(key)) {
            return "";
        }

        Node<Integer, String> node = map.get(key);
        refreshNode(node);
        return node.value;
    }

    public void put(int key, String value) {
        if (map.containsKey(key)) {
            Node<Integer, String> node = map.get(key);
            node.value = value;
            map.put(key, node);

            refreshNode(node);
        } else {
            if (map.size() == cacheSize) {
                Node lastNode = doubleLinkedList.getLast();
                map.remove(lastNode.key);
                doubleLinkedList.removeNode(lastNode);
            }

            Node<Integer, String> newNode = new Node<>(key, value);
            map.put(key, newNode);
            doubleLinkedList.addHead(newNode);
        }
    }

    class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        public Node() {
            this.prev = this.next = null;
        }

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.prev = this.next = null;
        }
    }

    class DoubleLinkedList<K, V> {
        Node<K, V> head;
        Node<K, V> tail;

        // 构造方法
        public DoubleLinkedList() {
            head = new Node<>();
            tail = new Node<>();
            head.next = tail;
            tail.prev = head;
        }

        public void addHead(Node<K, V> node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }

        public void removeNode(Node<K, V> node) {
            node.next.prev = node.prev;
            node.prev.next = node.next;
            node.prev = null;
            node.next = null;
        }

        public Node getLast() {
            return tail.prev;
        }
    }

    public static void main(String[] args) {
        LRUDemo lruDemo = new LRUDemo(3);
        lruDemo.put(1, "美团");
        lruDemo.put(2, "微信");
        lruDemo.put(3, "抖音");
        lruDemo.put(4, "微博");
        System.out.println(lruDemo.map.keySet());

        System.out.println(lruDemo.get(2));
    }
}
```

