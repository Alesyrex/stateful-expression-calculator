package com.efimchick.ifmo.web.servlets;

import org.apache.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/calc/*")
public class ExpressionFilter implements Filter {

    public static final Integer LEFT_RANGE = -10000;
    public static final Integer RIGHT_RANGE = 10000;
    public static final String EXPRESSION = "expression";

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        boolean noValidRequest = false;
        HttpRequestWrapper request = new HttpRequestWrapper((HttpServletRequest) servletRequest);
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestBody = request.getBody();
        String param = new StringBuilder(request.getPathInfo()).delete(0, 1).toString();
        HttpSession session = request.getSession();
        if (param.equals(EXPRESSION) && isValidExpression(requestBody)) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            noValidRequest = true;
        } else {
            if (!param.equals(EXPRESSION)
                    && session.getAttribute(EXPRESSION).toString().contains(param)) {
                if (session.getAttribute(requestBody) != null) {
                    param = requestBody;
                    requestBody = (String) session.getAttribute(param);
                }
                noValidRequest = isValidRange(response, requestBody);
            }
        }
        if (!noValidRequest) {
            filterChain.doFilter(request, servletResponse);
        }
    }

    private boolean isValidRange(HttpServletResponse response, String body) {
        boolean noValidRequest = false;
        try {
            int value = Integer.parseInt(body);
            if (value > RIGHT_RANGE || value < LEFT_RANGE) {
                response.setStatus(HttpStatus.SC_FORBIDDEN);
                noValidRequest = true;
            }
        } catch (NumberFormatException ex) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            ex.printStackTrace();
        }
        return noValidRequest;
    }

    private boolean isValidExpression(String body) {
        boolean valid = false;
        for (int i = 0; i < body.length() - 1; i++) {
            if (Character.isLetter(body.charAt(i)) && Character.isLetter(body.charAt(i + 1))) {
                valid = true;
            }
        }
        return valid;
    }

    @Override
    public void destroy() {

    }
}


