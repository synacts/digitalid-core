package net.digitalid.core.resolution.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.meta.TargetType;

import net.digitalid.core.identifier.Identifier;

/**
 * This annotation indicates that an {@link Identifier identifier} is {@link Identifier#isMapped() mapped}.
 * 
 * @see NonMapped
 */
@Documented
@TargetType(Identifier.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Mapped {}
