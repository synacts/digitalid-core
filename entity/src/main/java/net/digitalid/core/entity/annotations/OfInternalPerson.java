package net.digitalid.core.entity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.annotations.meta.TargetTypes;

import net.digitalid.core.entity.Entity;

import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.InternalPerson;

/**
 * This annotation indicates that the {@link Identity identity} of an {@link Entity entity} denotes an {@link InternalPerson internal person}.
 * 
 * @see OfType
 */
@Documented
@TargetTypes(Entity.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface OfInternalPerson {}
