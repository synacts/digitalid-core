package net.digitalid.service.core.block.wrappers.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;
import net.digitalid.utility.validation.meta.TargetType;

/**
 * This annotation indicates that a {@link SignatureWrapper signature} is {@link SignatureWrapper#isSigned() signed}.
 * 
 * @see NonSigned
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TargetType(SignatureWrapper.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Signed {}
