package ru.chousik.is.eurekaserver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EurekaServerApplicationTests {

    @Test
    void applicationClassIsPresent() {
        assertThat(EurekaServerApplication.class).isNotNull();
    }
}
