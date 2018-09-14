package de.comparus.opensource.longmap;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class LongMapImpl<V> implements LongMap<V> {

    /* ---------------- Default values ---------------- */

    /**
     * The default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 16;

    /**
     * The default load factor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /* ---------------- Link(s) between key(s) and value(s) in a bucket ---------------- */

    static class Node<V> {

        private List<Node<V>> nodes;
        private long key;
        private V value;

        private Node(long key, V value) {
            this.key = key;
            this.value = value;
            nodes = new LinkedList<>();
        }

        public List<Node<V>> getNodes() {
            return nodes;
        }

        public void setValue(V value) {
            this.value = value;
        }

        private long getKey() {
            return key;
        }

        private V getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Node)) {
                return false;
            }
            Node node = (Node) o;
            return Objects.equals(key, node.getKey()) &&
                    Objects.equals(value, node.getValue());
        }

        @Override
        public String toString() {
            return "key: " + key + "; value: " + value;
        }
    }

    /* ---------------- Fields ---------------- */

    /**
     * The number of key-value mappings contained in this map.
     */
    private int size;

    /**
     * The load factor for this map.
     */
    private final float loadFactor;

    /**
     * The next count of buckets.
     * The time of resizing is calculated by the next formula: capacity * load factor.
     */
    private int threshold;

    /**
     * The array of nodes in this map.
     */
    private Node<V>[] currentNodes;

    /* ---------------- Constructors ---------------- */

    /**
     * Constructs an empty LongMapImpl with the specified initial capacity
     * and the specified load factor.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *                                  or the load factor is non-positive
     */
    @SuppressWarnings("unchecked")
    public LongMapImpl(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        }
        this.loadFactor = loadFactor;
        threshold = Math.round(initialCapacity * this.loadFactor);
        currentNodes = new Node[initialCapacity];
    }

    /**
     * Constructs an empty LongMapImpl with the default initial capacity (16)
     * and the default load factor (0.75).
     */
    public LongMapImpl() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty LongMapImpl with the specified initial capacity
     * and the default load factor (0.75).
     *
     * @param initialCapacity the initial capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     */
    public LongMapImpl(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty LongMapImpl with the default initial capacity (16)
     * and the specified load factor.
     *
     * @param loadFactor the load factor
     * @throws IllegalArgumentException if the the load factor is non-positive
     */
    public LongMapImpl(float loadFactor) {
        this(DEFAULT_CAPACITY, loadFactor);
    }

    /* ---------------- Main Methods ---------------- */

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or
     * null if there was no mapping for key
     */
    public V put(long key, V value) {
        if (size + 1 >= threshold) {
            resize();
        }

        Node<V> node = new Node<>(key, value);
        int index = hash(node);

        if (currentNodes[index] == null) {
            return add(index, node);
        }
        return resolvingCollision(index, node);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map
     * contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     * @throws ArrayIndexOutOfBoundsException if index equal or bigger then length of current nodes
     */
    public V get(long key) {
        int index = hash(key);

        if (currentNodes[index] != null) {
            List<Node<V>> nodes = currentNodes[index].getNodes();
            for (Node<V> node : nodes) {
                if (key == node.getKey()) {
                    return node.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public V remove(long key) {
        int index = hash(key);
        V value = null;

        if (currentNodes[index] == null) {
            return value;
        }

        if (currentNodes[index].getNodes().size() == 1) {
            value = currentNodes[index].getNodes().get(0).getValue();
            currentNodes[index] = null;
            size--;
            return value;
        }

        List<Node<V>> nodes = currentNodes[index].getNodes();
        for (Node<V> node : nodes) {
            if (key == node.getKey()) {
                value = node.getValue();
                nodes.remove(node);
                size--;
                return value;
            }
        }
        return value;
    }

    /**
     * Returns true if this map contains no key-value mappings.
     *
     * @return true if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     */
    public boolean containsKey(long key) {
        int index = hash(key);

        if (Objects.nonNull(currentNodes[index])) {
            List<Node<V>> nodes = currentNodes[index].getNodes();
            for (Node<V> node : nodes) {
                if (key == node.getKey()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this map maps one or more keys to the specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return true if this map maps one or more keys to the specified value
     */
    public boolean containsValue(V value) {
        return getAllNodes().stream().anyMatch(val -> isEqualValues(value, val.getValue()));
    }

    /**
     * Returns all the keys contained in this map.
     *
     * @return array of keys
     */
    public long[] keys() {
        List<Long> keys = getAllNodes().stream().map(Node::getKey).collect(Collectors.toList());

        long[] keySet = new long[keys.size()];

        for (int i = 0; i < keys.size(); i++) {
            keySet[i] = keys.get(i);
        }
        return keySet;
    }

    /**
     * Returns all the values contained in this map. Creation of arrays of
     * unknown type has a problem similar to the one of creation of objects
     * of an unknown type: the translation process maps the array to an
     * array of the type variable’s erasure, which is its leftmost bound or
     * Object, if no bound is specified.
     *
     * @return array of values or null if this map contains no values
     */
    @SuppressWarnings("unchecked")
    public V[] values() {
        List<V> values = getAllNodes().stream().map(Node::getValue).collect(Collectors.toList());
        Optional<V> genericClass = values.stream().filter(Objects::nonNull).findAny();

        if (values.isEmpty() || !genericClass.isPresent()) {
            return null;
        }
        V[] valuesSet = (V[]) Array.newInstance(genericClass.get().getClass(), values.size());

        return values.toArray(valuesSet);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public long size() {
        return size;
    }

    /**
     * Removes all of the mappings from this map.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        size = 0;
        for (int i = 0; i < currentNodes.length; i++){
            currentNodes[i] = null;
        }
    }

    /* ---------------- Utils Methods ---------------- */

    /**
     * Returns calculated hash of a node key.
     *
     * @param node a node, which key needs to be hashed
     * @return hash value
     */
    private int hash(Node<V> node) {
        return hash(node.getKey());
    }

    /**
     * Returns calculated hash of a key.
     *
     * @param key a key, which needs to be hashed
     * @return hash value
     */
    private int hash(long key) {
        return Objects.hashCode(key) % currentNodes.length;
    }

    /**
     * Resolving potential collision. It may happen when we try to add new node
     * in non-empty bucket. If new node has same key but another value with the
     * already defined in this bucket node(s), then new value will override
     * existing value. If new node has the same hasCode() like already existing
     * node(s) and they are not equals, then new node adding in this bucket,
     * but not replacing existing node(s).
     *
     * @param index index of the bucket in which we need to add new node
     * @param node  entity of node which we need to add
     * @return the previous value associated with key, or
     * null if there was no mapping for key
     */
    private V resolvingCollision(int index, Node<V> node) {
        V answer = null;

        List<Node<V>> multiValueNodes = currentNodes[index].getNodes();

        for (Node<V> oneValue : multiValueNodes) {

            boolean isEqualValues = isEqualValues(oneValue.getValue(), node.getValue());

            if ((oneValue.getKey() == node.getKey()) && !isEqualValues) {
                answer = oneValue.getValue();
                oneValue.setValue(node.getValue());
            }

            if ((oneValue.hashCode() == node.hashCode()) && !oneValue.equals(node)) {
                multiValueNodes.add(node);
                size++;
                answer = oneValue.getValue();
            }
        }
        return answer;
    }

    /**
     * Add new node to the empty bucket
     *
     * @param index index of the bucket in which we need to add new node
     * @param node  entity of node which we need to add
     * @return null
     */
    private V add(int index, Node<V> node) {
        currentNodes[index] = new Node<>(node.key, node.value);
        currentNodes[index].getNodes().add(node);
        size++;
        return null;
    }

    /**
     * Double the numbers of buckets and threshold. Initialize new array
     * of buckets and rewrite all nodes to it.
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<V>[] oldNodes = currentNodes;
        size = 0;
        threshold *= 2;

        currentNodes = new Node[oldNodes.length * 2];
        for (Node<V> node : oldNodes) {
            if (node != null) {
                for (Node<V> possibleMultiValue : node.getNodes()) {
                    put(possibleMultiValue.getKey(), possibleMultiValue.getValue());
                }
            }
        }
    }

    /**
     * Returns all pairs of key-value in this map.
     *
     * @return all pairs of key-value in this map
     */
    private List<Node<V>> getAllNodes() {
        List<Node<V>> nodes = new LinkedList<>();

        for (Node<V> currentNode : currentNodes) {
            if (currentNode != null) {
                nodes.addAll(currentNode.getNodes());
            }
        }
        return nodes;
    }

    /**
     * Comparing two values for equality with avoiding NullPointerException
     * because value can be equal to null
     *
     * @param value1 first value to compare
     * @param value2 second value to compare
     * @return true if values are equal
     */
    private boolean isEqualValues(V value1, V value2){
        if (value1 == null && value2 == null) {
            return true;
        } else if (value1 == null || value2 == null) {
            return false;
        } else {
            return value1.equals(value2);
        }
    }
}
