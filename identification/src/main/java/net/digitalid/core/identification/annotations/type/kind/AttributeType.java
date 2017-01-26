package net.digitalid.core.identification.annotations.type.kind;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identity.SemanticType;

/**
 * This annotation indicates that a {@link SemanticType semantic type} denotes an {@link SemanticType#isAttributeType() attribute type}.
 */
@Documented
// TODO: Implement a value validator instead: @TargetTypes(SemanticType.class)
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
public @interface AttributeType {}
