package mcdodik.concurrentcash;

public interface AtomicHash<K, V> {

    V get(K key);

    void put(K key, V value);

}
