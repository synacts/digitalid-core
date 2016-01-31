package net.digitalid.core.conversion.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.annotations.meta.TargetTypes;

import net.digitalid.core.conversion.Block;

/**
 * This annotation indicates that a {@link Block block} is {@link Block#isEncoding() encoding}.
 * 
 * @see NonEncoding
 * @see EncodingRecipient
 * @see NonEncodingRecipient
 */
@Documented
@TargetTypes(Block.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Encoding {}
