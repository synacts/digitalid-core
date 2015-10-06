package net.digitalid.service.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.InternalPerson;

/**
 * This annotation indicates that the {@link Identity identity} of an {@link Entity entity} denotes an {@link InternalPerson internal person}.
 * 
 * @see OfType
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Documented
@TargetType(Entity.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface OfInternalPerson {}
