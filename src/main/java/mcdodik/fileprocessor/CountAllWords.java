package mcdodik.fileprocessor;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

public class CountAllWords extends RecursiveTask<Long> {

    private static final long SIZE_THRESHOLD = 500 * 1024 * 1024;

    private final Path directory;
    private final Boolean isMain;

    public CountAllWords(Path directory, Boolean isMain) {
        this.isMain = isMain;
        this.directory = directory;
    }

    public CountAllWords(Path directory) {
        this(directory, true);
    }

    @Override
    protected Long compute() {
        Long count = 0L;
        List<CountAllWords> tasks = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    CountAllWords task = new CountAllWords(path);
                    task.fork();
                    tasks.add(task);
                } else if (Files.isRegularFile(path)) {
                    count += processFiles(path, tasks);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing directory: " + directory + " - " + e.getMessage());
        } finally {
            for (CountAllWords task : tasks) {
                // дождемся всех задач
                count += task.join();
            }
        }

        return count;
    }

    private Long processFiles(Path path, List<CountAllWords> tasks) throws IOException {
        // Если файл большой, то обработаем его в другом потоке
        if (Files.size(path) > SIZE_THRESHOLD && isMain) {
            CountAllWords task = new CountAllWords(path, false);
            task.fork();
            tasks.add(task);
        }

        // Считаем слова в файле
        try (Stream<String> lines = Files.lines(path)) {
            return lines.mapToLong(line -> {
                int count = 0;
                boolean isWord = false;
                for (char c : line.toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        if (isWord) {
                            count++;
                            isWord = false;
                        }
                    } else {
                        isWord = true;
                    }
                }
                return isWord ? count + 1 : count;
            }).sum();
        }
    }
}