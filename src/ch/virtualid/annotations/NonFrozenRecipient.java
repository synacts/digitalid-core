package ch.virtualid.annotations;

import ch.virtualid.interfaces.Freezable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a method should only be invoked on {@link NonFrozen non-frozen} objects.
 * 
 * @see Freezable
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface NonFrozenRecipient {}
