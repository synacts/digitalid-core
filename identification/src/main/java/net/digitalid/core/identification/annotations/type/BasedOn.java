package net.digitalid.core.identification.annotations.type;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* TODO: Remove this annotation or fix the following Javadoc.
 * This annotation indicates that the {@link Block#getType() type} of a {@link Block block} is
 * {@link SemanticType#isBasedOn(net.digitalid.service.core.identity.SemanticType) based on} the type of the given identifier.
 */
@Deprecated
@Documented
@Retention(RetentionPolicy.CLASS)
// TODO: Implement a value validator instead: @TargetTypes({Block.class, SemanticType.class})
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface BasedOn {
    String value();
}
