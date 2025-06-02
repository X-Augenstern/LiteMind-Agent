package com.xz.xzaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具类，用于让自主规划智能体能够合理地中断
 */
public class TerminateTool {

    private final String TERMINATE_DESCRIPTION = "Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.\n" +
            "When you have finished all the tasks, call this tool to end the work.";

    @Tool(description = TERMINATE_DESCRIPTION)
    public String doTerminate() {
        return "The interaction has been completed";
    }
}
