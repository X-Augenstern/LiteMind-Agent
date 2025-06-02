package com.xz.xzaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct(Reasoning and Acting)模式的代理抽象类
 * 实现了思考——行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)  // 生成的 equals() 和 hashCode() 方法中，会调用父类的 equals() 和 hashCode()：父类的字段也参与比较和哈希计算
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行下一步行动
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动的执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            // 先思考
            boolean shouldAct = think();
            if (!shouldAct)
                return "Thinking complete - no action needed";
            // 再行动
            return act();
        } catch (Exception e) {
            // 记录异常日志
            return "Error executing this step: " + e;
        }
    }
}
