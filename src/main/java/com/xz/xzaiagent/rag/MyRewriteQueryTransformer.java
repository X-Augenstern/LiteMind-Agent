package com.xz.xzaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * 重写查询转换器
 */
@Component
public class MyRewriteQueryTransformer {

    private final QueryTransformer queryTransformer;

    public MyRewriteQueryTransformer(ChatModel dashscopeChatModel) {
        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(ChatClient.builder(dashscopeChatModel))
                .build();
    }

    /**
     * 查询重写
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        // 执行查询重写
        Query transformedQuery = queryTransformer.transform(query);
        // 输出重写后的查询
        return transformedQuery.text();
    }
}
