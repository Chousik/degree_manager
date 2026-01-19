package ru.chousik.is.gatewayserver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayServerApplicationTests {

    @Test
    void applicationClassIsPresent() {
        assertThat(GatewayServerApplication.class).isNotNull();
    }
}
