package com.example.cache

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

class LRUCache<K, V>(private val capacity: Int) : SynchronizedObject() {

    private class Node<K, V>(
        val key: K,
        var value: V,
        var prev: Node<K, V>? = null,
        var next: Node<K, V>? = null
    )

    private val map = atomic(mutableMapOf<K, Node<K, V>>())
    private var head: Node<K, V>? = null
    private var tail: Node<K, V>? = null

    fun get(key: K): V? = synchronized(this) {
        val node = map.value[key] ?: return null
        moveToFront(node)
        return node.value
    }

    fun put(key: K, value: V) = synchronized(this) {
        if (map.value.containsKey(key)) {
            val node = map.value[key]!!
            node.value = value
            moveToFront(node)
        } else {
            if (map.value.size >= capacity) {
                tail?.let {
                    map.value.remove(it.key)
                    remove(it)
                }
            }
            val newNode = Node(key, value)
            addToFront(newNode)
            map.value[key] = newNode
        }
    }

    fun remove(key: K) = synchronized(this) {
        val node = map.value.remove(key) ?: return
        remove(node)
    }

    fun clear() = synchronized(this) {
        map.value.clear()
        head = null
        tail = null
    }

    fun size(): Int = synchronized(this) {
        map.value.size
    }

    private fun moveToFront(node: Node<K, V>) {
        remove(node)
        addToFront(node)
    }

    private fun addToFront(node: Node<K, V>) {
        node.next = head
        node.prev = null
        head?.prev = node
        head = node
        if (tail == null) tail = head
    }

    private fun remove(node: Node<K, V>) {
        node.prev?.next = node.next
        node.next?.prev = node.prev
        if (node == head) head = node.next
        if (node == tail) tail = node.prev
    }
}
