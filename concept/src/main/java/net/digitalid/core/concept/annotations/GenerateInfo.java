package net.digitalid.core.concept.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.meta.Interceptor;
import net.digitalid.utility.generator.information.method.MethodInformation;
import net.digitalid.utility.generator.information.type.TypeInformation;
import net.digitalid.utility.generator.interceptor.MethodInterceptor;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.processing.utility.StaticProcessingEnvironment;
import net.digitalid.utility.processor.generator.JavaFileGenerator;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.concept.Concept;
import net.digitalid.core.concept.ConceptIndexBuilder;
import net.digitalid.core.concept.ConceptInfo;
import net.digitalid.core.concept.ConceptInfoBuilder;
import net.digitalid.core.entity.NonHostEntity;

/**
 * This annotation is used to indicate that an info object must be generated, which contains information about the service, the index, with which factory an instance of the type is built and its converter.
 */
@Documented
@Target(ElementType.METHOD)
@Interceptor(GenerateInfo.Interceptor.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateInfo {
    
    /**
     * This class generates content for the annotated method.
     */
    @Stateless
    public static class Interceptor extends MethodInterceptor {
        
        @Pure
        @Override
        protected @Nonnull @NonEmpty String getPrefix() {
            return "implemented";
        }
        
        @Pure
        @Override
        public void generateFieldsRequiredByMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull TypeInformation typeInformation) {
            final @Nonnull List<@Nonnull ? extends TypeMirror> superTypes = StaticProcessingEnvironment.getTypeUtils().directSupertypes(typeInformation.getType());
            final @Nonnull DeclaredType conceptType = (DeclaredType) FiniteIterable.of(superTypes).findUnique(superType -> ProcessingUtility.isRawlyAssignable(superType, Concept.class));
//            final @Nonnull FiniteIterable<@Nonnull TypeVariable> typeArguments = FiniteIterable.of(conceptType.getTypeArguments()).instanceOf(TypeVariable.class);
            final FiniteIterable<? extends TypeMirror> typeArguments = FiniteIterable.of(conceptType.getTypeArguments());
    
            Require.that(typeArguments.size() == 2).orThrow("Expected two type arguments for type $, but got $", typeInformation.getName(), typeArguments.size());
            
            javaFileGenerator.addField("static final @" + javaFileGenerator.importIfPossible(Nonnull.class) + " " + javaFileGenerator.importIfPossible(ConceptInfo.class) + Brackets.inPointy(javaFileGenerator.importIfPossible(typeArguments.get(0)) + ", " + javaFileGenerator.importIfPossible(typeArguments.get(1)) + ", " + typeInformation.getName()) + " INFO = " + javaFileGenerator.importIfPossible(ConceptInfoBuilder.class) + "." + Brackets.inPointy(javaFileGenerator.importIfPossible(typeArguments.get(0)) + ", " + javaFileGenerator.importIfPossible(typeArguments.get(1)) + ", " + typeInformation.getName()) + "withService" + Brackets.inRound("SERVICE") + ".withName" + Brackets.inRound(Quotes.inDouble(typeInformation.getName())) + ".withIndex" + Brackets.inRound(javaFileGenerator.importIfPossible(ConceptIndexBuilder.class) + ".buildWithFactory" + Brackets.inRound(typeInformation.getSimpleNameOfGeneratedSubclass() + "::new")) + ".withConverter" + Brackets.inRound(typeInformation.getSimpleNameOfGeneratedConverter() + ".INSTANCE") + ".build()");
        }
    
        @Pure
        @Override
        protected void implementInterceptorMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull String statement, @Nullable String resultVariable, @Nullable String defaultValue) {
        }
    
    }
    
}
