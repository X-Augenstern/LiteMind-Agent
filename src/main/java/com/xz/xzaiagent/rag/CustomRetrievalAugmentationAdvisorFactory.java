package com.xz.xzaiagent.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

/**
 * 创建自定义 RAG 检索增强顾问的工厂
 */
@Component
public class CustomRetrievalAugmentationAdvisorFactory {

    /**
     * 创建自定义 RAG 检索增强顾问
     */
    public static Advisor createCustomRetrievalAugmentationAdvisor(VectorStore vectorStore, String status) {
        // 过滤特定状态的文档表达式
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();

        VectorStoreDocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)  // 过滤规则
                .similarityThreshold(0.5)  // 相似度阈值
                .topK(3)  // 返回文档数量
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(CustomContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
