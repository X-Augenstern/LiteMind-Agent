package com.xz.xzaiagent.app;

import com.xz.xzaiagent.advisor.MyLoggerAdvisor;
import com.xz.xzaiagent.chatmemory.InFileChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 简单对话服务 - 单次思考模式（与LiteMind Agent（深度思考模式）形成对比）
 */
@Component
@Slf4j
public class SimpleChat {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是一个深耕医疗领域多年的专家，能够回答用户的各种关于医疗方面的问题。";

    /**
     * 初始化简单对话客户端
     */
    public SimpleChat(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new InFileChatMemory(fileDir);

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * 简单对话 - SSE流式输出（单次思考模式）
     *
     * @param message 用户消息
     * @param chatId  对话ID（用于多轮对话记忆）
     * @return SSE流式响应
     */
    public SseEmitter doChatByStream(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3分钟超时

        // 使用异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
                // 获取流式响应
                Flux<String> contentFlux = chatClient
                        .prompt()
                        .user(message)
                        .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                        .stream()
                        .content();

                // 订阅流式数据并发送给客户端
                contentFlux.subscribe(
                        chunk -> {
                            try {
                                if (chunk != null && !chunk.isEmpty()) {
                                    sseEmitter.send(chunk);
                                }
                            } catch (IOException e) {
                                log.error("发送SSE消息失败", e);
                                sseEmitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("流式响应出错", error);
                            try {
                                sseEmitter.send("抱歉，处理您的请求时遇到错误，请稍后重试。");
                                sseEmitter.complete();
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        () -> {
                            // 流式响应完成
                            try {
                                sseEmitter.send("[DONE]");
                                sseEmitter.complete();
                            } catch (IOException e) {
                                sseEmitter.completeWithError(e);
                            }
                        }
                );
            } catch (Exception e) {
                log.error("处理对话请求失败", e);
                try {
                    sseEmitter.send("处理请求时遇到错误，请稍后重试。");
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            }
        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            sseEmitter.complete();
        });

        // 设置完成回调
        sseEmitter.onCompletion(() -> log.info("SSE连接完成"));

        return sseEmitter;
    }
}