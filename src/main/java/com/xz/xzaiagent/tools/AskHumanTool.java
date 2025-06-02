package com.xz.xzaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Scanner;

public class AskHumanTool {

    private final Scanner scanner = new Scanner(System.in);

    @Tool(description = "Use this tool to ask human for help")
    public String askHuman(@ToolParam(description = "The question you want to ask human") String inquire) {
        System.out.println("Bot: " + inquire + "\n\nYou:");
        return scanner.nextLine().trim();
    }
}
