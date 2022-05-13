package com.efimchick.ifmo.web.servlets;

import org.apache.http.HttpStatus;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/calc/*")
public class ExpressionServlet extends HttpServlet {
    public static final char LETTER_A = 'a';
    public static final char LETTER_Z = 'z';
    public static final char NUMBER_0 = '0';
    public static final char NUMBER_9 = '9';
    public static final String EMPTY = "";
    public static final Pattern REGEX = Pattern.compile("\\s+");
    public static final char OPEN_BRACKET = '(';
    public static final char CLOSE_BRACKET = ')';
    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char MULTIPLE = '*';
    public static final char DIVIDE = '/';
    public static final String EXPRESSION = "expression";

    private static final long serialVersionUID = 1;

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        HttpRequestWrapper request = (HttpRequestWrapper) req;
        HttpSession session = request.getSession();
        String param = new StringBuilder(req.getPathInfo()).delete(0, 1).toString();
        Matcher matcher = REGEX.matcher(request.getBody());
        String body = matcher.replaceAll(EMPTY);
        if (session.getAttribute(param) == null) {
            resp.setStatus(HttpStatus.SC_CREATED);
        } else {
            if (session.getAttribute(param) != null) {
                resp.setStatus(HttpStatus.SC_OK);
            }
        }
        session.setAttribute(param, body);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        Integer result = calculate(session.getAttribute(EXPRESSION).toString(), session);
        if (result != null) {
            resp.setStatus(HttpStatus.SC_OK);
            resp.getWriter().print(result);
        } else {
            resp.setStatus(HttpStatus.SC_CONFLICT);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession();
        String parameter = new StringBuilder(req.getPathInfo()).delete(0, 1).toString();
        if (session.getAttribute(parameter) != null) {
            session.removeAttribute(parameter);
            resp.setStatus(HttpStatus.SC_NO_CONTENT);
        }
    }

    private Integer calculate(String expression, HttpSession session) {
        Integer result = null;
        boolean emptyValue = false;
        Stack<Character> operations = new Stack<>();
        Stack<Integer> values = new Stack<>();
        char[] variables = expression.toCharArray();
        for (Character var : variables) {
            if (isNumber(var)) {
                values.push(Integer.parseInt(var.toString()));
            } else if (isLetter(var)) {
                String value = (String) session.getAttribute(String.valueOf(var));
                if (value == null) {
                    emptyValue = true;
                    break;
                }
                char charValue = value.charAt(0);
                while (isLetter(charValue)) {
                    value = session.getAttribute(String.valueOf(charValue)).toString();
                    charValue = value.charAt(0);
                }
                values.push(Integer.valueOf(value));
            } else if (var == OPEN_BRACKET) {
                operations.push(var);
            } else if (var == CLOSE_BRACKET) {
                while (operations.peek() != OPEN_BRACKET) {
                    values.push(calcSimpleOperation(operations.pop(), values.pop(), values.pop()));
                }
                operations.pop();
            } else {
                while (!operations.isEmpty() && checkPriority(var, operations.peek())) {
                    values.push(calcSimpleOperation(operations.pop(), values.pop(), values.pop()));
                }
                operations.push(var);
            }
        }
        if (!emptyValue) {
            while (!operations.isEmpty()) {
                values.push(calcSimpleOperation(operations.pop(), values.pop(), values.pop()));
            }
            result = values.pop();
        }
        return result;
    }

    private boolean isNumber(Character var) {
        return var >= NUMBER_0 && var <= NUMBER_9;
    }

    private boolean isLetter(Character var) {
        return var >= LETTER_A && var <= LETTER_Z;
    }

    private Integer calcSimpleOperation(char operation, Integer secondValue, Integer firstValue) {
        int result;
        switch (operation) {
            case PLUS:
                result = firstValue + secondValue;
                break;
            case MINUS:
                result = firstValue - secondValue;
                break;
            case MULTIPLE:
                result = firstValue * secondValue;
                break;
            case DIVIDE:
                result = firstValue / secondValue;
                break;
            default:
                result = 0;
                break;
        }
        return result;
    }

    private boolean checkPriority(char currentOperation, char previousOperation) {
        boolean priority = previousOperation != OPEN_BRACKET && previousOperation != CLOSE_BRACKET;

        if ((currentOperation == MULTIPLE || currentOperation == DIVIDE)
                && (previousOperation == PLUS || previousOperation == MINUS)) {
            priority = false;
        }
        return priority;
    }
}
