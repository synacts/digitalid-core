package net.digitalid.core.conversion.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.meta.TargetType;

import net.digitalid.core.conversion.Block;

/**
 * This annotation indicates that a method should only be invoked on {@link Block#isEncoding() encoding} {@link Block blocks}.
 * 
 * @see Encoding
 * @see NonEncoding
 * @see NonEncodingRecipient
 */
@Documented
@TargetType(Block.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface EncodingRecipient {}
