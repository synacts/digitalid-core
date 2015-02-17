package ch.virtualid.annotations;
  
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * This annotation indicates that the elements of a {@link Collection collection} are {@link Nullable nullable}.
 * (This annotation is only necessary until the source code can be transitioned to Java 1.8 with its type annotations).
 * 
 * @see ElementsNonNullable
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface ElementsNullable {}
