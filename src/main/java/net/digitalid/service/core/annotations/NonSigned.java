package net.digitalid.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This annotation indicates that a {@link SignatureWrapper signature} is {@link SignatureWrapper#isNotSigned() not signed}.
 * 
 * @see Signed
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TargetType(SignatureWrapper.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface NonSigned {}
