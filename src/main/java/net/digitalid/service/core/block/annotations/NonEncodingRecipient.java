package net.digitalid.service.core.block.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.block.Block;
import net.digitalid.utility.annotations.meta.TargetType;

/**
 * This annotation indicates that a method should only be invoked on non-{@link Exposed exposed} objects.
 * 
 * @see Encoding
 * @see NonEncoding
 * @see EncodingRecipient
 */
@Documented
@TargetType(Block.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface NonEncodingRecipient {}
