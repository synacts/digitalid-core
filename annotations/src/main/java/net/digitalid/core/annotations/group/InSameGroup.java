package net.digitalid.core.annotations.group;

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


import net.digitalid.utility.processing.logging.ErrorLogger;
import net.digitalid.utility.processing.logging.SourcePosition;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.processing.utility.TypeImporter;
import net.digitalid.utility.validation.annotations.meta.ValueValidator;
import net.digitalid.utility.validation.annotations.type.Stateless;
import net.digitalid.utility.validation.contract.Contract;
import net.digitalid.utility.validation.validators.StringValidator;

/**
 * This annotation indicates that the annotated group member is in the same group as the surrounding group member.
 * 
 * @see GroupMember
 * @see InGroup
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@ValueValidator(InSameGroup.Validator.class)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface InSameGroup {
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    /**
     * This class checks the use of and generates the contract for the surrounding annotation.
     */
    @Stateless
    public static class Validator extends StringValidator {
        
        private static final @Nonnull FiniteIterable<@Nonnull Class<?>> targetTypes = FiniteIterable.of(GroupMember.class);
        
        @Pure
        @Override
        public @Nonnull FiniteIterable<@Nonnull Class<?>> getTargetTypes() {
            return targetTypes;
        }
        
        @Pure
        @Override
        public void checkUsage(@Nonnull Element element, @Nonnull AnnotationMirror annotationMirror, @NonCaptured @Modified @Nonnull ErrorLogger errorLogger) {
            super.checkUsage(element, annotationMirror, errorLogger);
            
            if (!ProcessingUtility.isRawSubtype(ProcessingUtility.getSurroundingType(element), GroupMember.class)) {
                errorLogger.log("The annotation $ may only be used in group members:", SourcePosition.of(element, annotationMirror), getAnnotationNameWithLeadingAt());
            }
        }
        
        @Pure
        @Override
        public @Nonnull Contract generateContract(@Nonnull Element element, @Nonnull AnnotationMirror annotationMirror, @NonCaptured @Modified @Nonnull TypeImporter typeImporter) {
            return Contract.with("# == null || #.isIn(getGroup())", "The # has to be in the same group.", element);
        }
        
    }
    
}
