package net.digitalid.service.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.wrappers.Block;

/**
 * This annotation indicates that a method should only be invoked on non-{@link Exposed exposed} objects.
 * 
 * @see Encoding
 * @see NonEncoding
 * @see EncodingRecipient
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Documented
@TargetType(Block.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface NonEncodingRecipient {}
