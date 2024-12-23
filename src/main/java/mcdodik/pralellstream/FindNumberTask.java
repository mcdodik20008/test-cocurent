package mcdodik.pralellstream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

public class FindNumberTask extends RecursiveTask<List<Long>> {
    // количество элементов, при котором задача разделиться
    private static final int THRESHOLD = 1000;
    private final List<Long> arr;
    private final int start;
    private final int end;

    public FindNumberTask(List<Long> arr, int start, int end) {
        this.arr = arr;
        this.start = start;
        this.end = end;
    }

    @Override
    protected List<Long> compute() {
        if (end - start <= THRESHOLD) {
            List<Long> results = new ArrayList<>();
            for (int i = start; i < end; i++) {
                if (arr.get(i) % 777 == 0) {
                    results.add(arr.get(i));
                }
            }
            return results;
        }

        int mid = (start + end) / 2;
        FindNumberTask leftTask = new FindNumberTask(arr, start, mid);
        FindNumberTask rightTask = new FindNumberTask(arr, mid, end);

        leftTask.fork();
        List<Long> rightResult = rightTask.compute();
        List<Long> leftResult = leftTask.join();

        leftResult.addAll(rightResult);
        return leftResult;
    }
}