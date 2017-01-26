package net.digitalid.core.identification.annotations.identifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identifier.Identifier;

/**
 * This annotation indicates that an {@link Identifier identifier} is not mapped.
 * 
 * TODO: Identifier#isMapped() no longer exists.
 * 
 * @see Mapped
 */
@Documented
// TODO: Implement a value validator instead: @TargetTypes(Identifier.class)
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
public @interface NonMapped {}
