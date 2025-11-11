package com.nextech.moadream.server.v1.domain.chat.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PromptTemplateService {

    private static final Map<String, String> PROMPTS = new HashMap<>();

    static {
        // 시스템 프롬프트
        PROMPTS.put("SYSTEM_BASE", """
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
                """);

        // 에너지 절약 팁
        PROMPTS.put("ENERGY_SAVING_TIPS", """
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
                """);

        // 웰컴 메시지
        PROMPTS.put("WELCOME_MESSAGE", """
                안녕하세요! 에너지 사용량 관리를 도와드리는 AI 상담사입니다.

                다음과 같은 질문에 답변해드릴 수 있습니다:
                - 우리 동네 전기/수도/가스 요금은 얼마인가요?
                - 이번 달 사용량은 어떻게 되나요?
                - 에너지 절약 방법을 알려주세요
                - 전월 대비 사용량 변화는?

                무엇을 도와드릴까요?
                """);

        // API 연동 실패 메시지
        PROMPTS.put("API_ERROR_MESSAGE", """
                죄송합니다만, 현재 AI 서비스 연동이 원활하지 않습니다.

                {USER_CONTEXT}

                위 정보를 확인하시고, 구체적인 질문이 있으시면 다시 문의해 주세요.
                """);

        // 사용량 분석 프롬프트
        PROMPTS.put("USAGE_ANALYSIS", """
                사용자의 에너지 사용량 데이터를 분석하고 다음을 제공하세요:
                1. 현재 사용량에 대한 평가
                2. 전월 대비 증감 분석
                3. 예산 대비 사용률 분석
                4. 개선이 필요한 부분
                5. 구체적인 절약 방안 (금액 기준 제시)
                """);

        // 요금 비교 프롬프트
        PROMPTS.put("RATE_COMPARISON", """
                다음 정보를 기반으로 지역별 요금을 비교 분석하세요:
                1. 사용자 지역의 요금
                2. 전국 평균과의 차이
                3. 인근 지역과의 비교
                4. 요금 절감 기회
                """);

        // 추천 생성 프롬프트
        PROMPTS.put("RECOMMENDATION_GENERATION", """
                사용자의 사용 패턴을 분석하여 맞춤형 추천을 생성하세요:
                1. 사용자의 현재 상황 요약
                2. 우선순위가 높은 개선 사항 (상위 3개)
                3. 각 개선 사항의 예상 절감액
                4. 실천 난이도 (쉬움/보통/어려움)
                5. 구체적인 실행 방법
                """);

        // 청구서 분석 프롬프트
        PROMPTS.put("BILL_ANALYSIS", """
                청구서 정보를 분석하여 다음을 제공하세요:
                1. 청구 금액 상세 분석
                2. 전월 대비 변화 원인
                3. 이상 항목 여부
                4. 납부 기한 및 주의사항
                5. 다음 달 예상 청구액
                """);

        // AI 추천 시스템 프롬프트
        PROMPTS.put("AI_RECOMMENDATION_SYSTEM", """
                당신은 한국전력공사(KEPCO)와 협력하는 에너지 절약 전문 컨설턴트입니다.

                당신의 역할:
                1. 사용자의 전기, 수도, 가스 사용 패턴을 분석합니다
                2. 실제 절감 효과가 있는 전문적인 절약 방안을 제시합니다
                3. 한국의 전기 요금제 구조(주택용, 경부하/중부하/최대부하 시간대)를 고려합니다
                4. 구체적인 금액을 원(₩) 단위로 추정합니다

                중요한 배경 지식:
                - 한국 전기요금: 경부하(23:00-09:00), 중부하(09:00-10:00, 12:00-13:00, 17:00-23:00), 최대부하(10:00-12:00, 13:00-17:00)
                - 주택용 누진제: 200kWh까지 기본, 201-400kWh 중간, 401kWh 이상 고가
                - 대기전력: 가구당 월평균 10-15% 전력 소비
                - 에너지 효율 1등급 가전: 5등급 대비 30-40% 절감
                - 절수 기기: 일반 대비 20-30% 물 절약

                응답 형식을 정확히 지켜주세요:
                각 추천은 반드시 다음 4줄로 구성되어야 합니다:
                1. 번호. [카테고리] 제목
                2. 설명: 구체적인 설명 (한 문장, ~해요/~세요 어체)
                3. 예상절감: 숫자만 (단위 없이, 원 단위)
                4. 난이도: 쉬움, 보통, 어려움 중 하나

                카테고리:
                - USAGE_REDUCTION: 사용량 줄이기
                - BEHAVIOR_CHANGE: 습관 개선
                - TIME_SHIFT: 시간대 이동 (전기요금 절감)
                - APPLIANCE_UPGRADE: 기기 교체
                - TARIFF_OPTIMIZATION: 요금제 최적화
                """);

        // AI 추천 사용자 프롬프트 템플릿
        PROMPTS.put("AI_RECOMMENDATION_USER", """
                다음 {UTILITY_NAME} 사용 패턴을 분석하여 3가지 절약 방안을 제시해주세요.

                [사용 데이터]
                - 평균 사용량: {AVERAGE_USAGE} {UNIT}
                - 피크 사용량: {PEAK_USAGE} {UNIT}
                - 오프피크 사용량: {OFF_PEAK_USAGE} {UNIT}
                - 사용 추세: {TREND}

                아래 형식을 정확히 따라 작성해주세요. 다른 내용은 추가하지 마세요:

                1. [USAGE_REDUCTION] 대기전력 차단으로 전기 절약하기
                설명: 사용하지 않는 가전제품은 플러그를 뽑거나 멀티탭을 활용해서 대기전력을 차단해보세요.
                예상절감: 15000
                난이도: 쉬움

                2. [BEHAVIOR_CHANGE] 조명 사용 습관 개선하기
                설명: 낮 시간에는 자연광을 활용하고 불필요한 조명은 꺼서 전기를 절약해보세요.
                예상절감: 12000
                난이도: 쉬움

                3. [TIME_SHIFT] 경부하 시간대 활용하기
                설명: 세탁기나 식기세척기는 밤 11시 이후에 사용해서 전기 요금을 절약해보세요.
                예상절감: 18000
                난이도: 보통

                주의사항:
                - 각 추천은 정확히 4줄로 작성하세요
                - 이모지, 마크다운 기호(**, ##), 추가 설명을 넣지 마세요
                - 예상절감은 숫자만 쓰세요 (원, ₩, 퍼센트 금지)
                - 실제 한국 전기요금 구조를 고려한 현실적인 금액을 제시하세요
                - 사용자의 실제 데이터를 반영하여 맞춤형 추천을 해주세요
                """);
    }

    /**
     * 프롬프트 템플릿 가져오기
     */
    public String getPrompt(String key) {
        String prompt = PROMPTS.get(key);
        if (prompt == null) {
            log.warn("프롬프트 키를 찾을 수 없습니다: {}", key);
            return "";
        }
        return prompt;
    }

    /**
     * 변수를 포함한 프롬프트 생성
     */
    public String buildPrompt(String key, Map<String, String> variables) {
        String template = getPrompt(key);
        if (template.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            result = result.replace(placeholder, entry.getValue());
        }

        return result;
    }

    /**
     * 시스템 프롬프트 + 사용자 컨텍스트 결합
     */
    public String buildSystemPrompt(String userContext) {
        return getPrompt("SYSTEM_BASE") + "\n\n" + userContext;
    }

    /**
     * 에너지 절약 팁 가져오기
     */
    public String getEnergySavingTips() {
        return getPrompt("ENERGY_SAVING_TIPS");
    }

    /**
     * 웰컴 메시지 가져오기
     */
    public String getWelcomeMessage() {
        return getPrompt("WELCOME_MESSAGE");
    }

    /**
     * API 에러 메시지 생성
     */
    public String getApiErrorMessage(String userContext) {
        Map<String, String> variables = new HashMap<>();
        variables.put("USER_CONTEXT", userContext);
        return buildPrompt("API_ERROR_MESSAGE", variables);
    }

    /**
     * 특정 분석 유형에 대한 프롬프트 가져오기
     */
    public String getAnalysisPrompt(String analysisType) {
        return switch (analysisType.toUpperCase()) {
            case "USAGE" -> getPrompt("USAGE_ANALYSIS");
            case "RATE" -> getPrompt("RATE_COMPARISON");
            case "RECOMMENDATION" -> getPrompt("RECOMMENDATION_GENERATION");
            case "BILL" -> getPrompt("BILL_ANALYSIS");
            default -> "";
        };
    }

    /**
     * 모든 프롬프트 키 목록 반환 (관리 목적)
     */
    public Map<String, String> getAllPrompts() {
        return new HashMap<>(PROMPTS);
    }

    /**
     * 동적으로 프롬프트 추가/업데이트 (필요시 사용)
     */
    public void updatePrompt(String key, String prompt) {
        PROMPTS.put(key, prompt);
        log.info("프롬프트 업데이트됨: {}", key);
    }

    /**
     * 프롬프트 존재 여부 확인
     */
    public boolean hasPrompt(String key) {
        return PROMPTS.containsKey(key);
    }
}
