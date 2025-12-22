package com.xz.xzaiagent.controller;

import com.xz.xzaiagent.agent.ActiveAgentRegistry;
import com.xz.xzaiagent.agent.LiteMind;
import com.xz.xzaiagent.app.LoveApp;
import com.xz.xzaiagent.app.SimpleChat;
import com.xz.xzaiagent.utils.IdUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;


@RestController
@RequestMapping("/ai")
public class AIController {

    @Resource
    private LoveApp loveApp;
    @Resource
    private ToolCallback[] allTools;
    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private SimpleChat simpleChat;

    @Resource
    private ActiveAgentRegistry activeAgentRegistry;


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
     * SSE 流式调用 LLM（简单对话模式）
     * 与 /liteMind/chat 的区别：
     * - 此接口：单次思考，直接回答，适合简单对话
     * - /liteMind/chat：深度思考，多轮思考-行动循环，适合复杂任务
     *
     * @param message 用户消息
     * @param chatId  对话ID（用于多轮对话记忆）
     * @return SSE流式响应
     */
    @GetMapping("/chat/simple")
    public SseEmitter doSimpleChat(String message, String chatId) {
        String finalChatId = IdUtil.validate_or_generate_chatId(chatId);

        // register placeholder so terminate can be called even before stream is fully established
        activeAgentRegistry.register(finalChatId, null, null, null);

        // forward chatId upstream so Agent uses it as conversation id
        SseEmitter sse = simpleChat.doChatByStream(message, finalChatId);

        // send initial chatId info to client
        try {
            sse.send("__CHAT_ID__:" + finalChatId);
        } catch (IOException ignored) {
        }

        // registry will be updated by SimpleChat when disposable is available
        return sse;
    }

    /**
     * SSE 流式调用 LiteMind（深度思考模式）
     */
    @GetMapping("/chat/liteMind")
    public SseEmitter doChatWithLiteMind(String message, String chatId) {
        String finalChatId = IdUtil.validate_or_generate_chatId(chatId);

        // register placeholder so terminate can be called even before Agent registers
        activeAgentRegistry.register(finalChatId, null, null, null);

        // create new LiteMind instance and start stream, forwarding chatId upstream
        LiteMind liteMind = new LiteMind(allTools, dashscopeChatModel);
        // inform agent of requested chatId for internal registration
        liteMind.setRequestedChatId(finalChatId);
        liteMind.setActiveAgentRegistry(activeAgentRegistry);
        SseEmitter sse = liteMind.runByStream(message);

        // send initial chatId info to client
        try {
            sse.send("__CHAT_ID__:" + finalChatId);
        } catch (IOException ignored) {
        }

        // register actual agent entry (override placeholder)
        activeAgentRegistry.register(finalChatId, liteMind, sse, null);
        return sse;
    }

    /**
     * 外部终止接口：根据 chatId 终止正在运行的 Agent / SSE 流
     */
    @PostMapping("/chat/terminate")
    public ResponseEntity<java.util.Map<String, Object>> terminateChat(
            @RequestParam("chatId") String chatId,
            @RequestParam(name = "final", required = false, defaultValue = "false") boolean hard
    ) {
        boolean ok = activeAgentRegistry.terminate(chatId, hard);
        Map<String, Object> body = new HashMap<>();
        body.put("chatId", chatId);
        if (ok) {
            body.put("ok", true);
            body.put("result", "terminated");
            return ResponseEntity.ok(body);
        } else {
            body.put("ok", false);
            body.put("result", "not_found");
            return ResponseEntity.status(NOT_FOUND).body(body);
        }
    }
}
