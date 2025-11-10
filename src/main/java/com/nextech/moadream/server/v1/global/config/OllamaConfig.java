package com.nextech.moadream.server.v1.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.ai.ollama.chat.enabled", havingValue = "true", matchIfMissing = false)
public class OllamaConfig {

    @Bean
    public ChatClient.Builder chatClientBuilder(OllamaChatModel ollamaChatModel) {
        log.info("Configuring Spring AI Ollama Chat Client");
        return ChatClient.builder(ollamaChatModel);
    }
}