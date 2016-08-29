package net.digitalid.core.block.annotations.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation indicates that a {@link Block block} is {@link Block#isEncoded() encoded}.
 * 
 * @see NonEncoded
 */
@Documented
//@TargetTypes(Block.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Encoded {}
