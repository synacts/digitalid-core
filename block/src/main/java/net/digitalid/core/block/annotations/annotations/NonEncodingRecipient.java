package net.digitalid.core.block.annotations.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * This annotation indicates that a method should only be invoked on non-{@link Exposed exposed} objects.
 * 
 * @see Encoding
 * @see NonEncoding
 * @see EncodingRecipient
 */
@Documented
//@TargetTypes(Block.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface NonEncodingRecipient {}
