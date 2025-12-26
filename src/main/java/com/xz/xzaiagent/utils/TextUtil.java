package com.xz.xzaiagent.utils;

/**
 * Text normalization utilities shared across agents and chat components.
 */
public class TextUtil {

    /**
     * 规范化消息输出：合并多重空行为单个空行，去除行首尾空白，保留段落分隔。
     * 用于在更上游处清理 LLM / 工具 返回的文本，减少下游产生空的 SSE data 事件的概率。
     */
    public static String normalizeMessage(String raw) {
        if (raw == null) return "";

        StringBuilder sb = new StringBuilder();

        // 统一换行符：将Windows换行（\r\n）替换为Unix换行（\n），消除系统差异
        String normalized = raw.replace("\r\n", "\n");
        // 按换行分割所有行（关键：-1参数保留末尾空行，避免丢失信息）
        String[] lines = normalized.split("\n", -1);
        // 标记上一行是否是空白行（用于控制连续空行）
        boolean lastWasBlank = false;
        for (String line : lines) {
            // 跳过null行（防御性处理）
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                // 规则：仅当“上一行不是空白行”且“结果已有内容”时，才保留一个空行（避免开头/连续空行）
                if (!lastWasBlank && !sb.isEmpty()) {
                    // 保留单个空行作为段落分隔
                    sb.append("\n");
                    lastWasBlank = true;
                }
            } else {
                // 规则：如果结果已有内容，且上一行不是空白行，先加换行（避免内容粘连）
                if (!sb.isEmpty() && !lastWasBlank) {
                    sb.append("\n");
                }
                sb.append(trimmed);
                lastWasBlank = false;
            }
        }
        String out = sb.toString().trim();
        // 合并多种类型的连续空白符为单个普通空格
        out = out.replaceAll("[ \\t\\u00A0\\u2007\\u202F]+", " ");
        // 移除汉字之间的多余空格（避免汉字被空格分隔）
        out = out.replaceAll("(\\p{IsHan})\\s+(\\p{IsHan})", "$1$2");
        return out;
    }
}


