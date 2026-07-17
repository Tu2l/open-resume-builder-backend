package com.tu2l.user.controller.api;

import com.tu2l.common.model.ErrorResponse;
import com.tu2l.common.model.SuccessResponse;
import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.user.model.request.*;
import com.tu2l.user.model.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Registration, login, token management and password flows")
@RequestMapping(value = "/auth", version = "1")
public interface AuthenticationApi {

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns an access/refresh token pair. " +
                          "The `password` field must be a **Base64-encoded** string of the raw password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — invalid input format",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email or username already registered",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    ResponseEntity<@NonNull AuthResponse> register(@Valid @RequestBody final NewUserRegisterRequest request);

    @Operation(
            summary = "Authenticate (login)",
            description = "Authenticates a user with email and Base64-encoded password. " +
                          "Returns an access token (short-lived) and a refresh token. " +
                          "Set `rememberMe: true` to receive a longer-lived access token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials, or account locked/disabled",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/authenticate")
    ResponseEntity<@NonNull AuthResponse> authenticate(@Valid @RequestBody final LoginRequest request);

    @Operation(
            summary = "Refresh access token",
            description = "Exchanges a valid refresh token for a new access/refresh token pair. " +
                          "The supplied refresh token is invalidated immediately after use."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh-token")
    ResponseEntity<@NonNull AuthResponse> refreshToken(@Valid @RequestBody final RefreshTokenRequest request);

    @Operation(
            summary = "Logout (invalidate token)",
            description = "Blacklists the current access token. All subsequent requests using that token will be rejected.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token is missing, malformed, expired, or already invalidated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    ResponseEntity<@NonNull BaseResponse> logout(
            @Parameter(hidden = true)
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token);

    @Operation(
            summary = "Request password reset",
            description = "Sends a one-time password-reset link to the registered email address. " +
                          "To prevent user enumeration the response is always `200 OK`, " +
                          "regardless of whether the email is known."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reset email dispatched (or silently suppressed for unknown addresses)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — invalid email format",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/forgot-password")
    ResponseEntity<@NonNull BaseResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request);

    @Operation(
            summary = "Reset password",
            description = "Sets a new password using the single-use reset token received by email. " +
                          "Unlike login/register, the `newPassword` field is **not** Base64-encoded — " +
                          "it must be the raw password that satisfies the complexity rules."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error, or reset token is invalid/expired",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset-password")
    ResponseEntity<@NonNull BaseResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request);

    @Operation(
            summary = "Verify email address",
            description = "Confirms the user's email using the verification token that was sent during registration. " +
                          "Pass the token as a Bearer value in the `Authorization` header.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Verification token is invalid, already used, or expired",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/verify-email")
    ResponseEntity<@NonNull BaseResponse> verifyEmail(
            @Parameter(required = true)
            @RequestParam("token") String token);

    @Operation(
            summary = "Resend email verification",
            description = "Re-sends the email-verification link. To prevent user enumeration the response is " +
                          "always `200 OK`, regardless of whether the email is known or already verified."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification email dispatched (or silently suppressed)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — invalid email format",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/resend-verification")
    ResponseEntity<@NonNull BaseResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request);
}
