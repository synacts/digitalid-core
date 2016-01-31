package net.digitalid.core.identity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.annotations.meta.TargetTypes;

import net.digitalid.core.identity.Type;

/**
 * This annotation indicates that a {@link Type type} is {@link Type#isLoaded() loaded}.
 * 
 * @see NonLoaded
 */
@Documented
@TargetTypes(Type.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Loaded {}
