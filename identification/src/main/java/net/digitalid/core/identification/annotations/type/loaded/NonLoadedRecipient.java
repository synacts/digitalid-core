package net.digitalid.core.identification.annotations.type.loaded;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identity.Type;

/**
 * This annotation indicates that a method should only be invoked on a not {@link Type#isLoaded() loaded} {@link Type type}.
 * 
 * @see LoadedRecipient
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface NonLoadedRecipient {}
