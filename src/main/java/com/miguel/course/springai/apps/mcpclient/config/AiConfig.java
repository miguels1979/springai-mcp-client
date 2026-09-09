package com.miguel.course.springai.apps.mcpclient.config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, SyncMcpToolCallbackProvider provider){
        return builder.defaultTools(provider).build();
    }

}
