package com.xz.xzaiagent.app;

import com.xz.xzaiagent.advisor.MyLoggerAdvisor;
import com.xz.xzaiagent.advisor.ReReadingAdvisor;
import com.xz.xzaiagent.chatmemory.InFileChatMemory;
import com.xz.xzaiagent.rag.CustomRetrievalAugmentationAdvisorFactory;
import com.xz.xzaiagent.rag.LoveAppRagCloudAdvisorConfig;
import com.xz.xzaiagent.rag.MyRewriteQueryTransformer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：" +
            "单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；" +
            "已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 初始化 AI 客户端 ChatClient
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        // ChatMemory chatMemory = new InMemoryChatMemory();

        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";  // 获取当前工作目录（Working Directory），返回的是 Java 程序启动时的目录：命令行运行程序时所在的目录/ IDE 运行程序时项目的根目录
        ChatMemory chatMemory = new InFileChatMemory(fileDir);

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                        // new ReReadingAdvisor()
                        // new SimpleLoggerAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                // 为当前拦截器对象指定参数
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)  // 指定对话id，取当前对话id的上下文
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  // 指定获取对话历史消息的条数，如果设置为1则相当于没有记忆，只关注当前对话消息
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 基础对话（支持多轮对话记忆），SSE 流式传输
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                // 为当前拦截器对象指定参数
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)  // 指定对话id，取当前对话id的上下文
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  // 指定获取对话历史消息的条数，如果设置为1则相当于没有记忆，只关注当前对话消息
                .stream()
                .content();  // 请求线程就不用一直阻塞了，stream 在每次服务器端 AI 响应的新的结果都会去激活响应式对象去发出通知，后端就会把新收到的内容推送给前端
        // 异步通知，在得到 content 前可以去处理别的事，得到后再处理 content。一个操作推动另一个操作进行，而不用一直阻塞等待数据到来
        // content.subscribe(c -> log.info("content: {}", c));
    }

    record LoveReport(String title, List<String> suggestions) {
        // Java 提供的一种简洁的方式来定义只用于承载数据的类
        // 本质上是：自动生成构造器、getter、equals、hashCode、toString 方法的 final 类
        // 限制项	            说明
        // 不可变	            所有字段都是 final，一旦创建不能更改
        // 不能继承其他类	        record 隐式继承 java.lang.Record，不能再继承其他类
        // 可以实现接口	        可以用 implements 实现接口
        // 可以加方法	        可以定义自己的方法，但不能有实例字段：因为 record 的设计目标就是纯粹的数据载体、所有状态在构造时完全指定（不可变性）、不允许引入除构造组件以外的可变状态，但可以有静态字段或静态方法
        // 不能有实例初始化块	    不能写 {} 代码块，只能在构造函数中写逻辑
    }

    /**
     * AI 恋爱报告功能（结构化输出）
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                // 为当前拦截器对象指定参数
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)  // 指定对话id，取当前对话id的上下文
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))  // 指定获取对话历史消息的条数，如果设置为1则相当于没有记忆，只关注当前对话消息
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    @Resource  // 会先基于名称注入，可以减少冲突
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    // @Resource
    // private VectorStore PgVectorVectorStore;

    @Resource
    private MyRewriteQueryTransformer myRewriteQueryTransformer;

    /**
     * AI 恋爱知识库问答功能，基于 RAG DB 进行对话
     */
    public String doChatWithRag(String message, String chatId) {

        String rewrittenMessage = myRewriteQueryTransformer.doQueryRewrite(message);

        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用重写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 应用 RAG 知识库问答
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 应用 RAG 检索增强服务（基于云知识库服务）
                // .advisors(loveAppRagCloudAdvisor)
                // 应用 RAG 知识库问答（基于PostgreSQL）
                // .advisors(new QuestionAnswerAdvisor(PgVectorVectorStore))
                // 应用自定义 RAG 检索增强顾问（文档检索器 + 上下文查询增强器）
                // .advisors(
                //         CustomRetrievalAugmentationAdvisorFactory.createCustomRetrievalAugmentationAdvisor(
                //                 loveAppVectorStore, "单身"
                //         )
                // )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 调用工具能力
     */
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 恋爱报告功能（支持调用工具）
     */
    public String doChatWithTool(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 调用 MCP 服务
     * <p>
     * Spring AI MCP 服务启动时会自动读取 mcp-servers.json，从中找到工具自动注册到 ToolCallbackProvider 上
     */
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * AI 恋爱报告功能（支持 MCP）
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(toolCallbackProvider)  // MCP 的底层还是工具调用
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
