package com.franco.backend.testutil;

import com.franco.backend.exception.ApiException;
import com.franco.backend.exception.ErrorCode;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class ApiExceptionAssertions {

    private ApiExceptionAssertions() {
        // utility class
    }

    public static void assertApiException(
            ThrowingRunnable action,
            HttpStatus expectedStatus,
            ErrorCode expectedErrorCode,
            String expectedMessageKey
    ) {
        assertThatThrownBy(() -> action.run())
            .isInstanceOf(ApiException.class)
            .satisfies(ex -> {
                ApiException apiEx = (ApiException) ex;

                assertThat(apiEx.getStatus())
                    .isEqualTo(expectedStatus);

                assertThat(apiEx.getErrorCode())
                    .isEqualTo(expectedErrorCode);

                if (expectedMessageKey != null) {
                    assertThat(apiEx.getMessage())
                        .isEqualTo(expectedMessageKey);
                }
            });
    }

    // interfaz funcional para lambdas
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
