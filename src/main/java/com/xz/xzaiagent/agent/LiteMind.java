package com.xz.xzaiagent.agent;

import com.xz.xzaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * LiteMind AI 超级智能体（拥有自主规划能力，可以直接使用）
 */
@Component
public class LiteMind extends ToolCallAgent{

    public LiteMind(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("LiteMind");
        this.setSystemPrompt(com.xz.xzaiagent.agent.prompt.LiteMind.SYSTEM_PROMPT);
        this.setNextStepPrompt(com.xz.xzaiagent.agent.prompt.LiteMind.NEXT_STEP_PROMPT);
        this.setMaxSteps(20);

        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
