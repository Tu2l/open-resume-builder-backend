package com.tu2l.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates the <em>decoded</em> value of a Base64-encoded password against the
 * application's password policy (length + complexity).
 * <p>
 * The request fields carry the password Base64-encoded, so a plain
 * {@code @Pattern}/{@code @Size} only sees the encoding, not the real password.
 * This constraint decodes first and then enforces the policy. It is intentionally
 * lenient about the Base64 encoding itself (returns valid when the value is
 * blank or not decodable) so that the companion {@code @NotBlank} /
 * {@code @Pattern(BASE_64_PATTERN)} constraints own those messages.
 */
@Documented
@Constraint(validatedBy = EncodedPasswordValidator.class)
@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ValidEncodedPassword {

    String message() default "Password must be between 8 and 100 characters and contain at least one uppercase letter, "
            + "one lowercase letter, one number and one special character (@$!%*?&)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
