package com.shivanshu.personal_finance_manager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service generating and validating JSON Web Tokens (JWT).
 */
@Service
public class JwtService {

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long ACCESS_TOKEN_TIME = 1000L * 60 * 60;
    private static final long REFRESH_TOKEN_TIME = 1000L * 60 * 60 * 24 * 10;

    public String generateAccessToken(String email) {
        return buildToken(email, ACCESS, ACCESS_TOKEN_TIME);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, REFRESH, REFRESH_TOKEN_TIME);
    }

    private String buildToken(String email, String type, long validityMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim(TYPE_CLAIM, type)
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + validityMs))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isAccessToken(String token) {
        return ACCESS.equals(extractClaim(token, c -> c.get(TYPE_CLAIM, String.class)));
    }

    public boolean isRefreshToken(String token) {
        return REFRESH.equals(extractClaim(token, c -> c.get(TYPE_CLAIM, String.class)));
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            return extractUserName(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return true;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}