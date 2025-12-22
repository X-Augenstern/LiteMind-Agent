package com.xz.xzaiagent.utils;

import java.util.UUID;

public class IdUtil {
    /**
     * If client provided chatId (sessionChatId), validate and forward; otherwise generate one here
     *
     * @param chatId providedChatId
     * @return finalChatId
     */
    public static String validate_or_generate_chatId(String chatId) {
        boolean providedChatId = chatId != null && !chatId.trim().isEmpty();
        if (providedChatId) {
            chatId = chatId.trim().toLowerCase();
            if (!chatId.matches("[0-9a-f]{32}")) {
                throw new IllegalArgumentException("Invalid chatId format");
            }
        } else {
            // generate 32 hex id (uuid4 hex without dashes)
            chatId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        }
        return chatId;
    }
}
