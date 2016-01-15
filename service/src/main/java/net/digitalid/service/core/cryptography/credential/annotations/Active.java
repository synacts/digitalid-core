package net.digitalid.service.core.cryptography.credential.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.cryptography.credential.Credential;
import net.digitalid.utility.validation.meta.TargetType;

/**
 * This annotation indicates that a {@link Credential credential} is {@link Credential#isActive() active}.
 */
@Documented
@TargetType(Credential.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Active {}
