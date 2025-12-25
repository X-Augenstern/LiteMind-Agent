package com.xz.xzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.xz.xzaiagent.constant.FileConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类（提供文件读写功能）
 */
@Slf4j
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "从文件中读取内容")
    public String readFile(@ToolParam(description = "要读取的文件名") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            log.error("读取文件出错：{}", e.getMessage());
            return "文件操作工具：读取文件出错！";
        }
    }

    @Tool(description = "把内容写入文件")
    public String writeFile(@ToolParam(description = "要写入的文件名") String fileName,
                            @ToolParam(description = "要写入的内容") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "文件已成功保存：" + filePath;
        } catch (Exception e) {
            log.error("写入文件出错：{}", e.getMessage());
            return "文件操作工具：写入文件出错！";
        }
    }
}

// EN
// Read content from a file
// Name of a file to read
// Error reading file:
// Write content to file
// Name of the file to write
// Content to write to the file
// File written successfully to:
// Error writing to file: