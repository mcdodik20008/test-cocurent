package mcdodik.fileprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import static org.junit.jupiter.api.Assertions.*;

class CountAllWordsTest {

    public static final long FAKE_SIZE_THRESHOLD = 130_000;

    private Path testDirectory;

    @BeforeEach
    void setUp() throws IOException {
        // Создадим тестовую директорию
        testDirectory = Files.createTempDirectory("testDir");

        // Создадим несколько файлов
        createTestFile(testDirectory.resolve("smallFile.txt"), "Hello World!");
        createTestFile(testDirectory.resolve("largeFile.txt"), "Hello World! \n".repeat(10_000)); // Большой файл
        createTestFile(testDirectory.resolve("mediumFile.txt"), "Java is awesome! \n".repeat(100));
    }

    private void createTestFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    @Test
    void testCountAllWords() {
        CountAllWords countAllWordsTask = new CountAllWords(testDirectory);
        Long result = countAllWordsTask.fork().join();

        // Проверим, что количество слов равно ожидаемому
        long expectedWordCount = 2 + (2 * 10_000) + (3 * 100); // маленький файл + большой файл + средний файл
        assertEquals(expectedWordCount, result);
    }

    @Test
    void testLargeFileProcessing() throws IOException {
        Path largeFile = testDirectory.resolve("largeFile.txt");
        long fileSize = Files.size(largeFile);

        System.out.println(fileSize);

        assertTrue(fileSize > FAKE_SIZE_THRESHOLD);

        CountAllWords countAllWordsTask = new CountAllWords(testDirectory);
        Long result = countAllWordsTask.fork().join();

        // Проверим, что файл был обработан
        assertTrue(result > 0);
    }

    @Test
    void testErrorHandling() throws IOException {
        Path invalidDirectory = Paths.get("non/existing/directory");
        CountAllWords countAllWordsTask = new CountAllWords(invalidDirectory);

        assertDoesNotThrow(countAllWordsTask::fork);
    }
}
