package com.project.me.central_java_service.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class InternalRequestFilter implements Filter {
    private final String CODE;

    public InternalRequestFilter(@Value("${service.code}") String CODE) {
        this.CODE = CODE;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String gatewayCode = request.getHeader("X-Gateway-For");

        if (gatewayCode.equals(CODE)) {
            chain.doFilter(servletRequest, servletResponse);
        } else {
            HttpServletResponse response = (HttpServletResponse) servletRequest;
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            log.warn("CentralService. InternalRequestFilter. Несанкционированный запрос. Запрос отклонен");
            response.getWriter().write("Access Denied");
        }
    }
}
