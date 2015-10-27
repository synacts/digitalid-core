package net.digitalid.service.core.cryptography.credential.annotations;

import net.digitalid.service.core.cryptography.credential.Credential;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a {@link Credential credential} is {@link Credential#isActive() active}.
 */
@Documented
@TargetType(Credential.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Active {}
