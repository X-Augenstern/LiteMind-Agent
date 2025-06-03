package com.xz.xzaiagent.controller;

import com.xz.xzaiagent.agent.LiteMind;
import com.xz.xzaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private LoveApp loveApp;
    @Resource
    private ToolCallback[] allTools;
    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用
     */
    @GetMapping("/loveApp/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用（1）
     * <p>
     * MediaType.TEXT_EVENT_STREAM_VALUE: HTTP 响应头中就包含要以文本流的形式返回，前端就能一个接一个地拿到文本片段了
     */
    @GetMapping(value = "/loveApp/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用（2）
     * <p>
     * ServerSentEvent再给String包一层，此时就不需要再手动指定响应头了，框架会自动添加
     */
    @GetMapping("/loveApp/chat/sse2")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE2(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                // 声明泛型类/接口/变量时，泛型在类名后，作用于整个类或接口。List<String>：声明对象、类的时候，类型写在类名后
                // 调用静态泛型方法时，在方法名或构造器前显式指定泛型类型。调用泛型静态方法时，类型写在方法名前（或者叫“类名和方法名之间”）
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用（3）
     * <p>
     * 最灵活，可以自行控制每次收到片段要做的事情
     */
    @GetMapping("/loveApp/chat/sse3")
    public SseEmitter doChatWithLoveAppSSE3(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);  // 设置 3min 超时时间
        // 获取 Flux 响应式数据流并直接通过订阅推送给 SseEmitter
        loveApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        // SSE 推送一定要及时关闭，否则会一直占用连接
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

    /**
     * SSE 流式调用 LiteMind
     */
    @GetMapping("/liteMind/chat")
    public SseEmitter doChatWithLiteMind(String message) {
        // 不能使用自动注入，因为这是一个单例，每次使用必须 new 一个新的，防止各个用户调用同一个 LiteMind 造成阻塞
        LiteMind liteMind = new LiteMind(allTools, dashscopeChatModel);
        return liteMind.runByStream(message);
    }
}
