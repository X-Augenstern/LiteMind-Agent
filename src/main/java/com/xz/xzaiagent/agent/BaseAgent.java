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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
                    sseEmitter.send("Cannot run agent from state: " + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("Cannot run agent with empty user prompt");
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
                    log.info("Executing step {}/{}", currentStep, maxSteps);

                    // 单步执行
                    String res = step();

                    // 检查是否陷入循环
                    if (isStuck())
                        handleStuckState();

                    // 输出当前每一步的结果到 SSE，推送给客户端
                    sseEmitter.send("Step " + currentStep + ": " + res);
                }

                if (currentStep >= maxSteps) {
                    currentStep = 0;
                    this.state = AgentState.IDLE;
                    sseEmitter.send("Terminated: Reached max steps (" + maxSteps + ")");
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                this.state = AgentState.ERROR;
                String errMsg = "Error executing agent: " + e;
                log.error(errMsg);
                try {
                    sseEmitter.send(errMsg);
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
            log.warn("SSE connection timeout");
        });

        // 设置完成回调
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING)
                this.state = AgentState.FINISHED;
            this.cleanUp();
            log.info("SSE connection completed");
        });

        return sseEmitter;
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

        Message lastMessage = messageList.get(messageList.size() - 1);
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
        String stuckPrompt = "Observed duplicate responses. Consider new strategies and avoid repeating ineffective paths already attempted";
        this.nextStepPrompt = stuckPrompt + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        log.warn("Agent detected stuck state. Added prompt: {}", stuckPrompt);
    }

    /**
     * 清理资源
     */
    protected void cleanUp() {
    }
}
