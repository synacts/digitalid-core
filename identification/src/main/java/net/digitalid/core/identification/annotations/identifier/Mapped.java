package net.digitalid.core.identification.annotations.identifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identifier.Identifier;

/**
 * This annotation indicates that an {@link Identifier identifier} is {@link Identifier#isMapped() mapped}.
 * 
 * @see NonMapped
 */
@Documented
// TODO: Implement a value validator instead: @TargetTypes(Identifier.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Mapped {}
