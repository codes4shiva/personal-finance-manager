package com.shivanshu.personal_finance_manager.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shivanshu.personal_finance_manager.dto.request.LoginRequest;
import com.shivanshu.personal_finance_manager.dto.request.RegisterRequest;
import com.shivanshu.personal_finance_manager.entity.UserEntity;
import com.shivanshu.personal_finance_manager.entity.UserPrincipal;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationAndServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AppUserDetailsService appUserDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    private String validEmail;
    private String validAccessToken;

    @BeforeEach
    void setUp() throws Exception {
        validEmail = "jwtuser_" + System.nanoTime() + "@example.com";
        RegisterRequest registerReq = new RegisterRequest(validEmail, "Password123!", "Jwt User", "+1234567890");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest(validEmail, "Password123!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessCookie = result.getResponse().getCookie("accessToken");
        assertNotNull(accessCookie);
        validAccessToken = accessCookie.getValue();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    private String createExpiredToken(String subject, String type) {
        long past = System.currentTimeMillis() - 100000;
        return Jwts.builder()
                .claim("type", type)
                .subject(subject)
                .issuedAt(new Date(past))
                .expiration(new Date(past + 1000))
                .signWith(getSigningKey())
                .compact();
    }

    @Test
    @DisplayName("Valid access token via cookie authenticates successfully")
    void testValidAccessTokenViaCookie() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .cookie(new Cookie("accessToken", validAccessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    @DisplayName("Valid access token via Authorization Bearer header fallback authenticates successfully")
    void testValidAccessTokenViaBearerFallback() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray());
    }

    @Test
    @DisplayName("Expired token via cookie results in 401 without exception leaks")
    void testExpiredTokenReturns401() throws Exception {
        String expiredToken = createExpiredToken(validEmail, "access");
        mockMvc.perform(get("/api/categories")
                        .cookie(new Cookie("accessToken", expiredToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @DisplayName("Tampered signature token returns 401")
    void testTamperedSignatureTokenReturns401() throws Exception {
        String tamperedToken = validAccessToken.substring(0, validAccessToken.length() - 5) + "xyz12";
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @DisplayName("Garbage string token returns 401")
    void testGarbageStringTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .cookie(new Cookie("accessToken", "this-is-not-a-valid-jwt-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer garbage.header.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Token for deleted or non-existent user returns 401")
    void testTokenForNonExistentUserReturns401() throws Exception {
        String ghostToken = jwtService.generateAccessToken("nonexistent_user@example.com");
        mockMvc.perform(get("/api/categories")
                        .cookie(new Cookie("accessToken", ghostToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @DisplayName("Refresh token used as an access token is rejected with 401")
    void testRefreshTokenAsAccessTokenReturns401() throws Exception {
        String refreshToken = jwtService.generateRefreshToken(validEmail);
        mockMvc.perform(get("/api/categories")
                        .cookie(new Cookie("accessToken", refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("JwtService unit verification: generate, extract, validate, wrong user, expiration")
    void testJwtServiceUnitLogic() {
        String email = "unit_jwt@example.com";
        String access = jwtService.generateAccessToken(email);
        String refresh = jwtService.generateRefreshToken(email);

        assertTrue(jwtService.isAccessToken(access));
        assertFalse(jwtService.isRefreshToken(access));

        assertTrue(jwtService.isRefreshToken(refresh));
        assertFalse(jwtService.isAccessToken(refresh));

        assertEquals(email, jwtService.extractUserName(access));
        assertEquals(email, jwtService.extractUserName(refresh));

        UserEntity userEntity = new UserEntity(email, "pass", "Unit Name", "1234567890");
        UserDetails matchingUser = new UserPrincipal(userEntity);

        UserEntity otherUserEntity = new UserEntity("other@example.com", "pass", "Other", "0987654321");
        UserDetails otherUser = new UserPrincipal(otherUserEntity);

        assertTrue(jwtService.validateToken(access, matchingUser));
        assertFalse(jwtService.validateToken(access, otherUser));

        String expiredToken = createExpiredToken(email, "access");
        assertTrue(jwtService.isTokenExpired(expiredToken));
    }
}
