package com.xz.xzaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import com.xz.xzaiagent.agent.ActiveAgentRegistry;
import jakarta.annotation.Resource;

/**
 * 终止工具类，用于让自主规划智能体能够合理地中断
 */
@Slf4j
public class TerminateTool {

    @Resource
    private ActiveAgentRegistry activeAgentRegistry;

    @Tool(description = "终止当前交互：传入 chatId 以结束对应的智能体会话")
    public String doTerminate(@ToolParam(description = "要终止的 chatId（32 hex）") String chatId) {
        if (chatId == null || chatId.isEmpty()) {
            return "终止失败，对话Id为空！";
        }
        boolean ok;
        try {
            ok = activeAgentRegistry.terminate(chatId, true);
        } catch (Exception e) {
            log.error("终止工具出错：{}", e.getMessage());
            return "终止失败！";
        }
        return ok ? "终止完成！" : "终止失败：找不到对话Id！";
    }
}

// EN
// Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
// When you have finished all the tasks, call this tool to end the work.
// The interaction has been completed