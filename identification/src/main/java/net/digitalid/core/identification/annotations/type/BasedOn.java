package net.digitalid.core.identification.annotations.type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identity.SemanticType;

/**
 * This annotation indicates that the {@link Block#getType() type} of a {@link Block block} is
 * {@link SemanticType#isBasedOn(net.digitalid.service.core.identity.SemanticType) based on} the type of the given identifier.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
// TODO: Implement a value validator instead: @TargetTypes({Block.class, SemanticType.class})
@Target(ElementType.TYPE_USE)
public @interface BasedOn {
    String value();
}
