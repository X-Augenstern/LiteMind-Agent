package com.xz.xzaiagent.constant;

import lombok.Getter;

@Getter
public enum TextTruncate {

    /**
     * HTML 最大截断长度
     */
    MAX_HTML_LEN(600),

    /**
     * JSON 最大截断长度
     */
    MAX_JSON_LEN(800),

    /**
     * 长文本 最大截断长度
     */
    MAX_LONG_TEXT_LEN(600);

    private final int value;

    TextTruncate(int value) {
        this.value = value;
    }
}
