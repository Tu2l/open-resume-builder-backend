package com.tu2l.user.validation;

import com.tu2l.common.constant.CommonConstants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Decodes a Base64-encoded password and checks the decoded value against the
 * length and complexity policy. See {@link ValidEncodedPassword}.
 */
public class EncodedPasswordValidator implements ConstraintValidator<ValidEncodedPassword, String> {

    private static final Pattern COMPLEXITY = Pattern.compile(CommonConstants.Pattern.PASSWORD_PATTERN);

    @Override
    public boolean isValid(String encoded, ConstraintValidatorContext context) {
        // Blank or non-decodable input is handled by the companion @NotBlank /
        // @Pattern(BASE_64_PATTERN) constraints; don't double-report here.
        if (encoded == null || encoded.isBlank()) {
            return true;
        }
        final String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return true;
        }
        return decoded.length() >= CommonConstants.Validation.PASSWORD_MIN_LENGTH
                && decoded.length() <= CommonConstants.Validation.PASSWORD_MAX_LENGTH
                && COMPLEXITY.matcher(decoded).matches();
    }
}
