package com.xz.xzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.xz.xzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 资源下载工具类
 */
public class ResourceDownloadTool {

    @Tool(name = "资源下载", description = "从指定 URL 下载资源并保存为文件")
    public String downloadResource(@ToolParam(description = "要下载资源的 URL") String url,
                                   @ToolParam(description = "保存的文件名") String fileName) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);  // 创建目录
            HttpUtil.downloadFile(url, new File(filePath));
            return "资源已下载并保存：" + filePath;
        } catch (Exception e) {
            return "下载资源失败：" + e.getMessage();
        }
    }
}

// EN
// Download a response from a given URL
// URL of the resource to download
// Name of the file to save the download resource
// Resource download successfully to:
// Error downloading resource: