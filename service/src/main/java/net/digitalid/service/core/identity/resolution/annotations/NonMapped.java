package net.digitalid.service.core.identity.resolution.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.utility.validation.meta.TargetType;

/**
 * This annotation indicates that an {@link Identifier identifier} is not {@link Identifier#isMapped() mapped}.
 * 
 * @see Mapped
 */
@Documented
@TargetType(Identifier.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface NonMapped {}
