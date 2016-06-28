package net.digitalid.core.entity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.InternalPerson;

/**
 * This annotation indicates that the {@link Identity identity} of an {@link Entity entity} denotes an {@link InternalPerson internal person}.
 * 
 * @see OfType
 */
@Documented
//@TargetTypes(Entity.class)
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OfInternalPerson {
    
    // TODO: Write a value validator if this annotation is still used at all.
    
}
