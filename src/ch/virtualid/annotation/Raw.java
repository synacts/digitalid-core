package ch.virtualid.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;

/**
 * This annotation indicates that a parameter or local variable might not yet be fully initialized.<br>
 * Therefore, you cannot rely on the {@link Nonnull} property of a field or method called on a raw variable.<br>
 * Please make sure that you only pass {@code this} in a constructor to methods that are aware of this situation.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Documented
@Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Raw {}
