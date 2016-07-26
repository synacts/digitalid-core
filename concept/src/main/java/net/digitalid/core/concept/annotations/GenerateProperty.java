package net.digitalid.core.concept.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;

/**
 * Description.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
//@TypeValidator(GenerateProperty.Validator.class)
public @interface GenerateProperty {
    
    @Nonnull String requiredPermissionsToExecuteMethod() default "";
    
    @Nonnull String requiredRestrictionsToExecuteMethod() default "";
    
    @Nonnull String requiredAgentToExecuteMethod() default "";
    
    @Nonnull String requiredPermissionsToSeeMethod() default "";
    
    @Nonnull String requiredRestrictionsToSeeMethod() default "";
    
    @Nonnull String requiredAgentToSeeMethod() default "";
    
    // TODO: Implement a validator.
    
}
