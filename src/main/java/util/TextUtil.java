package util;
import java.util.*;

/**
 * 文本处理
 */
public class TextUtil {

    // 计算基于编辑距离的相似度[1,7,10](@ref)
    public static double calculateEditDistanceSimilarity(String text1, String text2) {
        int editDistance = computeEditDistance(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());

        if (maxLength == 0) {
            return 1.0;
        }

        return 1.0 - (double) editDistance / maxLength;
    }

    // 计算编辑距离（Levenshtein距离）[1,7](@ref)
    private static int computeEditDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] dp = new int[m + 1][n + 1];

        // 初始化边界条件
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // 动态规划计算编辑距离
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(
                                    dp[i - 1][j] + 1,     // 删除
                                    dp[i][j - 1] + 1),    // 插入
                                    dp[i - 1][j - 1] + 1  // 替换
                    );
                }
            }
        }

        return dp[m][n];
    }

//    private static int computeEditDistance(String s1, String s2) {
//        int m = s1.length();
//        int n = s2.length();
//
//        // 优化：使用两行数组代替完整的二维矩阵
//        int[] prev = new int[n + 1];
//        int[] curr = new int[n + 1];
//
//        // 初始化第一行（空字符串到s2的编辑距离）
//        for (int j = 0; j <= n; j++) {
//            prev[j] = j;
//        }
//
//        // 动态规划计算编辑距离
//        for (int i = 1; i <= m; i++) {
//            // 每行开始时，初始化当前行的第一个元素
//            curr[0] = i;
//
//            for (int j = 1; j <= n; j++) {
//                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
//                    curr[j] = prev[j - 1];  // 字符相同，继承左上角值
//                } else {
//                    curr[j] = Math.min(Math.min(
//                                    prev[j] + 1,     // 删除（来自上一行）
//                                    curr[j - 1] + 1),    // 插入（来自左边）
//                            prev[j - 1] + 1  // 替换（来自左上角）
//                    );
//                }
//            }
//
//            // 交换数组引用，准备下一轮计算
//            int[] temp = prev;
//            prev = curr;
//            curr = temp;
//        }
//
//        // 最终结果在prev数组中（因为最后交换了一次）
//        return prev[n];
//    }
}
