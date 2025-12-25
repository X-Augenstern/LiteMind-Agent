package com.xz.xzaiagent.agent;

import com.xz.xzaiagent.advisor.MyLoggerAdvisor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.xz.xzaiagent.agent.prompt.LiteMind.NEXT_STEP_PROMPT_ZH;
import static com.xz.xzaiagent.agent.prompt.LiteMind.SYSTEM_PROMPT_ZH;

/**
 * LiteMind AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Setter
@Component
@Slf4j
public class LiteMind extends ToolCallAgent {

    // Optional externally provided sessionChatId; if set, agent should use it and register
    private String requestedChatId;

    public LiteMind(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("LiteMind");
        this.setSystemPrompt(SYSTEM_PROMPT_ZH);
        this.setNextStepPrompt(NEXT_STEP_PROMPT_ZH);
        this.setMaxSteps(10);

        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }

    @Override
    public SseEmitter runByStream(String userPrompt) {
        // Call base implementation to get emitter
        SseEmitter emitter = super.runByStream(userPrompt);
        // If a requestedChatId was provided and registry available, register this agent/emitter
        if (this.requestedChatId != null && this.getActiveAgentRegistry() != null) {
            try {
                this.getActiveAgentRegistry().register(this.requestedChatId, this, emitter, null);
            } catch (Exception e) {
                // ignore registration errors but log
                log.warn("针对 chatId：{}，智能体向 ActiveAgentRegistry 注册失败", this.requestedChatId, e);
            }
        }
        return emitter;
    }
}
