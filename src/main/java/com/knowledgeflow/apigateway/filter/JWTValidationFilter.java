package com.knowledgeflow.apigateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.knowledgeflow.apigateway.dto.JwtUserDto;
import com.knowledgeflow.apigateway.util.JWTUtil;

import reactor.core.publisher.Mono;

@Component
public class JWTValidationFilter implements GlobalFilter, Ordered{
	private final JWTUtil jwtUtil;
	private final String INTERNAL_SECRET_KEY;
	private final RouteValidator routeValidator;
	public JWTValidationFilter(@Value("${internal.secret-key}") String INTERNAL_SECRET_KEY, JWTUtil jwtUtil, RouteValidator routeValidator) {
        this.INTERNAL_SECRET_KEY = INTERNAL_SECRET_KEY;
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getPath().value();
		if (routeValidator.isPublic(path)) {
		    return chain.filter(exchange);
		}
		String authHeader =
                exchange.getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        	exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        	try {
                JwtUserDto user = jwtUtil.validateAndExtractUsername(token);

                ServerHttpRequest request =
                        exchange.getRequest()
                                .mutate()
                                .header("X-User-Id", user.getUserId())
                                .header("X-Username", user.getUsername())
                                .header("X-Firstname", user.getFirstName())
                                .header("X-Gateway-Auth", INTERNAL_SECRET_KEY)
                                .build();

                return chain.filter(exchange.mutate().request(request).build());

            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
	}

	@Override
	public int getOrder() {
		return -1;
	}

	

}
