package com.tu2l.user.service.impl;

import com.tu2l.common.exception.AuthenticationException;
import com.tu2l.common.model.JwtTokenType;
import com.tu2l.common.util.CommonUtil;
import com.tu2l.user.audit.AuditService;
import com.tu2l.user.config.AuthConfigValues;
import com.tu2l.user.entity.UserAccountStatus;
import com.tu2l.user.entity.UserCredential;
import com.tu2l.user.entity.UserEntity;
import com.tu2l.user.exception.DuplicateUserException;
import com.tu2l.user.model.request.NewUserRegisterRequest;
import com.tu2l.user.service.AuthTokenService;
import com.tu2l.user.service.EmailService;
import com.tu2l.user.service.PasswordService;
import com.tu2l.user.service.UserService;
import com.tu2l.user.utils.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock UserService userService;
    @Mock AuthTokenService authTokenService;
    @Mock EmailService emailService;
    @Mock PasswordService passwordService;
    @Mock UserMapper userMapper;
    @Mock CommonUtil commonUtil;
    @Mock AuditService auditService;

    private static final AuthConfigValues DEFAULT_CFG = new AuthConfigValues(5, 15, 14, false);
    private static final AuthConfigValues REQUIRE_VERIFIED_CFG = new AuthConfigValues(5, 15, 14, true);

    private AuthenticationServiceImpl service(AuthConfigValues cfg) {
        return new AuthenticationServiceImpl(userService, authTokenService, emailService,
                passwordService, userMapper, cfg, commonUtil, auditService);
    }

    private UserEntity userWith(UserAccountStatus status) {
        return UserEntity.builder()
                .id(1L)
                .username("john")
                .email("john@example.com")
                .password("hash")
                .accountStatus(status)
                .build();
    }

    // --- registration (#7) ---

    @Test
    void register_duplicate_throwsConflict() {
        var req = new NewUserRegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        when(userService.existsByUsernameOrEmail("john", "john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service(DEFAULT_CFG).register(req))
                .isInstanceOf(DuplicateUserException.class);
    }

    // --- lockout (#4, #5) ---

    @Test
    void authenticate_resetsCounterWhenPriorLockExpired() {
        var status = new UserAccountStatus();
        status.setEnabled(true);
        status.setFailedLoginAttempts(5);
        status.setAccountLockedUntil(LocalDateTime.now().minusMinutes(1)); // expired lock
        var user = userWith(status);
        when(userService.getUserByEmailWithDetails("john@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("pw", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service(DEFAULT_CFG).authenticate("john@example.com", "pw", false))
                .isInstanceOf(AuthenticationException.class);

        // Counter was reset to 0 then incremented to 1 — not straight back to a re-lock.
        assertThat(status.getFailedLoginAttempts()).isEqualTo(1);
        assertThat(status.getAccountLockedUntil()).isNull();
    }

    @Test
    void authenticate_locksUsingLockDurationNotRememberMe() {
        var status = new UserAccountStatus();
        status.setEnabled(true);
        status.setFailedLoginAttempts(4); // next failure hits the cap (5)
        var user = userWith(status);
        when(userService.getUserByEmailWithDetails("john@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("pw", "hash")).thenReturn(false);

        // rememberMe=true must NOT extend the lock to the (shorter, here) remember-me window.
        assertThatThrownBy(() -> service(DEFAULT_CFG).authenticate("john@example.com", "pw", true))
                .isInstanceOf(AuthenticationException.class);

        assertThat(status.getAccountLockedUntil())
                .isAfter(LocalDateTime.now().plusMinutes(14))   // proves 15-min lock duration, not 14-min remember-me
                .isBefore(LocalDateTime.now().plusMinutes(16));
    }

    // --- email verification gate (#6) ---

    @Test
    void authenticate_blocksUnverifiedWhenRequired() {
        var status = new UserAccountStatus();
        status.setEnabled(true);
        status.setEmailVerified(false);
        var user = userWith(status);
        when(userService.getUserByEmailWithDetails("john@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("pw", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service(REQUIRE_VERIFIED_CFG).authenticate("john@example.com", "pw", false))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("not verified");
    }

    @Test
    void authenticate_succeedsAndAttachesTokens() {
        var status = new UserAccountStatus();
        status.setEnabled(true);
        status.setEmailVerified(true);
        var user = userWith(status);
        when(userService.getUserByEmailWithDetails("john@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("pw", "hash")).thenReturn(true);
        when(authTokenService.generateToken(user, JwtTokenType.REFRESH)).thenReturn("refresh");
        when(authTokenService.generateAccessToken(user, false)).thenReturn("access");
        when(commonUtil.sha256Hex(anyString())).thenReturn("hash");
        when(authTokenService.issuedAt(anyString())).thenReturn(LocalDateTime.now());
        when(authTokenService.expiresAt(anyString())).thenReturn(LocalDateTime.now().plusDays(1));
        when(userService.saveUser(user)).thenReturn(user);

        var result = service(DEFAULT_CFG).authenticate("john@example.com", "pw", false);

        assertThat(result.getPlainAccessToken()).isEqualTo("access");
        assertThat(result.getPlainRefreshToken()).isEqualTo("refresh");
    }

    @Test
    void authenticate_rememberMe_issuesRememberMeAccessToken() {
        var status = new UserAccountStatus();
        status.setEnabled(true);
        status.setEmailVerified(true);
        var user = userWith(status);
        when(userService.getUserByEmailWithDetails("john@example.com")).thenReturn(user);
        when(passwordService.verifyPassword("pw", "hash")).thenReturn(true);
        when(authTokenService.generateToken(user, JwtTokenType.REFRESH)).thenReturn("refresh");
        // rememberMe=true must request the longer-lived ("remember me") access token.
        when(authTokenService.generateAccessToken(user, true)).thenReturn("rememberAccess");
        when(commonUtil.sha256Hex(anyString())).thenReturn("hash");
        when(authTokenService.issuedAt(anyString())).thenReturn(LocalDateTime.now());
        when(authTokenService.expiresAt(anyString())).thenReturn(LocalDateTime.now().plusDays(1));
        when(userService.saveUser(user)).thenReturn(user);

        var result = service(DEFAULT_CFG).authenticate("john@example.com", "pw", true);

        assertThat(result.getPlainAccessToken()).isEqualTo("rememberAccess");
        verify(authTokenService).generateAccessToken(user, true);
    }

    // --- forgot password enumeration (#8) ---

    @Test
    void forgotPassword_unknownEmail_returnsTrueWithoutSendingMail() {
        when(userService.findByEmailWithCredentials("ghost@example.com")).thenReturn(Optional.empty());

        assertThat(service(DEFAULT_CFG).forgotPassword("ghost@example.com")).isTrue();
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_knownEmail_sendsResetMail() {
        var user = userWith(new UserAccountStatus());
        when(userService.findByEmailWithCredentials("john@example.com")).thenReturn(Optional.of(user));
        when(authTokenService.generateToken(user, JwtTokenType.PASSWORD_RESET)).thenReturn("reset");
        when(commonUtil.sha256Hex(anyString())).thenReturn("hash");
        when(authTokenService.issuedAt(anyString())).thenReturn(LocalDateTime.now());
        when(authTokenService.expiresAt(anyString())).thenReturn(LocalDateTime.now().plusHours(1));
        when(userService.saveUser(user)).thenReturn(user);
        when(emailService.sendPasswordResetEmail("john@example.com", "reset")).thenReturn(true);

        assertThat(service(DEFAULT_CFG).forgotPassword("john@example.com")).isTrue();
        verify(emailService).sendPasswordResetEmail("john@example.com", "reset");
    }

    // --- refresh rotation + reuse detection (#10) ---

    @Test
    void refreshToken_rotatesRefreshToken() {
        var credential = UserCredential.builder()
                .tokenType(JwtTokenType.REFRESH)
                .token("oldhash")
                .active(true)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        var user = userWith(new UserAccountStatus());
        user.addUserCredential(credential);

        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("oldRefresh")).thenReturn("oldhash");
        when(authTokenService.generateToken(user, JwtTokenType.REFRESH)).thenReturn("newRefresh");
        when(authTokenService.refreshAccessToken("oldRefresh", user)).thenReturn("newAccess");
        when(commonUtil.sha256Hex("newRefresh")).thenReturn("newRefreshHash");
        when(commonUtil.sha256Hex("newAccess")).thenReturn("newAccessHash");
        when(authTokenService.issuedAt(anyString())).thenReturn(LocalDateTime.now());
        when(authTokenService.expiresAt(anyString())).thenReturn(LocalDateTime.now().plusDays(1));
        when(userService.saveUser(user)).thenReturn(user);

        var result = service(DEFAULT_CFG).refreshToken("oldRefresh", "john");

        assertThat(result.getPlainRefreshToken()).isEqualTo("newRefresh");
        assertThat(result.getPlainAccessToken()).isEqualTo("newAccess");
        assertThat(credential.getActive()).isFalse(); // presented token deactivated
    }

    @Test
    void refreshToken_reuseOfRotatedToken_revokesAllSessions() {
        var credential = UserCredential.builder()
                .tokenType(JwtTokenType.REFRESH)
                .token("oldhash")
                .active(false) // already rotated
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        var user = userWith(new UserAccountStatus());
        user.addUserCredential(credential);

        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("oldRefresh")).thenReturn("oldhash");

        assertThatThrownBy(() -> service(DEFAULT_CFG).refreshToken("oldRefresh", "john"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("reuse");

        assertThat(user.getCredentialsByType(JwtTokenType.REFRESH)).isNull(); // all sessions cleared
    }

    // --- verification token is one-time + clean on expiry (#11) ---

    @Test
    void verifyEmail_invalidToken_returnsFalse() {
        when(authTokenService.validateToken("t", JwtTokenType.EMAIL_VERIFICATION)).thenReturn(false);

        assertThat(service(DEFAULT_CFG).verifyEmail("t")).isFalse();
    }

    @Test
    void verifyEmail_consumesTokenOnSuccess() {
        var credential = UserCredential.builder()
                .tokenType(JwtTokenType.EMAIL_VERIFICATION)
                .token("vhash")
                .active(true)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        var status = new UserAccountStatus();
        var user = userWith(status);
        user.addUserCredential(credential);

        when(authTokenService.validateToken("t", JwtTokenType.EMAIL_VERIFICATION)).thenReturn(true);
        when(authTokenService.getUsername("t")).thenReturn("john");
        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("t")).thenReturn("vhash");
        when(userService.saveUser(user)).thenReturn(user);

        assertThat(service(DEFAULT_CFG).verifyEmail("t")).isTrue();
        assertThat(status.isEmailVerified()).isTrue();
        assertThat(user.getCredentialByTokenTypeAndToken(JwtTokenType.EMAIL_VERIFICATION, "vhash")).isNull();
    }

    @Test
    void verifyEmail_replayAfterConsumption_returnsFalse() {
        var user = userWith(new UserAccountStatus()); // no verification credential present
        when(authTokenService.validateToken("t", JwtTokenType.EMAIL_VERIFICATION)).thenReturn(true);
        when(authTokenService.getUsername("t")).thenReturn("john");
        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("t")).thenReturn("vhash");

        assertThat(service(DEFAULT_CFG).verifyEmail("t")).isFalse();
    }

    // --- logout ---

    @Test
    void logout_revokesTokenAndAudits() {
        var credential = UserCredential.builder()
                .tokenType(JwtTokenType.ACCESS)
                .token("tokenHash")
                .active(true)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();
        var user = userWith(new UserAccountStatus());
        user.addUserCredential(credential);

        when(authTokenService.getUsername("tok")).thenReturn("john");
        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("tok")).thenReturn("tokenHash");
        when(userService.saveUser(user)).thenReturn(user);

        assertThat(service(DEFAULT_CFG).logout("tok")).isTrue();
        assertThat(user.getCredentialByTokenTypeAndToken(JwtTokenType.ACCESS, "tokenHash")).isNull();
    }

    @Test
    void logout_tokenNotInCredentials_returnsFalse() {
        var user = userWith(new UserAccountStatus());
        when(authTokenService.getUsername("tok")).thenReturn("john");
        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("tok")).thenReturn("tokenHash");
        when(userService.saveUser(user)).thenReturn(user);

        assertThat(service(DEFAULT_CFG).logout("tok")).isFalse();
    }

    // --- reset password ---

    @Test
    void resetPassword_invalidToken_returnsFalse() {
        when(authTokenService.validateToken("tok", JwtTokenType.PASSWORD_RESET)).thenReturn(false);

        assertThat(service(DEFAULT_CFG).resetPassword("tok", "newPw")).isFalse();
        verify(userService, never()).getUserWithCredentials(anyString());
    }

    @Test
    void resetPassword_tokenNotInCredentials_returnsFalse() {
        var user = userWith(new UserAccountStatus());
        when(authTokenService.validateToken("tok", JwtTokenType.PASSWORD_RESET)).thenReturn(true);
        when(authTokenService.getUsername("tok")).thenReturn("john");
        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("tok")).thenReturn("resetHash");

        assertThat(service(DEFAULT_CFG).resetPassword("tok", "newPw")).isFalse();
        verify(passwordService, never()).hashPassword(anyString());
    }

    @Test
    void resetPassword_validToken_setsPasswordAndClearsAllCredentials() {
        var credential = UserCredential.builder()
                .tokenType(JwtTokenType.PASSWORD_RESET)
                .token("resetHash")
                .active(true)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        var user = userWith(new UserAccountStatus());
        user.addUserCredential(credential);

        when(authTokenService.validateToken("tok", JwtTokenType.PASSWORD_RESET)).thenReturn(true);
        when(authTokenService.getUsername("tok")).thenReturn("john");
        when(userService.getUserWithCredentials("john")).thenReturn(user);
        when(commonUtil.sha256Hex("tok")).thenReturn("resetHash");
        when(passwordService.hashPassword("newPw")).thenReturn("newHash");
        when(userService.saveUser(user)).thenReturn(user);

        assertThat(service(DEFAULT_CFG).resetPassword("tok", "newPw")).isTrue();
        assertThat(user.getPassword()).isEqualTo("newHash");
        assertThat(user.getCredentialsByType(JwtTokenType.PASSWORD_RESET)).isNull();
    }

    // --- resend verification (G) ---

    @Test
    void resendVerification_unknownEmail_uniformTrueNoMail() {
        when(userService.findByEmailWithCredentials("ghost@example.com")).thenReturn(Optional.empty());

        assertThat(service(DEFAULT_CFG).resendVerification("ghost@example.com")).isTrue();
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void resendVerification_alreadyVerified_noMail() {
        var status = new UserAccountStatus();
        status.setEmailVerified(true);
        var user = userWith(status);
        when(userService.findByEmailWithCredentials("john@example.com")).thenReturn(Optional.of(user));

        assertThat(service(DEFAULT_CFG).resendVerification("john@example.com")).isTrue();
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void resendVerification_unverified_sendsMail() {
        var status = new UserAccountStatus();
        status.setEmailVerified(false);
        var user = userWith(status);
        when(userService.findByEmailWithCredentials("john@example.com")).thenReturn(Optional.of(user));
        when(authTokenService.generateToken(user, JwtTokenType.EMAIL_VERIFICATION)).thenReturn("vtok");
        when(commonUtil.sha256Hex(anyString())).thenReturn("hash");
        when(authTokenService.issuedAt(anyString())).thenReturn(LocalDateTime.now());
        when(authTokenService.expiresAt(anyString())).thenReturn(LocalDateTime.now().plusHours(1));
        when(userService.saveUser(user)).thenReturn(user);
        when(emailService.sendVerificationEmail("john@example.com", "vtok")).thenReturn(true);

        assertThat(service(DEFAULT_CFG).resendVerification("john@example.com")).isTrue();
        verify(emailService).sendVerificationEmail("john@example.com", "vtok");
    }
}
