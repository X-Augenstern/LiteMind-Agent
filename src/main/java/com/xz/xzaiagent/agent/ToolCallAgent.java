package com.xz.xzaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.xz.xzaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 处理工具调用的代理抽象类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    /**
     * 可调用的工具
     */
    private final ToolCallback[] availableTools;

    /**
     * 保存工具调用信息的响应结果（要调用哪些工具）
     */
    private ChatResponse toolCallChatResponse;

    /**
     * 工具调用管理者
     */
    private final ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 1、校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        // 2、调用 LLM，记录响应结果，用于后续 Act
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            this.toolCallChatResponse = getChatClient()
                    .prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();

            // 3、解析响应结果，获取要调用的工具
            // 获取助手信息
            AssistantMessage assistantMessage = this.toolCallChatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String res = assistantMessage.getText();
            log.info(getName() + "'s thoughts: " + res);
            log.info("{} selected {} tools to use", getName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("Tool name: %s, param: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            if (toolCallList.isEmpty()) {
                // 如果不需要调用工具，需要单独把助手消息添加到上下文中
                getMessageList().add(assistantMessage);
                return false;
            } else
                // 如果需要调用工具，不需要记入助手消息，在 Act 中才会执行工具，执行工具后才会有结果，这个结果里是包含助手消息的
                return true;
        } catch (Exception e) {
            log.error("Oops! The " + getName() + "'s thinking process hit a snag: " + e.getMessage());
            // 即使抛出异常了也要告诉 AI 错误信息
            getMessageList().add(new AssistantMessage("Error encountered while processing: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!this.toolCallChatResponse.hasToolCalls())
            return "No tools need to be called";

        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), this.getChatOptions());
        ToolExecutionResult toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, this.toolCallChatResponse);

        // 记录信息的上下文，conversationHistory 已经包含了助手信息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());

        // 工具调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(this.getMessageList());
        String res = toolResponseMessage.getResponses().stream()
                .map(response -> "Tool " + response.name() + " completed its mission! Result: " + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(res);

        // 判断是否调用了终止工具
        handleSpecialTool(toolResponseMessage);

        return res;
    }

    private void handleSpecialTool(ToolResponseMessage tMsg) {
        Optional<ToolResponseMessage.ToolResponse> terminateToolCalled = tMsg.getResponses().stream()
                .filter(response -> response.name().equals("doTerminate"))
                .findFirst();
        if (terminateToolCalled.isPresent()) {
            // 任务结束，更改状态
            log.info("Special tool {} has completed the task!", terminateToolCalled.get().name());
            setState(AgentState.FINISHED);
        }
    }
}
