package com.eaglebank.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add correlation ID and request tracing to all HTTP requests.
 * Enhances logging by providing request tracking across service layers.
 */
@Component
@Order(1) // Execute before other filters
@Slf4j
public class RequestTracingFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";
    private static final String MDC_REQUEST_METHOD_KEY = "httpMethod";
    private static final String MDC_REQUEST_URI_KEY = "requestUri";
    private static final String MDC_USER_AGENT_KEY = "userAgent";
    private static final String MDC_CLIENT_IP_KEY = "clientIp";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Generate or extract correlation ID
            String correlationId = getOrCreateCorrelationId(httpRequest);
            
            // Set up MDC context
            MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
            MDC.put(MDC_REQUEST_METHOD_KEY, httpRequest.getMethod());
            MDC.put(MDC_REQUEST_URI_KEY, httpRequest.getRequestURI());
            MDC.put(MDC_USER_AGENT_KEY, httpRequest.getHeader("User-Agent"));
            MDC.put(MDC_CLIENT_IP_KEY, getClientIpAddress(httpRequest));
            
            // Add correlation ID to response headers
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            
            long startTime = System.currentTimeMillis();
            
            log.info("REQUEST_START: {} {} from {} - User-Agent: {}", 
                    httpRequest.getMethod(), 
                    httpRequest.getRequestURI(),
                    getClientIpAddress(httpRequest),
                    httpRequest.getHeader("User-Agent"));
            
            // Continue with the filter chain
            chain.doFilter(request, response);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("REQUEST_END: {} {} - Status: {} - Duration: {}ms", 
                    httpRequest.getMethod(), 
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration);
                    
        } finally {
            // Clear MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
