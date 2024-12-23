package mcdodik.prodcons;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyCons {

    public final List<List<Integer>> dataList = new ArrayList<>();

    public MyCons(Queue<Pair<Integer, String>> queue, int threadCount) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            var arr = new ArrayList<Integer>();
            dataList.add(arr);
            executor.scheduleAtFixedRate(() -> {
                int value = 0;
                while (queue.isEmpty()) {
                    try {
                        Thread.sleep(10);
                        value += 1;
                        System.out.println("value " + value);
                        if (value > 10){
                            executor.shutdown();
                            return;
                        }
                    } catch (Exception ignored) {}
                }
                arr.add(queue.poll().getValue0());

            }, 0, 1, TimeUnit.MICROSECONDS);
        }
    }

}
