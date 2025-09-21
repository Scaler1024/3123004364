import java.io.IOException;
import static util.FileUtil.*;
import static util.TextUtil.*;
import java.util.concurrent.atomic.AtomicLong;

public class PaperCheck {

    // 内存限制：2048MB
    private static final long MAX_MEMORY_BYTES = 2048L * 1024 * 1024;
    private static final int DEFAULT_CHUNK_SIZE = 1024 * 1024; // 1MB默认块大小

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("用法: java PaperCheck 文件1 文件2 结果文件 [自定义块大小]");
            System.out.println("内存限制: 2048MB");
            return;
        }

        String originalFilePath = args[0];
        String plagiarizedFilePath = args[1];
        String resultFilePath = args[2];
        int customChunkSize = args.length > 3 ? Integer.parseInt(args[3]) : 0;

        try {
            // 获取文件信息
            long originalSize = getFileSize(originalFilePath);
            long plagiarizedSize = getFileSize(plagiarizedFilePath);
            long maxFileSize = Math.max(originalSize, plagiarizedSize);

            // 计算安全块大小（考虑内存限制）
            int chunkSize = calculateSafeChunkSize(maxFileSize, customChunkSize);

            System.out.printf("文件1大小: %.2f MB\n", originalSize / (1024.0 * 1024));
            System.out.printf("文件2大小: %.2f MB\n", plagiarizedSize / (1024.0 * 1024));
            System.out.printf("使用块大小: %d KB\n", chunkSize / 1024);

            double similarity;
            if (maxFileSize > calculateMemoryThreshold()) {
                // 大文件使用流式处理
                System.out.println("使用流式查重模式...");
                similarity = calculateStreamingSimilarityWithMemoryLimit(
                        originalFilePath, plagiarizedFilePath, chunkSize);
            } else {
                // 小文件使用内存处理
                System.out.println("使用内存查重模式...");
                String originalText = readFile(originalFilePath);
                String plagiarizedText = readFile(plagiarizedFilePath);
                similarity = calculateEditDistanceSimilarity(originalText, plagiarizedText);
            }

            // 写入结果
            writeFile(resultFilePath, similarity);

            System.out.println("查重完成！");
            System.out.printf("相似度: %.2f%%\n", similarity * 100);

        } catch (IOException e) {
            System.err.println("处理文件时出错: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 计算安全块大小
    private static int calculateSafeChunkSize(long maxFileSize, int customChunkSize) {
        if (customChunkSize > 0) {
            return Math.min(customChunkSize, (int) (MAX_MEMORY_BYTES / 4));
        }

        // 动态计算块大小：确保同时处理2个块+算法内存不超过限制
        long availableMemory = MAX_MEMORY_BYTES - getCurrentMemoryUsage();
        long safeChunkSize = (availableMemory / 8); // 保守估计，留出足够空间

        // 限制块大小在合理范围内
        int chunkSize = (int) Math.min(safeChunkSize, 16 * 1024 * 1024); // 最大16MB
        chunkSize = Math.max(chunkSize, 256 * 1024); // 最小256KB

        return chunkSize;
    }

    // 计算内存使用阈值（决定使用哪种模式）
    private static long calculateMemoryThreshold() {
        long availableMemory = MAX_MEMORY_BYTES - getCurrentMemoryUsage();
        return availableMemory / 2; // 使用一半可用内存作为阈值
    }

    // 获取当前内存使用量
    private static long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}