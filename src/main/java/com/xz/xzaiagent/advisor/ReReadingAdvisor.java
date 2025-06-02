package com.xz.xzaiagent.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 可提高 LLM 的推理能力
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 执行请求前改写 Prompt
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());  // 复制到一个新的 HashMap 中，以便修改时不会影响原始对象
        // re2_input_query 是动态变量，把用户原本请求中的输入advisedRequest.userText()作为动态变量的值
        advisedUserParams.put("re2_input_query", advisedRequest.userText());

        // 更新上下文
        // advisedRequest = advisedRequest.updateContext(context -> {
        //     context.put("re2_input_query", "xxxxxx");
        //     return context;
        // });

        return AdvisedRequest.from(advisedRequest)
                .userText("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """)
                .userParams(advisedUserParams)
                .build();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
