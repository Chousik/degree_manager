package ru.chousik.is.gatewayserver.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetAwayConfigTest {

    @Test
    void springSecurityFilterChain_isCreated() {
        GetAwayConfig config = new GetAwayConfig();

        assertThat(config.springSecurityFilterChain()).isNotNull();
    }
}
