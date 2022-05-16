package com.efimchick.ifmo.web.servlets;

import org.apache.http.HttpStatus;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/calc/*")
public class ExpressionServlet extends HttpServlet {
    public static final String EMPTY = "";
    public static final Pattern REGEX = Pattern.compile("\\s+");
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
        Deque<Character> operations = new ArrayDeque<>();
        Deque<Integer> values = new ArrayDeque<>();
        for (Character var : expression.toCharArray()) {
            if (isNumber(var)) {
                values.push(Integer.parseInt(var.toString()));
            } else if (isLetter(var)) {
                String value = extractValueBeforeCalculation(session, var);
                if (value == null) {
                    emptyValue = true;
                    break;
                }
                values.push(Integer.valueOf(value));
            } else {
                preCalculationResult(operations, values, var);
            }
        }
        if (!emptyValue) {
            result = resultCalculation(operations, values);
        }
        return result;
    }

    private Integer resultCalculation(Deque<Character> operations, Deque<Integer> values) {
        Integer result;
        while (!operations.isEmpty()) {
            values.push(calcSimpleOperation(operations.pop(), values.pop(), values.pop()));
        }
        result = values.pop();
        return result;
    }

    private String extractValueBeforeCalculation(HttpSession session, char var) {
        String value = (String) session.getAttribute(String.valueOf(var));
        if (value != null) {
            while (isLetter(value.charAt(0))) {
                value = session.getAttribute(String.valueOf(value.charAt(0))).toString();
            }
        }
        return value;
    }

    private void preCalculationResult(Deque<Character> operations, Deque<Integer> values, Character var) {
        if (var == Symbol.OPEN_BRACKET.getSymbol()) {
            operations.push(var);
        } else if (var == Symbol.CLOSE_BRACKET.getSymbol()) {
            while (operations.peek() != Symbol.OPEN_BRACKET.getSymbol()) {
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

    private boolean isNumber(Character var) {
        return var >= Symbol.NUMBER_0.getSymbol() && var <= Symbol.NUMBER_9.getSymbol();
    }

    private boolean isLetter(Character var) {
        return var >= Symbol.LETTER_A.getSymbol() && var <= Symbol.LETTER_Z.getSymbol();
    }

    private Integer calcSimpleOperation(char operation, Integer secondValue, Integer firstValue) {
        int result;
        if (operation == Symbol.PLUS.getSymbol()) {
            result = firstValue + secondValue;
        } else if (operation == Symbol.MINUS.getSymbol()) {
            result = firstValue - secondValue;
        } else if (operation == Symbol.MULTIPLE.getSymbol()) {
            result = firstValue * secondValue;
        } else {
            result = firstValue / secondValue;
        }
        return result;
    }

    private boolean checkPriority(char currentOperation, char previousOperation) {
        boolean priority = previousOperation != Symbol.OPEN_BRACKET.getSymbol()
                && previousOperation != Symbol.CLOSE_BRACKET.getSymbol();

        if ((currentOperation == Symbol.MULTIPLE.getSymbol() || currentOperation == Symbol.DIVIDE.getSymbol())
                && (previousOperation == Symbol.PLUS.getSymbol() || previousOperation == Symbol.MINUS.getSymbol())) {
            priority = false;
        }
        return priority;
    }
}
