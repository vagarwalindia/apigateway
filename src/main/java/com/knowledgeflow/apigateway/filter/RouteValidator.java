package com.knowledgeflow.apigateway.filter;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

    private static final List<String> PUBLIC_ROUTES = List.of(
            "/usermanagement/auth"
    );

    public boolean isPublic(String path) {
        return PUBLIC_ROUTES.stream().anyMatch(path::startsWith);
    }
}
