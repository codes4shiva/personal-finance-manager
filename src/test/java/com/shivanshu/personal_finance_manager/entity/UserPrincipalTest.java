package com.shivanshu.personal_finance_manager.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    @Test
    @DisplayName("UserPrincipal getters and flags")
    void testUserPrincipal() {
        UserEntity user = new UserEntity("principal@test.com", "pass123", "Principal Name", "1234567890");
        user.setId(1L);

        UserPrincipal principal = new UserPrincipal(user);
        assertEquals("principal@test.com", principal.getUsername());
        assertEquals("pass123", principal.getPassword());
        assertEquals(user, principal.getUser());
        assertTrue(principal.getAuthorities().isEmpty());
        assertTrue(principal.isAccountNonExpired());
        assertTrue(principal.isAccountNonLocked());
        assertTrue(principal.isCredentialsNonExpired());
        assertTrue(principal.isEnabled());
    }
}
