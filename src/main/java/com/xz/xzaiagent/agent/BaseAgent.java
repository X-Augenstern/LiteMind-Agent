package com.xz.xzaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.xz.xzaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.xz.xzaiagent.agent.prompt.LiteMind.STUCK_PROMPT_CH;


/**
 * 基础代理抽象类，用于管理代理状态和执行流程
 * <p>
 * 提供状态管理、内存管理和基于步骤的执行循环等基础功能
 * 子类必须实现 step 方法
 */
@Data
@Slf4j
public abstract class BaseAgent {

    /**
     * 名字
     */
    private String name;

    /**
     * 系统提示词
     */
    private String systemPrompt;
    /**
     * 下一步提示词
     */
    private String nextStepPrompt;

    /**
     * 代理状态
     */
    private AgentState state = AgentState.IDLE;

    /**
     * 初始化步骤
     */
    private int currentStep = 0;
    /**
     * 最大执行步骤
     */
    private int maxSteps = 10;

    /**
     * 依赖-LLM
     */
    private ChatClient chatClient;
    /**
     * 依赖-记忆
     */
    private List<Message> messageList = new ArrayList<>();

    /**
     * 最多允许代理输出重复消息的次数
     */
    private int duplicateThreshold = 2;

    /**
     * 当前步骤的用户友好消息（用于SSE流式输出）
     */
    // private String currentUserMessage; // 转为从 messageList 中读取 AssistantMessage，避免双重状态管理

    @Resource
    private ActiveAgentRegistry activeAgentRegistry;

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 基础校验
        if (this.state != AgentState.IDLE)
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        if (StrUtil.isBlank(userPrompt))
            throw new RuntimeException("Cannot run agent with empty user prompt");

        // 状态改变
        this.state = AgentState.RUNNING;

        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();

        try {
            // 执行循环
            while (currentStep < maxSteps && this.state != AgentState.FINISHED) {
                currentStep++;
                log.info("Executing step {}/{}", currentStep, maxSteps);

                // 单步执行
                String res = step();

                // 检查是否陷入循环
                if (isStuck())
                    handleStuckState();

                results.add("Step " + currentStep + ": " + res);
            }

            if (currentStep >= maxSteps) {
                currentStep = 0;
                this.state = AgentState.IDLE;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            this.state = AgentState.ERROR;
            String errMsg = "Error executing agent: " + e;
            log.error(errMsg);
            return errMsg;
        } finally {
            this.cleanUp();
        }
    }

    /**
     * 运行代理（流式输出）
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public SseEmitter runByStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);

        // 使用线程异步处理，避免阻塞主线程，否则会等到循环执行完才把 sseEmitter 返回出去，结果还是同步调用
        CompletableFuture.runAsync(() -> {
            // 基础校验
            try {
                if (this.state != AgentState.IDLE) {
                    safeSend(sseEmitter, "当前状态无法启动智能体: " + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    safeSend(sseEmitter, "用户提示词消息内容不能为空，无法启动智能体");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }

            // 状态改变
            this.state = AgentState.RUNNING;

            messageList.add(new UserMessage(userPrompt));

            try {
                // 执行循环
                while (currentStep < maxSteps && this.state != AgentState.FINISHED) {
                    currentStep++;
                    log.info("当前执行步骤：{}/{}", currentStep, maxSteps);

                    // 单步执行
                    String res = step();

                    // 检查是否陷入循环
                    if (isStuck())
                        handleStuckState();

                    // 输出当前每一步的结果到 SSE，推送给客户端
                    if (StrUtil.isNotBlank(res)) {
                        safeSend(sseEmitter, currentStep > 1 ? "\n" : "" + "【步骤" + currentStep + "】" + res);
                    }
                }

                if (currentStep >= maxSteps) {
                    currentStep = 0;
                    this.state = AgentState.IDLE;
                    safeSend(sseEmitter, "已达到最大执行步骤（" + maxSteps + "），任务已终止");
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                this.state = AgentState.ERROR;
                String errMsg = "智能体执行过程中出错：" + e;
                log.error(errMsg, e);
                try {
                    safeSend(sseEmitter, errMsg);
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                this.cleanUp();
            }
        });

        // 设置超时回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanUp();
            log.warn("SSE 连接超时。");
        });

        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING)
                this.state = AgentState.FINISHED;
            this.cleanUp();
            log.info("SSE 连接完毕。");
        });

        return sseEmitter;
    }


    /**
     * Helper to send SSE messages and register generated chatId if present.
     */
    private void safeSend(SseEmitter emitter, String message) throws IOException {
        if (message == null) return;
        if (message.startsWith("__CHAT_ID__:")) {
            String genId = message.substring("__CHAT_ID__:".length()).trim();
            if (genId.matches("[0-9a-fA-F]{32}")) {
                String normalized = genId.toLowerCase();
                try {
                    activeAgentRegistry.register(normalized, this, emitter, null);
                    log.info("已把生成的 chatId {} 注册进 ActiveAgentRegistry", normalized);
                } catch (Exception e) {
                    log.warn("注册生成的 chatId 失败 {}", normalized, e);
                }
            }
        }
        emitter.send(message);
    }

    /**
     * 定义单个步骤，交给子类去实现
     */
    public abstract String step();

    /**
     * 检查代理是否陷入循环
     *
     * @return 是否陷入循环
     */
    protected boolean isStuck() {
        List<Message> messageList = this.getMessageList();
        if (messageList.size() < 2)
            return false;

        Message lastMessage = messageList.getLast();
        if (StrUtil.isBlank(lastMessage.getText()))
            return false;

        int duplicateCount = 0;
        for (int i = messageList.size() - 2; i >= 0; i--) {
            Message tmp = messageList.get(i);
            if (tmp.getMessageType() == MessageType.ASSISTANT && tmp.getText().equals(lastMessage.getText()))
                duplicateCount++;
        }

        return duplicateCount >= this.duplicateThreshold;
    }

    /**
     * 处理陷入循环的状态
     */
    protected void handleStuckState() {
        this.nextStepPrompt = STUCK_PROMPT_CH + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        log.warn("智能体检测到循环状态，补充提示词：{}", STUCK_PROMPT_CH);
    }

    /**
     * 清理资源
     */
    protected void cleanUp() {
    }
}
