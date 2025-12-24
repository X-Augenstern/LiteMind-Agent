package com.xz.xzaiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 网页抓取工具类
 */
public class WebScrapingTool {

    @Tool(name = "网页抓取", description = "抓取网页内容并返回页面 HTML")
    public String scrapeWebPage(@ToolParam(description = "要抓取的网页 URL") String url) {
        try {
            Document doc = Jsoup.connect(url).get();  // 文档对象
            return doc.html();  // 得到完整的网页内容（String 类型的字符串）
        } catch (Exception e) {
            return "抓取网页出错：" + e.getMessage();
        }
    }
}

// EN
// Scrape the content of a web page
// URL of the web page to scrape
// Error scraping web page: