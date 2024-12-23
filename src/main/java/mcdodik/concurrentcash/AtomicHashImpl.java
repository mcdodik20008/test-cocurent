package mcdodik.concurrentcash;

import org.javatuples.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicHashImpl<K, V> implements AtomicHash<K, V> {

    private ConcurrentHashMap<K, org.javatuples.Pair<V, AtomicLong>> map = new ConcurrentHashMap<>();
    public int borderUsagesToDelete = 10;

    public AtomicHashImpl() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            for (K key : map.keySet()) {
                Pair<V, AtomicLong> pair = map.get(key);
                if (pair == null) {
                    continue;
                }
                if (pair.getValue1().get() < borderUsagesToDelete) {
                    map.remove(key);
                } else {
                    AtomicLong count = pair.getValue1();
                    count.compareAndSet(count.get(), 0);
                }

            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public V get(K key) {
        var value = map.get(key);
        if (value == null) {
            return null;
        }

        AtomicLong countUsages = value.getValue1();
        countUsages.incrementAndGet();
        return value.getValue0();
    }


    @Override
    public void put(K key, V value) {
        map.put(key, Pair.with(value, new AtomicLong(0)));
    }

}
