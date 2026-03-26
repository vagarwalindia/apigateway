package com.knowledgeflow.apigateway.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.knowledgeflow.apigateway.dto.JwtClaims;
import com.knowledgeflow.apigateway.dto.JwtUserDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtil {
	private final SecretKey key;
	
	public JWTUtil(@Value("${jwt.secret}") String SECRET_KEY) {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

	public String generateToken(JwtUserDto user, long expiryMinutes) {
		Date now = new Date();
	    Date expiry = new Date(now.getTime() + expiryMinutes * 60 * 1000);
		return Jwts.builder()
				.subject(user.getUserId())
				.claim("username", user.getUsername())
	            .claim("firstName", user.getFirstName())
	            .claim("roles", user.getRoles())
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}
	
	public JwtUserDto validateAndExtractUsername(String token) {
	    try {
	        Claims claims = Jwts.parser()
	                .verifyWith(key)
	                .build()
	                .parseSignedClaims(token)
	                .getPayload();

	        return JwtUserDto.builder()
	                .userId(claims.getSubject())
	                .username(claims.get(JwtClaims.USERNAME, String.class))
	                .firstName(claims.get(JwtClaims.FIRST_NAME, String.class))
	                .roles(null)
	                .build();

	    } catch (JwtException e) {
	        return null;
	    }
	}
	
	public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
