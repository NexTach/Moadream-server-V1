package com.nextech.moadream.server.v1.domain.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextech.moadream.server.v1.domain.chat.dto.ChatResponse;
import com.nextech.moadream.server.v1.domain.chat.entity.ChatMessage;
import com.nextech.moadream.server.v1.domain.chat.entity.ChatSession;
import com.nextech.moadream.server.v1.domain.chat.enums.MessageRole;
import com.nextech.moadream.server.v1.domain.chat.repository.ChatMessageRepository;
import com.nextech.moadream.server.v1.domain.chat.repository.ChatSessionRepository;
import com.nextech.moadream.server.v1.domain.user.entity.User;
import com.nextech.moadream.server.v1.domain.user.repository.UserRepository;
import com.nextech.moadream.server.v1.global.exception.BusinessException;
import com.nextech.moadream.server.v1.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UserContextService userContextService;
    private final RegionalRateService regionalRateService;
    private final PromptTemplateService promptTemplateService;
    private final ChatClient.Builder chatClientBuilder;

    @Value("${spring.ai.openai.chat.enabled:false}")
    private boolean aiEnabled;

    @Transactional
    public ChatResponse sendMessage(Long userId, Long sessionId, String userMessage) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        ChatSession session;
        if (sessionId == null) {
            session = createNewSession(user, userMessage);
        } else {
            session = chatSessionRepository.findBySessionIdAndUser(sessionId, user)
                    .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
        }

        ChatMessage userMsg = ChatMessage.builder().chatSession(session).role(MessageRole.USER).content(userMessage)
                .build();
        chatMessageRepository.save(userMsg);

        String userContext = userContextService.buildUserContext(user);
        String aiResponse = callOpenAI(session, userMessage, userContext);

        ChatMessage aiMsg = ChatMessage.builder().chatSession(session).role(MessageRole.ASSISTANT).content(aiResponse)
                .build();
        chatMessageRepository.save(aiMsg);

        return ChatResponse.from(aiMsg);
    }

    @Transactional
    public ChatSession createNewSession(User user, String initialMessage) {
        String title = generateSessionTitle(initialMessage);
        ChatSession session = ChatSession.builder().user(user).sessionTitle(title).isActive(true).build();
        return chatSessionRepository.save(session);
    }

    private String generateSessionTitle(String message) {
        if (message.length() > 30) {
            return message.substring(0, 30) + "...";
        }
        return message;
    }

    private String callOpenAI(ChatSession session, String userMessage, String userContext) {
        if (!aiEnabled) {
            log.info("AI chat disabled, returning fallback response");
            return generateFallbackResponse(userMessage, userContext);
        }

        try {
            List<ChatMessage> history = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);
            List<Message> messages = buildMessageHistory(history, userMessage, userContext);

            ChatClient chatClient = chatClientBuilder.build();
            Prompt prompt = new Prompt(messages);

            String response = chatClient.prompt(prompt).call().content();

            log.info("AI chat response for session {}: {}", session.getSessionId(), response);
            return response;

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
            return generateFallbackResponse(userMessage, userContext);
        }
    }

    private List<Message> buildMessageHistory(List<ChatMessage> history, String currentMessage, String userContext) {
        List<Message> messages = new ArrayList<>();

        // System message with user context
        messages.add(new SystemMessage(promptTemplateService.buildSystemPrompt(userContext)));

        // Add conversation history (limit to last 10 messages)
        history.stream().limit(10).forEach(msg -> {
            if (msg.getRole() == MessageRole.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else if (msg.getRole() == MessageRole.ASSISTANT) {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        });

        // Add current user message
        messages.add(new UserMessage(currentMessage));

        return messages;
    }

    private String generateFallbackResponse(String userMessage, String userContext) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("요금") && (lowerMessage.contains("지역") || lowerMessage.contains("동네"))) {
            return regionalRateService.getAllRegionalRates();
        }

        if (lowerMessage.contains("절약") || lowerMessage.contains("줄이")) {
            return promptTemplateService.getEnergySavingTips();
        }

        if (lowerMessage.contains("사용량") || lowerMessage.contains("얼마")) {
            return promptTemplateService.getApiErrorMessage(userContext);
        }

        return promptTemplateService.getWelcomeMessage();
    }

    public List<ChatResponse> getSessionMessages(Long userId, Long sessionId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ChatSession session = chatSessionRepository.findBySessionIdAndUser(sessionId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        return chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session).stream().map(ChatResponse::from)
                .collect(Collectors.toList());
    }

    public List<ChatSession> getUserSessions(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return chatSessionRepository.findByUserAndIsActiveOrderByUpdatedAtDesc(user, true);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ChatSession session = chatSessionRepository.findBySessionIdAndUser(sessionId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));
        session.deactivate();
    }
}
