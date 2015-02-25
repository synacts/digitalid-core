package net.digitalid.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.core.identifier.Identifier;

/**
 * This annotation indicates that a method should only be invoked on a {@link Identifier#isNotMapped() not mapped} {@link Identifier identifier}.
 * 
 * @see NonMapped
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Documented
@Target(ElementType.METHOD)
@TargetType(Identifier.class)
@Retention(RetentionPolicy.CLASS)
public @interface NonMappedRecipient {}
