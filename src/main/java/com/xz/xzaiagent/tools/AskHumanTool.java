package com.xz.xzaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Scanner;

public class AskHumanTool {

    private final Scanner scanner = new Scanner(System.in);

    @Tool(description = "使用此工具请求人工协助")
    public String askHuman(@ToolParam(description = "要询问人工的问题") String inquire) {
        System.out.println("请求人工协助: " + inquire + "\n\n回复请在此输入：");
        return scanner.nextLine().trim();
    }
}

// EN
// Use this tool to ask human for help
// The question you want to ask human
// Bot:
// You:
