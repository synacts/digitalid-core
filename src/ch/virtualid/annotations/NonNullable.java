package ch.virtualid.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a reference is non-nullable.
 * (This annotation is only valuable once the source code can
 * be transitioned to Java 1.8 with its type annotations).
 * 
 * @see Nullable
 * @see ElementsNonNullable
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Deprecated
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface NonNullable {}
