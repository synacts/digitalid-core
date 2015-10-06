package net.digitalid.service.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.identity.Type;

/**
 * This annotation indicates that a method should only be invoked on a {@link Type#isNotLoaded() not loaded} {@link Type type}.
 * 
 * @see LoadedRecipient
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface NonLoadedRecipient {}
