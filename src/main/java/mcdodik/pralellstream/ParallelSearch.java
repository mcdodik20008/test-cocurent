package mcdodik.pralellstream;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ParallelSearch {

    public static void main(String[] args) {
        // подготовка данных
        int size = 2_000_000;
        var arr = new ArrayList<Long>(size);
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            arr.add(random.nextLong(1, 778));
        }
        arr.add(777L);


        long startTime = System.currentTimeMillis();
        List<Long> divisibleBy777 = arr.stream()
                .parallel()
                .filter(num -> num % 777 == 0)
                .toList();
        long endTime = System.currentTimeMillis();
        System.out.println("parralel Stream: " + divisibleBy777.size());
        System.out.println("parralel Stream time: " + (endTime - startTime));

        //
        startTime = System.currentTimeMillis();
        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();
        ForkJoinPool forkJoinPool = new ForkJoinPool(100);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int x = i;
            futures.add(forkJoinPool.submit(() -> {
                int batch = size / 100;
                int start = x * batch;
                int end = (x + 1) * batch;

                for (int j = start; j <= end; j++) {
                    if (arr.get(j) % 777 == 0) {
                        queue.add(arr.get(j));
                    }
                }
            }));
        }
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception ignored) {
        }

        endTime = System.currentTimeMillis();
        System.out.println("ForkJoinPool: " + queue.size());
        System.out.println("ForkJoinPool time: " + (endTime - startTime));

        startTime = System.currentTimeMillis();
        forkJoinPool = new ForkJoinPool(100);
        FindNumberTask task = new FindNumberTask(arr, 0, size);
        List<Long> results = forkJoinPool.invoke(task);

        endTime = System.currentTimeMillis();
        System.out.println("FindNumberTask: " + results.size());
        System.out.println("FindNumberTask time: " + (endTime - startTime));
    }

}
