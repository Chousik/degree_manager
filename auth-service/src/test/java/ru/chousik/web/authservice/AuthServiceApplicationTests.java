package ru.chousik.web.authservice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceApplicationTests {

    @Test
    void applicationClassIsPresent() {
        assertThat(AuthServiceApplication.class).isNotNull();
    }
}
