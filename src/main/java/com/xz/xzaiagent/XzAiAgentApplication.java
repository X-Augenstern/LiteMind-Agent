package com.xz.xzaiagent;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class XzAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(XzAiAgentApplication.class, args);
    }

}
