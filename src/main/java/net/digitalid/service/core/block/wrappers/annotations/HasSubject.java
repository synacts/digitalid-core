package net.digitalid.service.core.block.wrappers.annotations;

import net.digitalid.service.core.block.wrappers.SignatureWrapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.utility.annotations.meta.TargetType;

/**
 * This annotation indicates that a {@link SignatureWrapper signature} has a {@link SignatureWrapper#hasSubject() subject}.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TargetType(SignatureWrapper.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface HasSubject {}
