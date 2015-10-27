package net.digitalid.service.core.block.annotations;

import net.digitalid.service.core.block.Block;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.utility.annotations.meta.TargetType;

/**
 * This annotation indicates that a {@link Block block} is not {@link Block#isEncoding() encoding}.
 * 
 * @see Encoding
 * @see EncodingRecipient
 * @see NonEncodingRecipient
 */
@Documented
@TargetType(Block.class)
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface NonEncoding {}
