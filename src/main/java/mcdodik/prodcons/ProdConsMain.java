package mcdodik.prodcons;

import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ProdConsMain {
    public static void main(String[] args) {
        AtomicBoolean stopped = new AtomicBoolean(false);
        var queue = new ConcurrentLinkedQueue<Pair<Integer, String>>();
        var prod = new MyProd(queue, stopped, 10, 100);
        var cons = new MyCons(queue, 100);
        try {
            while (!stopped.get()) {
                Thread.sleep(1);
            }
        } catch (InterruptedException ignored) {}

        Integer val = 0;
        Stream<Integer> stream = cons.dataList.stream().flatMap(Collection::stream);
        for (int i : stream.toList()) {
            val += i;
        }
        System.out.println("sum: " + val);
    }

    public static boolean validateQueueSum(ConcurrentLinkedQueue<Pair<Integer, String>> queue, int start, int end) {
        int actualSum = 0;

        // Считаем сумму всех чисел в очереди
        for (Pair<Integer, String> pair : queue) {
            actualSum += pair.getValue0();
        }

        // Ожидаемая сумма
        int expectedSum = (end * (end + 1)) / 2;

        // Сравниваем суммы
        if (actualSum != expectedSum) {
            System.out.println("Expected sum: " + expectedSum + ", but found: " + actualSum);
            return false;
        }

        return true;
    }
}
