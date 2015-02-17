package ch.virtualid.annotations;

import ch.virtualid.entity.Entity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.InternalPerson;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that the {@link Identity identity} of an {@link Entity entity} denotes an {@link InternalPerson internal person}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface OfInternalPerson {}
