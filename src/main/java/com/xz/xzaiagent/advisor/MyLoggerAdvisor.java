package com.xz.xzaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志，只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getOrder() {
        return 0;
    }

    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        // AdvisedResponse -> ChatResponse -> Generation -> AssistantMessage -> String
        log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);  // 处理请求
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);  // 执行链调用下一个拦截器，若已经没有下一个了，再调用AI方法

        // 读取上下文
        // String value = (String) advisedResponse.adviseContext().get("re2_input_query");

        this.observeAfter(advisedResponse);  // 处理响应
        return advisedResponse;
    }

    /**
     * 响应式编程
     */
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        // 流式响应，消息是一块一块/一片一片返回出来的，最后要得到完整的响应结果，需要把这些消息聚合起来，所以使用消息聚合器MessageAggregator
        // 这对于日志记录或其它需要观察震哥哥响应而非流中各个独立项的处理非常有用，注意不能在MessageAggregator中修改响应，因为它是个只读操作
        // 不会阻塞整个应用程序，但是会等到把全部消息拼接起来，短暂阻塞
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}
