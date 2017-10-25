package net.digitalid.core.unit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.processing.utility.TypeImporter;
import net.digitalid.utility.validation.annotations.meta.ValueValidator;
import net.digitalid.utility.validation.annotations.type.Stateless;
import net.digitalid.utility.validation.contract.Contract;

/**
 * This annotation indicates that the annotated object is {@link CoreUnitBased#isOnClient() on a client}.
 * 
 * @see OnHost
 * @see OnHostRecipient
 * @see OnClientRecipient
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ValueValidator(OnClient.Validator.class)
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface OnClient {
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    /**
     * This class checks the use of and generates the contract for the surrounding annotation.
     */
    @Stateless
    public static class Validator extends UnitBasedValidator {
        
        @Pure
        @Override
        public @Nonnull Contract generateContract(@Nonnull Element element, @Nonnull AnnotationMirror annotationMirror, @NonCaptured @Modified @Nonnull TypeImporter typeImporter) {
            return Contract.with("# == null || #.isOnClient()", "The # has to be null or be on a client.", element);
        }
        
    }
    
}
