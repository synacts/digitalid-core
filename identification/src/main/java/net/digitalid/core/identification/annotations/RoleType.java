package net.digitalid.core.identification.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.core.identification.identity.SemanticType;

/**
 * This annotation indicates that a {@link SemanticType semantic type} denotes a {@link SemanticType#isRoleType() role type}.
 */
@Documented
// TODO: Implement a value validator instead: @TargetTypes(SemanticType.class)
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.CLASS)
public @interface RoleType {}
