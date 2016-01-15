package net.digitalid.service.core.identity.resolution.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.meta.TargetType;

import net.digitalid.service.core.identifier.Identifier;

/**
 * This annotation indicates that a method should only be invoked on a non-{@link Identifier#isMapped() mapped} {@link Identifier identifier}.
 * 
 * @see NonMapped
 */
@Documented
@Target(ElementType.METHOD)
@TargetType(Identifier.class)
@Retention(RetentionPolicy.CLASS)
public @interface NonMappedRecipient {}
