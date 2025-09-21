package util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件处理
 */
public class FileUtil {

    public static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8).trim();
    }

    public static void writeFile(String outputFilePath, double similarity) throws IOException {
        String result = String.format("%.2f", similarity * 100);
        Files.write(Paths.get(outputFilePath), result.getBytes(StandardCharsets.UTF_8));
    }

    // 高效的分块读取（使用RandomAccessFile避免重复打开文件）
    public static String readChunkEfficiently(String filePath, long offset, int chunkSize) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            if (offset >= raf.length()) {
                return "";
            }

            raf.seek(offset);
            byte[] buffer = new byte[chunkSize];
            int bytesRead = raf.read(buffer, 0, chunkSize);

            if (bytesRead == -1) {
                return "";
            }

            return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
        }
    }

    // 获取文件大小
    public static long getFileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }

    // 批量读取多个块（减少IO操作）
    public static String[] readMultipleChunks(String filePath, long[] offsets, int chunkSize) throws IOException {
        String[] chunks = new String[offsets.length];
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            for (int i = 0; i < offsets.length; i++) {
                if (offsets[i] >= raf.length()) {
                    chunks[i] = "";
                    continue;
                }

                raf.seek(offsets[i]);
                byte[] buffer = new byte[chunkSize];
                int bytesRead = raf.read(buffer, 0, chunkSize);
                chunks[i] = bytesRead == -1 ? "" : new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            }
        }
        return chunks;
    }
}