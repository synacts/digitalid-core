package net.digitalid.service.core.entity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.Type;
import net.digitalid.utility.annotations.meta.TargetType;

/**
 * This annotation indicates that the {@link Identity identity} of an {@link Entity entity} denotes a {@link Type type}.
 * 
 * @see OfInternalPerson
 */
@Documented
@TargetType(Entity.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface OfType {}
