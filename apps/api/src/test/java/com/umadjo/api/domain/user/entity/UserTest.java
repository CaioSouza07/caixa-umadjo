package com.umadjo.api.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class UserTest {

    @Test
    @DisplayName("ADMIN tem ROLE_ADMIN e ROLE_COMMON")
    void adminAuthorities() {
        User user = new User();
        user.setRole(UserRole.ADMIN);

        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_COMMON");
    }

    @Test
    @DisplayName("COMMON tem apenas ROLE_COMMON")
    void commonAuthorities() {
        User user = new User();
        user.setRole(UserRole.COMMON);

        assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_COMMON");
    }
}
