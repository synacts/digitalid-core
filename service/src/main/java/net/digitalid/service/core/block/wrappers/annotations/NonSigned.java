package net.digitalid.service.core.block.wrappers.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.digitalid.utility.validation.meta.TargetType;

import net.digitalid.service.core.block.wrappers.signature.SignatureWrapper;

/**
 * This annotation indicates that a {@link SignatureWrapper signature} is {@link SignatureWrapper#isNotSigned() not signed}.
 * 
 * @see Signed
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TargetType(SignatureWrapper.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface NonSigned {}
