package com.xz.xzaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具类
 */
@Slf4j
public class WebScrapingTool {

    /**
     * 最大截断长度
     */
    private static final int MAX_TRUNCATE_LEN = 400;

    @Tool(description = "抓取网页内容并返回页面 HTML")
    public String scrapeWebPage(@ToolParam(description = "要抓取的网页 URL") String url) {
        try {
            Document doc = Jsoup.connect(url).get();  // 文档对象
            String title = doc.title();
            String description = "";
            try {
                description = doc.select("meta[name=description]").attr("content");
            } catch (Exception ignored) {
            }

            // 提取正文文本并截断为合理长度
            doc.body();
            String bodyText = doc.body().text();
            boolean truncated = false;
            if (bodyText.length() > MAX_TRUNCATE_LEN) {
                bodyText = bodyText.substring(0, MAX_TRUNCATE_LEN);
                truncated = true;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("网页标题: ").append(title).append("\n");
            if (!description.isEmpty()) {
                sb.append("摘要: ").append(description).append("\n");
            }
            sb.append("正文预览:\n").append(bodyText);
            if (truncated) {
                sb.append("... [内容被截断，访问原始页面获取完整内容]");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("抓取网页出错：{}", e.getMessage());
            return "网页抓取工具：抓取网页出错！";
        }
    }
}

// EN
// Scrape the content of a web page
// URL of the web page to scrape
// Error scraping web page: