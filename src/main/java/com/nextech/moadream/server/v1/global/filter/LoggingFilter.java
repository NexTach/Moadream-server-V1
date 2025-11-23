package com.nextech.moadream.server.v1.global.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

    private static final String[] NOT_LOGGING_URL = {"/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**",
            "/h2-console/**", "/favicon.ico", "/actuator/**"};

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isNotLoggingUrl(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isMultipartRequest(request)) {
            handleMultipartRequest(request, response, filterChain);
        } else {
            handleRegularRequest(request, response, filterChain);
        }
    }

    private void handleMultipartRequest(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        UUID logId = UUID.randomUUID();
        long startTime = System.currentTimeMillis();

        try {
            logMultipartRequest(request, logId);
            filterChain.doFilter(request, responseWrapper);
        } catch (Exception e) {
            log.error("LoggingFilter의 FilterChain에서 예외가 발생했습니다.", e);
            throw e;
        } finally {
            logResponse(responseWrapper, startTime, logId);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void handleRegularRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        CachedBodyRequestWrapper cachedRequest;

        try {
            cachedRequest = new CachedBodyRequestWrapper(request);
        } catch (IOException e) {
            log.error("요청 바디 캐싱 중 예외 발생 - 원본 요청으로 진행합니다.", e);
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        UUID logId = UUID.randomUUID();
        long startTime = System.currentTimeMillis();

        try {
            logRequest(cachedRequest, logId, cachedRequest.getCachedBody());
            filterChain.doFilter(cachedRequest, responseWrapper);
        } catch (Exception e) {
            log.error("LoggingFilter의 FilterChain에서 예외가 발생했습니다.", e);
            throw e;
        } finally {
            logResponse(responseWrapper, startTime, logId);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(HttpServletRequest request, UUID logId, byte[] cachedBody) {
        log.info(
                "Log-ID: {}, IP: {}, URI: {}, Http-Method: {}, Params: {}, Content-Type: {}, User-Cookies: {}, User-Agent: {}, Request-Body: {}",
                logId, request.getRemoteAddr(), request.getRequestURI(), request.getMethod(), request.getQueryString(),
                request.getContentType(), formatCookies(request.getCookies()), request.getHeader("User-Agent"),
                getRequestBody(cachedBody));
    }

    private void logMultipartRequest(HttpServletRequest request, UUID logId) {
        String contentLength = request.getHeader("Content-Length");
        if (contentLength == null) {
            contentLength = "[unknown]";
        }

        log.info(
                "Log-ID: {}, IP: {}, URI: {}, Http-Method: {}, Params: {}, Content-Type: {}, Content-Length: {}, User-Cookies: {}, User-Agent: {}, Request-Body: {}",
                logId, request.getRemoteAddr(), request.getRequestURI(), request.getMethod(), request.getQueryString(),
                request.getContentType(), contentLength, formatCookies(request.getCookies()),
                request.getHeader("User-Agent"), "[multipart omitted]");
    }

    private void logResponse(ContentCachingResponseWrapper response, long startTime, UUID logId) {
        long responseTime = System.currentTimeMillis() - startTime;
        String responseBody = getResponseBody(response);

        log.info("Log-ID: {}, Status-Code: {}, Content-Type: {}, Response Time: {}ms, Response-Body: {}", logId,
                response.getStatus(), response.getContentType(), responseTime, responseBody);
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        String contentType = response.getContentType();
        if (contentType == null) {
            contentType = "";
        }
        contentType = contentType.toLowerCase();

        if (isBinaryContent(contentType)) {
            return "[binary content omitted]";
        }

        byte[] contentArray = response.getContentAsByteArray();
        if (contentArray.length == 0) {
            return "[empty]";
        }

        return new String(contentArray, StandardCharsets.UTF_8);
    }

    private boolean isBinaryContent(String contentType) {
        return contentType.startsWith("application/octet-stream")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument")
                || contentType.startsWith("image/") || contentType.startsWith("video/")
                || contentType.startsWith("audio/") || contentType.startsWith("application/pdf")
                || contentType.startsWith("application/zip");
    }

    private String getRequestBody(byte[] cachedBody) {
        if (cachedBody == null || cachedBody.length == 0) {
            return "[empty]";
        }

        String content = new String(cachedBody, StandardCharsets.UTF_8);
        String oneLineContent = content.replaceAll("\\s", "");

        return StringUtils.hasText(oneLineContent) ? oneLineContent : "[empty]";
    }

    private String formatCookies(Cookie[] cookies) {
        if (cookies == null || cookies.length == 0) {
            return "[none]";
        }

        return Arrays.stream(cookies).map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining(", "));
    }

    private boolean isMultipartRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private boolean isNotLoggingUrl(String requestUri) {
        return Arrays.stream(NOT_LOGGING_URL).anyMatch(pattern -> pathMatcher.match(pattern, requestUri));
    }
}
