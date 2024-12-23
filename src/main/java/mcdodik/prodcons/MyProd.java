package mcdodik.prodcons;

import org.javatuples.Pair;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MyProd {

    private final AtomicInteger numberTask = new AtomicInteger(-1);

    public MyProd(Queue<Pair<Integer, String>> queue, AtomicBoolean stopped, int threadCount, int countAll) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.scheduleAtFixedRate(() -> {
                while (!stopped.get() && numberTask.get() <= countAll) {
                    queue.add(generateMessage());
            }
                if (numberTask.get() >= countAll) {
                    stopped.set(true);
                    executor.shutdown();
                }
            }, 0, 1, TimeUnit.MICROSECONDS);
        }
    }

    private Pair<Integer, String> generateMessage() {
        return Pair.with(numberTask.incrementAndGet(), Thread.currentThread().getName());
    }

}
