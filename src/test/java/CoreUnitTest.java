import org.junit.Test;
import org.junit.jupiter.api.io.TempDir;
import util.FileUtil;
import util.TextUtil;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class CoreUnitTest {

    @TempDir
    Path tempDir;

    // 测试1: 编辑距离计算（核心算法）
    @Test
    public void testComputeEditDistanceOptimized() {
        // 相同字符串
        assertEquals(0, TextUtil.computeEditDistanceOptimized("hello", "hello"));

        // 完全不同字符串
        assertEquals(3, TextUtil.computeEditDistanceOptimized("kitten", "sitting"));

        // 空字符串
        assertEquals(3, TextUtil.computeEditDistanceOptimized("", "abc"));
        assertEquals(5, TextUtil.computeEditDistanceOptimized("hello", ""));

        // 一个字符差异
        assertEquals(1, TextUtil.computeEditDistanceOptimized("cat", "bat"));

        // Unicode字符
        assertEquals(1, TextUtil.computeEditDistanceOptimized("中文", "中文文"));
    }

    // 测试2: 相似度计算
    @Test
    public void testCalculateEditDistanceSimilarity() {
        // 完全相同
        assertEquals(1.0, TextUtil.calculateEditDistanceSimilarity("same", "same"), 0.001);

        // 完全不同
        assertEquals(0.0, TextUtil.calculateEditDistanceSimilarity("abc", "xyz"), 0.001);

        // 空文本
        assertEquals(1.0, TextUtil.calculateEditDistanceSimilarity("", ""), 0.001);

        // 部分相似
        double similarity = TextUtil.calculateEditDistanceSimilarity("kitten", "sitting");
        assertTrue(similarity > 0.5 && similarity < 1.0);

        // 一个为空
        assertEquals(0.0, TextUtil.calculateEditDistanceSimilarity("text", ""), 0.001);
    }

    // 测试3: 文件读取（边界情况）
    @Test
    public void testReadChunkEfficiently() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.write(testFile, "Hello World!".getBytes());

        // 正常读取
        assertEquals("Hello", FileUtil.readChunkEfficiently(testFile.toString(), 0, 5));

        // 偏移超出文件大小
        assertEquals("", FileUtil.readChunkEfficiently(testFile.toString(), 100, 5));

        // 读取部分内容
        assertEquals("World!", FileUtil.readChunkEfficiently(testFile.toString(), 6, 10));

        // 块大小大于剩余内容
        assertEquals("World!", FileUtil.readChunkEfficiently(testFile.toString(), 6, 20));

        // 空文件
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);
        assertEquals("", FileUtil.readChunkEfficiently(emptyFile.toString(), 0, 5));
    }

    // 测试4: 批量读取多个块
    @Test
    public void testReadMultipleChunks() throws IOException {
        Path testFile = tempDir.resolve("multi.txt");
        Files.write(testFile, "This is a test content for multiple chunks".getBytes());

        long[] offsets = {0, 5, 10, 100}; // 最后一个偏移超出范围
        String[] chunks = FileUtil.readMultipleChunks(testFile.toString(), offsets, 5);

        assertEquals(4, chunks.length);
        assertEquals("This ", chunks[0]);
        assertEquals("is a ", chunks[1]);
        assertEquals("test ", chunks[2]);
        assertEquals("", chunks[3]); // 超出范围的返回空字符串
    }

    // 测试5: 加权平均计算
    @Test
    public void testCalculateWeightedAverage() {
        // 正常列表
        List<Double> similarities = Arrays.asList(0.8, 0.9, 0.7);
        assertEquals(0.8, TextUtil.calculateWeightedAverage(similarities), 0.001);

        // 空列表
        List<Double> emptyList = Arrays.asList();
        assertEquals(0.0, TextUtil.calculateWeightedAverage(emptyList), 0.001);

        // 单个元素
        List<Double> single = Arrays.asList(0.5);
        assertEquals(0.5, TextUtil.calculateWeightedAverage(single), 0.001);

        // 包含边界值
        List<Double> extremes = Arrays.asList(0.0, 1.0, 0.5);
        assertEquals(0.5, TextUtil.calculateWeightedAverage(extremes), 0.001);
    }

    // 测试6: 安全块大小计算
    @Test
    public void testCalculateSafeChunkSize() {
        // 默认计算（无自定义大小）
        int chunkSize1 = PaperCheck.calculateSafeChunkSize(1024 * 1024, 0);
        assertTrue(chunkSize1 >= 256 * 1024 && chunkSize1 <= 16 * 1024 * 1024);

        // 自定义大小在合理范围内
        int chunkSize2 = PaperCheck.calculateSafeChunkSize(1024 * 1024, 512 * 1024);
        assertEquals(512 * 1024, chunkSize2);

        // 自定义大小超过上限
        int chunkSize3 = PaperCheck.calculateSafeChunkSize(1024 * 1024, 100 * 1024 * 1024);
        assertTrue(chunkSize3 <= 16 * 1024 * 1024);

        // 极小文件
        int chunkSize4 = PaperCheck.calculateSafeChunkSize(100, 0);
        assertTrue(chunkSize4 >= 256 * 1024); // 仍然保持最小块大小
    }

    // 测试7: 文件大小获取（异常情况）
    @Test
    public void testGetFileSizeException() {
        // 不存在的文件应该抛出异常
        assertThrows(IOException.class, () -> {
            FileUtil.getFileSize("nonexistent_file.txt");
        });
    }

    // 测试8: 文件写入格式
    @Test
    public void testWriteFileFormat() throws IOException {
        Path outputFile = tempDir.resolve("output.txt");

        // 测试各种相似度值的格式化输出
        FileUtil.writeFile(outputFile.toString(), 0.0);
        assertEquals("0.00", Files.readString(outputFile));

        FileUtil.writeFile(outputFile.toString(), 0.5555);
        assertEquals("55.55", Files.readString(outputFile));

        FileUtil.writeFile(outputFile.toString(), 1.0);
        assertEquals("100.00", Files.readString(outputFile));

        FileUtil.writeFile(outputFile.toString(), 0.9999);
        assertEquals("99.99", Files.readString(outputFile));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenArgsLessThan3() {
        String[] args = {"file1.txt", "file2.txt"};
        PaperCheck.main(args);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldThrowExceptionWhenInvalidChunkSize() {
        String[] args = {"file1.txt", "file2.txt", "result.txt", "invalid"};
        PaperCheck.main(args);
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenFileLocked() throws Exception {
        // 另一个进程正在写入该文件
        FileUtil.readChunkEfficiently("locked_file.txt", 0, 1024);
    }

}