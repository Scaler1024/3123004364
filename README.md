# 文本查重工具
本项目实现了一个基于编辑距离的文本查重率计算工具。通过比较两篇文本的相似度，并将结果写入指定文件。

## 使用方式
命令格式
```
Java: java -jar main.jar  [orig_file] [other_file] [res_file]
```
命令参数
* orig_file：参考文件地址
* other_file：修改后文件的地址
* res_file：结果文件地址

## 代码框架
* 主程序入口 (PaperCheck.java)：负责命令行交互、流程控制和模块协调。

* 文件工具类 (FileUtil.java)：负责所有文件读写操作，封装I/O细节。

* 文本处理工具类 (TextUtil.java)：负责核心算法，计算文本相似度。


