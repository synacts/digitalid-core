package net.digitalid.core.concept.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//@TypeValidator(GenerateProperty.Validator.class)
public @interface GenerateIndex {
    
    // TODO: Implement a validator.
    
}
