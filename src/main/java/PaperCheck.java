import java.io.IOException;
import static util.FileUtil.readFile;
import static util.FileUtil.writeFile;
import static util.TextUtil.calculateEditDistanceSimilarity;

public class PaperCheck {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("用法: java DocumentSimilarityChecker 文件1 文件2 结果文件");
            return;
        }

        String originalFilePath = args[0];
        String plagiarizedFilePath = args[1];
        String resultFilePath = args[2];

        try {
            // 读取文件内容
            String originalText = readFile(originalFilePath);
            String plagiarizedText = readFile(plagiarizedFilePath);
            double editDistSim = calculateEditDistanceSimilarity(originalText, plagiarizedText);

            // 写入结果
            writeFile(resultFilePath, editDistSim);

            System.out.println("查重完成！");
            System.out.printf("相似度: %.2f%%\n", editDistSim * 100);

        } catch (IOException e) {
            System.err.println("处理文件时出错: " + e.getMessage());
        }
    }

}
