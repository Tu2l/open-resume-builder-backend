package com.tu2l.user.validation;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class EncodedPasswordValidatorTest {

    private final EncodedPasswordValidator validator = new EncodedPasswordValidator();

    private static String b64(String raw) {
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private boolean valid(String raw) {
        return validator.isValid(b64(raw), null);
    }

    @Test
    void acceptsCompliantDecodedPassword() {
        assertThat(valid("ValidP@ss1")).isTrue();
    }

    @Test
    void rejectsTooShort() {
        assertThat(valid("Ab1@")).isFalse(); // 4 chars, below 8
    }

    @Test
    void rejectsMissingUppercase() {
        assertThat(valid("validp@ss1")).isFalse();
    }

    @Test
    void rejectsMissingDigit() {
        assertThat(valid("ValidP@ssword")).isFalse();
    }

    @Test
    void rejectsMissingSpecialChar() {
        assertThat(valid("ValidPass1")).isFalse();
    }

    @Test
    void deferBlankToOtherConstraints() {
        assertThat(validator.isValid("", null)).isTrue();
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    void deferNonBase64ToOtherConstraints() {
        // '!' is outside the Base64 alphabet -> not decodable -> deferred to @Pattern(BASE_64_PATTERN).
        assertThat(validator.isValid("not-base64!!", null)).isTrue();
    }
}
