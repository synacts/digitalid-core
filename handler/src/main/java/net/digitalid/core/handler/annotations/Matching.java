package net.digitalid.core.handler.annotations;

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
import net.digitalid.utility.processing.logging.ErrorLogger;
import net.digitalid.utility.processing.logging.SourcePosition;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.processing.utility.TypeImporter;
import net.digitalid.utility.validation.annotations.meta.ValueValidator;
import net.digitalid.utility.validation.annotations.type.Stateless;
import net.digitalid.utility.validation.contract.Contract;
import net.digitalid.utility.validation.validator.ValueAnnotationValidator;

import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.Reply;

/**
 * This annotation indicates that a {@link Reply} {@link Reply#matches(net.digitalid.core.handler.method.Method) matches} a {@link Method}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ValueValidator(Matching.Validator.class)
public @interface Matching {
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    /**
     * This class checks the use of and generates the contract for the surrounding annotation.
     */
    @Stateless
    public static class Validator implements ValueAnnotationValidator {
        
        @Pure
        @Override
        public void checkUsage(@Nonnull Element element, @Nonnull AnnotationMirror annotationMirror, @NonCaptured @Modified @Nonnull ErrorLogger errorLogger) {
            if (!ProcessingUtility.isRawSubtype(element, Reply.class)) {
                errorLogger.log("The element $ has to be an instance of the reply class.", SourcePosition.of(element, annotationMirror), element);
            }
            if (!ProcessingUtility.isRawSubtype(ProcessingUtility.getSurroundingType(element), Method.class)) {
                errorLogger.log("The annotation '@Matching' can only be used in the method type.", SourcePosition.of(element, annotationMirror));
            }
        }
        
        @Pure
        @Override
        public @Nonnull Contract generateContract(@Nonnull Element element, @Nonnull AnnotationMirror annotationMirror, @NonCaptured @Modified @Nonnull TypeImporter typeImporter) {
            return Contract.with("# == null || #.matches(this)", "The reply has to be null or match this method.", element);
        }
        
    }
    
}
