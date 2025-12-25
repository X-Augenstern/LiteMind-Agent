package com.xz.xzaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 联网搜索工具类
 */
@Slf4j
public class WebSearchTool {

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 最大截断长度
     */
    private static final int MAX_TRUNCATE_LEN = 400;

    /**
     * 取出返回结果的前 5 条
     */
    private static final int TOP_5 = 5;

    /**
     * 最大片段长度
     */
    private static final int MAX_SNIPPET_LEN = 200;

    @Tool(description = "使用百度搜索引擎检索信息")
    public String searchWeb(@ToolParam(description = "搜索关键词") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            // 提取可能的结果字段（兼容不同接口命名）
            JSONArray organicResults = null;
            if (jsonObject.containsKey("organic_results")) {
                organicResults = jsonObject.getJSONArray("organic_results");
            } else if (jsonObject.containsKey("organicResults")) {
                organicResults = jsonObject.getJSONArray("organicResults");
            } else if (jsonObject.containsKey("results")) {
                organicResults = jsonObject.getJSONArray("results");
            }
            if (organicResults == null || organicResults.isEmpty()) {
                // 返回结构可能变更或无结果，记录并返回简洁错误提示（同时附带响应片段便于排查）
                String snippet = response.length() > MAX_TRUNCATE_LEN ? response.substring(0, MAX_TRUNCATE_LEN) : response;
                log.warn("联网搜索工具：未找到结果或搜索接口返回结构已变更。响应片段：{}", snippet);
                return "联网搜索工具：未找到结果或搜索接口返回结构已变更。";
            }

            int limit = Math.min(organicResults.size(), TOP_5);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                Object o = organicResults.get(i);
                if (!(o instanceof JSONObject item)) {
                    sb.append(o.toString());
                    if (i < limit - 1) sb.append("\n\n");
                    continue;
                }
                String title = item.getStr("title", item.getStr("title_no_formatting", ""));
                String snippetText = item.getStr("snippet", item.getStr("description", ""));
                // 保护长度
                if (snippetText != null && snippetText.length() > MAX_SNIPPET_LEN) {
                    snippetText = snippetText.substring(0, MAX_SNIPPET_LEN) + "...";
                }
                String link = item.getStr("link", item.getStr("url", ""));

                sb.append("- ").append(title != null ? title : "<no title>");
                if (link != null && !link.isEmpty()) {
                    sb.append(" (").append(link).append(")");
                }
                if (snippetText != null && !snippetText.isEmpty()) {
                    sb.append("\n  ").append(snippetText);
                }
                if (i < limit - 1) sb.append("\n\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("使用百度检索失败：{}", e.getMessage());
            return "联网搜索工具：使用百度检索失败！";
        }
    }
}

// EN
// Search for information from Baidu Search Engine
// Search query keyword
// Error searching Baidu: