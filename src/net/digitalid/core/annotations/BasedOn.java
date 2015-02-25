package net.digitalid.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This annotation indicates that the {@link Block#getType() type} of a {@link Block block} is
 * {@link SemanticType#isBasedOn(net.digitalid.core.identity.SemanticType) based on} the type of the given identifier.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Documented
@TargetType(Block.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface BasedOn {
    String value();
}
