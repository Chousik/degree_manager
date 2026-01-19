package ru.chousik.web.authservice.controller.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.chousik.web.authservice.exception.MissingTwoFactorCodeException;
import ru.chousik.web.authservice.exception.UsernameExistsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionControllerTest {

    @Test
    void handleValidationError_usesResponseStatusAnnotation() {
        GlobalExceptionController controller = new GlobalExceptionController();

        var response = controller.handleValidationError(new UsernameExistsException("user"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Error:");
    }

    @Test
    void handleValidationError_defaultsToBadRequest() {
        GlobalExceptionController controller = new GlobalExceptionController();

        var response = controller.handleValidationError(new MissingTwoFactorCodeException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("OTP_REQUIRED");
    }
}
