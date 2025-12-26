package com.xz.xzaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.xz.xzaiagent.agent.model.AgentState;
import com.xz.xzaiagent.utils.TextUtil;
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

import org.jsoup.Jsoup;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import static com.xz.xzaiagent.constant.TextTruncate.*;

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
    public ThinkResponse think() {
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
            // 规范化返回文本，移除多余空行并 trim
            res = TextUtil.normalizeMessage(res);
            log.info("{} 在本轮的思考结果为：{}", getName(), res);
            log.info("{} 挑选了 {} 个工具来使用", getName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名: %s, 参数: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            if (StrUtil.isNotBlank(toolCallInfo))
                log.info(toolCallInfo);

            if (toolCallList.isEmpty() || isTaskComplete(res)) {
                // 如果不需要调用工具，需要单独把助手消息添加到上下文中
                getMessageList().add(assistantMessage);
                log.info("任务已完成，将状态置为 FINISHED");
                setState(AgentState.FINISHED);
                return new ThinkResponse(false, res);
            } else
                // 如果需要调用工具，不需要记入助手消息，在 Act 中才会执行工具，执行工具后才会有结果，这个结果里是包含助手消息的
                return new ThinkResponse(true, res);
        } catch (Exception e) {
            log.error("{} 在本轮的思考中遇到意外困难：{}", getName(), e.getMessage());
            // 即使抛出异常了也要告诉 AI 错误信息
            String res = "在本轮思考中遇到错误：" + e.getMessage();
            getMessageList().add(new AssistantMessage(res));
            return new ThinkResponse(false, res);
        }
    }

    @Override
    public String act(String thinkMsg) {
        if (!this.toolCallChatResponse.hasToolCalls())
            return thinkMsg + "无需调用工具。";

        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), this.getChatOptions());
        ToolExecutionResult toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, this.toolCallChatResponse);

        // 记录信息的上下文，conversationHistory 已经包含了助手信息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());

        // 工具调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(this.getMessageList());
        String res = toolResponseMessage.getResponses().stream()
                .map(response -> {
                    String formatted = formatToolOutput(response.responseData());
                    return String.format("工具 %s 已执行，结果摘要：\n%s", response.name(), formatted);
                })
                .collect(Collectors.joining("\n\n"));
        // 规范化工具输出，合并空行
        res = TextUtil.normalizeMessage(res);
        log.info(res);

        // 判断是否调用了终止工具
        handleSpecialTool(toolResponseMessage);

        String combined = (thinkMsg == null ? "" : thinkMsg) + "\n" + res;
        return TextUtil.normalizeMessage(combined);
    }

    /**
     * 判断任务是否已完成
     * 通过检查LLM的回复内容来判断
     *
     * @param llmResponse LLM的回复文本
     * @return 是否已完成
     */
    private boolean isTaskComplete(String llmResponse) {
        if (StrUtil.isBlank(llmResponse)) {
            return false;
        }

        String lowerResponse = llmResponse.toLowerCase();

        // 检查是否包含任务完成的标志性词语
        return lowerResponse.contains("已完成") ||
                lowerResponse.contains("完成") && (lowerResponse.contains("任务") || lowerResponse.contains("工作")) ||
                lowerResponse.contains("finished") ||
                lowerResponse.contains("complete") && (lowerResponse.contains("task") || lowerResponse.contains("work")) ||
                lowerResponse.contains("任务结束") ||
                lowerResponse.contains("done");
    }

    /**
     * 格式化工具输出，针对 HTML/JSON/长文本做友好预览和截断
     */
    private String formatToolOutput(String data) {
        if (StrUtil.isBlank(data)) return "";
        String trimmed = data.trim();
        try {
            // HTML 内容 -> 提取纯文本预览
            int max_html_len = MAX_HTML_LEN.getValue();
            String lower = trimmed.toLowerCase();
            if (lower.startsWith("<!doctype") || lower.contains("<html") || lower.contains("<body")) {
                String text = Jsoup.parse(trimmed).text();
                return text.length() > max_html_len ? text.substring(0, max_html_len) + "...[已截断]" : text;
            }

            // JSON 内容 -> 美化并截断
            int max_json_len = MAX_JSON_LEN.getValue();
            if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                try {
                    if (trimmed.startsWith("{")) {
                        JSONObject obj = JSONUtil.parseObj(trimmed);
                        String pretty = obj.toStringPretty();
                        return pretty.length() > max_json_len ? pretty.substring(0, max_json_len) + "...[已截断]" : pretty;
                    } else {
                        JSONArray arr = JSONUtil.parseArray(trimmed);
                        String pretty = arr.toStringPretty();
                        return pretty.length() > max_json_len ? pretty.substring(0, max_json_len) + "...[已截断]" : pretty;
                    }
                } catch (Exception ignore) {
                    // 解析失败则继续走默认处理
                }
            }
        } catch (Exception ignored) {
        }

        // 默认：截断长文本
        int max_long_text_len = MAX_LONG_TEXT_LEN.getValue();
        return trimmed.length() > max_long_text_len ? trimmed.substring(0, max_long_text_len) + "...[已截断]" : trimmed;
    }

    private void handleSpecialTool(ToolResponseMessage tMsg) {
        Optional<ToolResponseMessage.ToolResponse> terminateToolCalled = tMsg.getResponses().stream()
                .filter(response -> response.name().equals("doTerminate") || response.name().equals("自行终止"))
                .findFirst();
        if (terminateToolCalled.isPresent()) {
            // 任务结束，更改状态
            log.info("特殊工具 {} 已完成任务！", terminateToolCalled.get().name());
            setState(AgentState.FINISHED);
        }
    }
}