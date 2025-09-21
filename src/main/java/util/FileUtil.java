package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件处理
 */
public class FileUtil {

    public static String readFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath))).trim();
    }

    public static void writeFile(String outputFilePath, double similarity) throws IOException {
        String result = String.format("%.2f", similarity);
        Files.write(Paths.get(outputFilePath), result.getBytes());
    }
}