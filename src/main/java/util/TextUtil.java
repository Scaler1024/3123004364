package util;
import java.io.IOException;
import java.util.*;

/**
 * 文本处理
 */
public class TextUtil {

    // 内存受限的流式相似度计算
    public static double calculateStreamingSimilarityWithMemoryLimit(
            String filePath1, String filePath2, int chunkSize) throws IOException {

        long totalSize1 = FileUtil.getFileSize(filePath1);
        long totalSize2 = FileUtil.getFileSize(filePath2);
        long maxTotalSize = Math.max(totalSize1, totalSize2);

        if (maxTotalSize == 0) return 1.0;

        List<Double> chunkSimilarities = new ArrayList<>();
        long processedBytes = 0;
        int batchSize = calculateBatchSize(chunkSize);

        System.out.print("处理进度: ");

        while (processedBytes < maxTotalSize) {
            // 批量处理多个块，减少IO开销
            int chunksToProcess = (int) Math.min(
                    batchSize,
                    (maxTotalSize - processedBytes + chunkSize - 1) / chunkSize
            );

            long[] offsets1 = new long[chunksToProcess];
            long[] offsets2 = new long[chunksToProcess];
            for (int i = 0; i < chunksToProcess; i++) {
                offsets1[i] = Math.min(processedBytes + i * chunkSize, totalSize1);
                offsets2[i] = Math.min(processedBytes + i * chunkSize, totalSize2);
            }

            // 批量读取
            String[] chunks1 = FileUtil.readMultipleChunks(filePath1, offsets1, chunkSize);
            String[] chunks2 = FileUtil.readMultipleChunks(filePath2, offsets2, chunkSize);

            // 批量计算相似度
            for (int i = 0; i < chunksToProcess; i++) {
                if (chunks1[i].isEmpty() && chunks2[i].isEmpty()) {
                    continue;
                }

                double similarity = calculateEditDistanceSimilarity(chunks1[i], chunks2[i]);
                chunkSimilarities.add(similarity);
            }

            processedBytes += chunksToProcess * chunkSize;

            // 显示进度
            int progress = (int) ((double) processedBytes / maxTotalSize * 100);
            System.out.print(progress + "% ");

            // 定期清理内存
            if (chunkSimilarities.size() > 1000) {
                System.gc(); // 建议垃圾回收
            }
        }

        System.out.println();

        // 计算加权平均（考虑块大小）
        return calculateWeightedAverage(chunkSimilarities);
    }

    // 计算批量处理大小（基于内存限制）
    public static int calculateBatchSize(int chunkSize) {
        long availableMemory = 2048L * 1024 * 1024 - getCurrentMemoryUsage();
        // 每个块需要：2个字符串内存 + 算法内存
        long memoryPerChunk = chunkSize * 2 * 2 + chunkSize * chunkSize / 10;
        int maxBatch = (int) (availableMemory / memoryPerChunk);

        return Math.max(1, Math.min(maxBatch, 100)); // 限制在1-100之间
    }

    // 获取当前内存使用
    public static long getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    // 计算加权平均值
    public static double calculateWeightedAverage(List<Double> similarities) {
        if (similarities.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        for (double sim : similarities) {
            sum += sim;
        }
        return sum / similarities.size();
    }

    // 原有的编辑距离计算（已优化）
    public static double calculateEditDistanceSimilarity(String text1, String text2) {
        if (text1.isEmpty() && text2.isEmpty()) return 1.0;

        int editDistance = computeEditDistanceOptimized(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        return 1.0 - (double) editDistance / maxLength;
    }

    // 空间优化的编辑距离计算
    public static int computeEditDistanceOptimized(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();

        if (m == 0) return n;
        if (n == 0) return m;

        // 使用单数组进一步节省空间
        int[] dp = new int[n + 1];
        for (int j = 0; j <= n; j++) {
            dp[j] = j;
        }

        for (int i = 1; i <= m; i++) {
            int prev = dp[0];
            dp[0] = i;

            for (int j = 1; j <= n; j++) {
                int temp = dp[j];
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = Math.min(Math.min(dp[j] + 1, dp[j - 1] + 1), prev + 1);
                }
                prev = temp;
            }
        }

        return dp[n];
    }
}
