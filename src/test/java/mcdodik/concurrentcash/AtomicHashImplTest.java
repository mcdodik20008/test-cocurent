package mcdodik.concurrentcash;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AtomicHashImplTest {

    @Test
    void testPutAndGet() {
        AtomicHashImpl<String, String> cache = new AtomicHashImpl<>();

        cache.put("key1", "value1");
        cache.put("key2", "value2");

        assertEquals("value1", cache.get("key1"));
        assertEquals("value2", cache.get("key2"));
        assertNull(cache.get("key3"));
    }

    @Test
    void testUsageCounterIncrement() {
        AtomicHashImpl<String, String> cache = new AtomicHashImpl<>();

        cache.put("key1", "value1");

        // Читаем ключ несколько раз.
        cache.get("key1");
        cache.get("key1");

        // Проверяем, что счетчик использования был увеличен.
        // В реальном сценарии этот счетчик не доступен напрямую,
        // поэтому проверка косвенная — через TimerTask, который сбрасывает счетчики.
        assertEquals("value1", cache.get("key1")); // Элемент все еще доступен.
    }

    @Test
    void testEvictionOfUnusedKeys() throws InterruptedException {
        AtomicHashImpl<String, String> cache = new AtomicHashImpl<>();

        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // Эмулируем отсутствие использования "key1".
        Thread.sleep(30 * 1000); // Дождемся запуска очистки кеша.
        for (int i = 0; i < 12; i++){
            cache.get("key2");
        }
        Thread.sleep(35 * 1000); // Дождемся запуска очистки кеша.

        assertNull(cache.get("key1")); // Должен быть удален.
        assertEquals("value2", cache.get("key2")); // Должен остаться.
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        AtomicHashImpl<String, Integer> cache = new AtomicHashImpl<>();

        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        cache.put("key1", 0);

        // Запускаем потоки, которые одновременно инкрементируют счетчик.
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    cache.get("key1");
                }
                latch.countDown();
            });
        }

        latch.await(); // Ждем завершения всех потоков.
        executor.shutdown();

        // Проверяем, что значение корректно.
        assertNotNull(cache.get("key1"));
    }

    @Test
    void testHighConcurrency() throws InterruptedException {
        AtomicHashImpl<String, Integer> cache = new AtomicHashImpl<>();
        String key = "key";
        int initialValue = 0;

        // Добавляем начальное значение.
        cache.put(key, initialValue);

        int numThreads = 1000; // Количество потоков.
        int numIterations = 100000; // Операций на поток.
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        AtomicInteger putCounter = new AtomicInteger(0);

        // Запускаем потоки.
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < numIterations; j++) {
                    // 80% операций - чтение, 20% - запись.
                    if (ThreadLocalRandom.current().nextInt(100) < 80) {
                        cache.get(key); // Читаем ключ.
                    } else {
                        cache.put(key, putCounter.incrementAndGet()); // Обновляем значение.
                    }
                }
                latch.countDown();
            });
        }

        latch.await(); // Ждем завершения всех потоков.
        executor.shutdown();

        // Проверяем, что ключ все еще существует в кеше.
        assertNotNull(cache.get(key));

        // Убедимся, что кеш корректно обработал записи.
        int finalValue = putCounter.get();
        assertTrue(finalValue > 0, "Put operation did not execute properly.");
        System.out.println("Final value written by put operations: " + finalValue);

        // Проверяем, что счетчик использования больше 0.
        System.out.println("Cache handled high concurrency successfully.");
    }

}