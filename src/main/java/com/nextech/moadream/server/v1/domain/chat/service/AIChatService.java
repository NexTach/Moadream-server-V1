package com.nextech.moadream.server.v1.domain.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    private static final String SYSTEM_PROMPT = """
            당신은 에너지 사용량 관리 전문 AI 상담사입니다.

            주요 역할:
            1. 사용자의 전기, 수도, 가스 사용량을 분석하고 조언합니다.
            2. 지역별 요금 정보를 정확하게 제공합니다.
            3. 에너지 절약 방법을 구체적으로 제안합니다.
            4. 사용자의 사용 패턴을 분석하여 맞춤형 추천을 제공합니다.
            5. 청구서 관련 질문에 답변합니다.

            주의사항:
            - 항상 친절하고 이해하기 쉬운 한국어로 답변하세요.
            - 구체적인 숫자와 계산 근거를 제시하세요.
            - 사용자 데이터를 기반으로 개인화된 조언을 제공하세요.
            - 지역별 요금 차이를 고려하세요.
            - 실용적이고 실천 가능한 조언을 우선하세요.
            """;

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
        try {
            if (openaiApiKey == null || openaiApiKey.isEmpty()) {
                return generateFallbackResponse(userMessage, userContext);
            }

            List<ChatMessage> history = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);
            List<Object> messages = buildMessageHistory(history, userMessage, userContext);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            var requestBody = new java.util.HashMap<String, Object>();
            requestBody.put("model", openaiModel);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1000);

            HttpEntity<Object> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openaiApiUrl, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage());
            return generateFallbackResponse(userMessage, userContext);
        }
    }

    private List<Object> buildMessageHistory(List<ChatMessage> history, String currentMessage, String userContext) {
        List<Object> messages = new ArrayList<>();

        var systemMsg = new java.util.HashMap<String, String>();
        systemMsg.put("role", "system");
        systemMsg.put("content", SYSTEM_PROMPT + "\n\n" + userContext);
        messages.add(systemMsg);

        history.stream().limit(10).forEach(msg -> {
            var historyMsg = new java.util.HashMap<String, String>();
            historyMsg.put("role", msg.getRole().name().toLowerCase());
            historyMsg.put("content", msg.getContent());
            messages.add(historyMsg);
        });

        var userMsg = new java.util.HashMap<String, String>();
        userMsg.put("role", "user");
        userMsg.put("content", currentMessage);
        messages.add(userMsg);

        return messages;
    }

    private String generateFallbackResponse(String userMessage, String userContext) {
        String lowerMessage = userMessage.toLowerCase();

        if (lowerMessage.contains("요금") && (lowerMessage.contains("지역") || lowerMessage.contains("동네"))) {
            return regionalRateService.getAllRegionalRates();
        }

        if (lowerMessage.contains("절약") || lowerMessage.contains("줄이")) {
            return generateEnergySavingTips();
        }

        if (lowerMessage.contains("사용량") || lowerMessage.contains("얼마")) {
            return "죄송합니다만, 현재 AI 서비스 연동이 원활하지 않습니다.\n\n" + userContext + "\n위 정보를 확인하시고, 구체적인 질문이 있으시면 다시 문의해 주세요.";
        }

        return "안녕하세요! 에너지 사용량 관리를 도와드리는 AI 상담사입니다.\n\n" + "다음과 같은 질문에 답변해드릴 수 있습니다:\n"
                + "- 우리 동네 전기/수도/가스 요금은 얼마인가요?\n" + "- 이번 달 사용량은 어떻게 되나요?\n" + "- 에너지 절약 방법을 알려주세요\n"
                + "- 전월 대비 사용량 변화는?\n\n" + "무엇을 도와드릴까요?";
    }

    private String generateEnergySavingTips() {
        return """
                💡 에너지 절약 꿀팁

                【전기 절약】
                - LED 전구 사용 (백열등 대비 80% 절감)
                - 대기전력 차단 (연간 5-10만원 절감)
                - 냉장고 적정 온도 유지 (냉장 3-4도, 냉동 -18도)
                - 에어컨 필터 정기 청소 (효율 15% 향상)

                【수도 절약】
                - 절수 샤워기 사용 (30-50% 절감)
                - 설거지 시 물받아 사용
                - 변기 물탱크에 벽돌 넣기

                【가스 절약】
                - 압력솥 활용 (일반 냄비 대비 60% 절감)
                - 뚜껑 사용하여 조리
                - 보일러 적정 온도 유지 (외출 시 18도, 수면 시 15도)
                """;
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
