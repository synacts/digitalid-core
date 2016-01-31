package net.digitalid.core.identity.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.annotations.meta.TargetTypes;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.identity.SemanticType;

/**
 * This annotation indicates that the {@link Block#getType() type} of a {@link Block block} is
 * {@link SemanticType#isBasedOn(net.digitalid.service.core.identity.SemanticType) based on} the type of the given identifier.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TargetTypes({Block.class, SemanticType.class})
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface BasedOn {
    String value();
}
