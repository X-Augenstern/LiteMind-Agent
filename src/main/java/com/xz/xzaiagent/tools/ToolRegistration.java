package com.xz.xzaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 集中注册类
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String apiKey;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        // AskHumanTool askHumanTool = new AskHumanTool();
        return ToolCallbacks.from(  // filter(toolMethod -> toolMethod.isAnnotationPresent(Tool.class)) 会去识别 @Tool 注解为 AI 可调用的工具
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                terminalOperationTool,
                resourceDownloadTool,
                pdfGenerationTool,
                terminateTool
                // askHumanTool 先不主动询问用户
        );
    }
}
