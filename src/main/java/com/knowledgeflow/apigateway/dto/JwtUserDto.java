package com.knowledgeflow.apigateway.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JwtUserDto {
    private String userId;
    private String username;
    private String firstName;
    private List<String> roles;

}
