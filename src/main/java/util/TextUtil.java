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
}
