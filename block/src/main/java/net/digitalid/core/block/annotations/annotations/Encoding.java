package net.digitalid.core.block.annotations.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.processing.logging.ErrorLogger;
import net.digitalid.utility.processing.logging.SourcePosition;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.validation.annotations.meta.TypeValidator;
import net.digitalid.utility.validation.annotations.type.Stateless;
import net.digitalid.utility.validation.validator.TypeAnnotationValidator;

import net.digitalid.core.block.Block;


/**
 * This annotation indicates that a {@link Block block} is {@link Block#isEncoding() encoding}.
 * 
 * @see NonEncoding
 * @see EncodingRecipient
 * @see NonEncodingRecipient
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@TypeValidator(Encoding.Validator.class)
@Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Encoding {
    
    /**
     * This class checks the use of the surrounding annotation.
     */
    @Stateless
    public static class Validator implements TypeAnnotationValidator {
        
        @Pure
        @Override
        public void checkUsage(@Nonnull Element element, @Nonnull AnnotationMirror annotationMirror, @NonCaptured @Modified @Nonnull ErrorLogger errorLogger) {
            final @Nonnull TypeElement typeElement = (TypeElement) element;
            if (!ProcessingUtility.isRawSubtype(typeElement, Block.class)) {
                errorLogger.log("The type $ has to implement the block class.", SourcePosition.of(element, annotationMirror), typeElement);
            }
        }
        
    }
    
}
