package com.xz.xzaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具类，用于让自主规划智能体能够合理地中断
 */
public class TerminateTool {

    private final String TERMINATE_DESCRIPTION = "终止当前交互：当任务完成或无法继续执行任务时，调用此工具以结束工作。";

    @Tool(description = TERMINATE_DESCRIPTION)
    public String doTerminate() {
        return "交互已完成";
    }
}

// EN
// Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
// When you have finished all the tasks, call this tool to end the work.
// The interaction has been completed