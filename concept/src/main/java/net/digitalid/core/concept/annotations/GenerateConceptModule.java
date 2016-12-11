package net.digitalid.core.concept.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.type.DeclaredType;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.meta.Interceptor;
import net.digitalid.utility.generator.information.method.MethodInformation;
import net.digitalid.utility.generator.information.type.TypeInformation;
import net.digitalid.utility.processing.logging.ProcessingLog;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.processor.generator.JavaFileGenerator;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.subject.annotations.GenerateSubjectModule;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.concept.ConceptModule;
import net.digitalid.core.concept.ConceptModuleBuilder;

/**
 * This method interceptor generates a concept module with the service, the index, with which factory an instance of the type is built, and its converter.
 * 
 * @see GenerateSynchronizedProperty
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(GenerateConceptModule.Interceptor.class)
public @interface GenerateConceptModule {
    
    /**
     * This class generates the interceptor for the surrounding annotation.
     */
    @Stateless
    public static class Interceptor extends GenerateSubjectModule.Interceptor {
        
        @Pure
        @Override
        public void generateFieldsRequiredByMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull TypeInformation typeInformation) {
            final @Nullable DeclaredType conceptType = ProcessingUtility.getSupertype(typeInformation.getType(), Concept.class);
            if (conceptType == null) { ProcessingLog.error("The type $ is not a subtype of Concept.", ProcessingUtility.getQualifiedName(typeInformation.getType())); }
            final @Nonnull FiniteIterable<@Nonnull String> types = FiniteIterable.of(conceptType.getTypeArguments()).combine(FiniteIterable.of(typeInformation.getType())).map(javaFileGenerator::importIfPossible).evaluate();
            
            javaFileGenerator.addField("static final @" + javaFileGenerator.importIfPossible(Nonnull.class) + " " + javaFileGenerator.importIfPossible(ConceptModule.class) + types.join(Brackets.POINTY) + " MODULE = " + javaFileGenerator.importIfPossible(ConceptModuleBuilder.class) + "." + types.join(Brackets.POINTY) + "withService(SERVICE).withConceptFactory" + Brackets.inRound(typeInformation.getSimpleNameOfGeneratedSubclass() + "::new") + ".withEntityConverter" + Brackets.inRound(javaFileGenerator.importIfPossible("net.digitalid.core.entity." + types.get(0) + "Converter") + ".INSTANCE") + ".withConceptConverter" + Brackets.inRound(typeInformation.getSimpleNameOfGeneratedConverter() + ".INSTANCE") + ".build()");
        }
        
    }
    
}
