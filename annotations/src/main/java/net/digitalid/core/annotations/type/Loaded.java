package net.digitalid.core.annotations.type;

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
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.processing.utility.TypeImporter;
import net.digitalid.utility.validation.annotations.meta.ValueValidator;
import net.digitalid.utility.validation.annotations.type.Stateless;
import net.digitalid.utility.validation.contract.Contract;
import net.digitalid.utility.validation.validator.ValueAnnotationValidator;

/**
 * This annotation indicates that the declaration of a type is {@link Loadable#isLoaded() loaded}.
 * 
 * @see NonLoaded
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@ValueValidator(Loaded.Validator.class)
@Target({ElementType.TYPE_USE, ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface Loaded {
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    /**
     * This class checks the use of and generates the contract for the surrounding annotation.
     */
    @Stateless
    public static class Validator implements ValueAnnotationValidator {
        
        private static final @Nonnull FiniteIterable<@Nonnull Class<?>> targetTypes = FiniteIterable.of(Loadable.class);
        
        @Pure
        @Override
        public @Nonnull FiniteIterable<@Nonnull Class<?>> getTargetTypes() {
            return targetTypes;
        }
        
        @Pure
        @Override
        public @Nonnull Contract generateContract(@Nonnull Element element, @Nonnull AnnotationMirror annotationMirror, @NonCaptured @Modified @Nonnull TypeImporter typeImporter) {
            return Contract.with("# == null || #.isLoaded()", "The # has to be null or loaded.", element);
        }
        
    }
    
}
