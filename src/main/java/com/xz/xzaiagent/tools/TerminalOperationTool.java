package com.xz.xzaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 终端操作工具类
 */
public class TerminalOperationTool {

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
            Process process = pb.start();
            // 在 Java 中执行一个外部命令（如 shell 命令、Windows 命令、Python 脚本等）得到一个运行中的外部进程对象
            // Process process = Runtime.getRuntime().exec(command);
            // 外部程序有输出内容（比如执行了 echo hello，它会输出 hello）。
            // 这个输出内容是“Java 程序来读取”的，对 Java 程序来说是“读入”，即：输入流（InputStream）。
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {  // 获取进程输入流从中获取到进程输出结果
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();  // 等待命令执行完成
            if (exitCode != 0)
                sb.append("Command execution failed with exit code: ").append(exitCode);
        } catch (IOException | InterruptedException e) {
            sb.append("Error executing command: ").append(e.getMessage());
        }
        return sb.toString();
    }
}
