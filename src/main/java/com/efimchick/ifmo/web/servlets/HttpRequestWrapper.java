package com.efimchick.ifmo.web.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Scanner;

public class HttpRequestWrapper extends HttpServletRequestWrapper {
    private final String body;

    public HttpRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        Scanner scanner = new Scanner(request.getInputStream());
        StringBuilder temp = new StringBuilder();
        while (scanner.hasNext()) {
            temp.append(scanner.next());
        }
        body = temp.toString();
    }

    public String getBody() {
        return body;
    }
}
