package com.tu2l.user.controller;

import com.tu2l.user.service.AuthenticationService;
import com.tu2l.user.utils.AuthResponseBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock AuthenticationService authenticationService;
    @Mock AuthResponseBuilder authResponseBuilder;

    @InjectMocks AuthenticationController controller;

    @Test
    void logout_stripsBearerPrefixBeforeDelegating() {
        // The Authorization header arrives as "Bearer <jwt>"; the service must receive the bare JWT.
        when(authenticationService.logout("the.jwt.token")).thenReturn(true);

        var response = controller.logout("Bearer the.jwt.token");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(authenticationService).logout("the.jwt.token");
    }

    @Test
    void logout_caseInsensitiveBearerScheme() {
        when(authenticationService.logout("the.jwt.token")).thenReturn(true);

        controller.logout("bearer the.jwt.token");

        verify(authenticationService).logout("the.jwt.token");
    }

    @Test
    void logout_bareTokenWithoutPrefix_passedThrough() {
        when(authenticationService.logout("the.jwt.token")).thenReturn(true);

        controller.logout("the.jwt.token");

        verify(authenticationService).logout("the.jwt.token");
    }

    @Test
    void logout_invalidToken_returnsBadRequest() {
        when(authenticationService.logout("the.jwt.token")).thenReturn(false);

        var response = controller.logout("Bearer the.jwt.token");

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
