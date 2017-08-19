package net.digitalid.core.subject.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.type.DeclaredType;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.generator.annotations.meta.Interceptor;
import net.digitalid.utility.generator.information.method.MethodInformation;
import net.digitalid.utility.generator.information.type.TypeInformation;
import net.digitalid.utility.processing.logging.ProcessingLog;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.processing.utility.StaticProcessingEnvironment;
import net.digitalid.utility.processor.generator.JavaFileGenerator;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.property.annotations.GenerateSubjectModule;

import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.subject.CoreSubjectModule;
import net.digitalid.core.subject.CoreSubjectModuleBuilder;

/**
 * This method interceptor generates a core subject module with the service, the index, with which factory an instance of the type is built, and its converter.
 * 
 * @see GenerateSynchronizedProperty
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(GenerateCoreSubjectModule.Interceptor.class)
public @interface GenerateCoreSubjectModule {
    
    /**
     * This class generates the interceptor for the surrounding annotation.
     */
    @Stateless
    public static class Interceptor extends GenerateSubjectModule.Interceptor {
        
        @PureWithSideEffects
        private void generateModuleField(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull TypeInformation typeInformation, @Nonnull DeclaredType coreSubjectType, @Nonnull DeclaredType subjectType, boolean supperClass) {
            final @Nonnull FiniteIterable<@Nonnull String> types = FiniteIterable.of(coreSubjectType.getTypeArguments()).combine(FiniteIterable.of(subjectType)).map(javaFileGenerator::importIfPossible);
            
            final @Nonnull StringBuilder field = new StringBuilder("static final @");
            field.append(javaFileGenerator.importIfPossible(Nonnull.class));
            field.append(" ");
            field.append(javaFileGenerator.importIfPossible(CoreSubjectModule.class));
            field.append(types.join(Brackets.POINTY));
            if (supperClass) { field.append(" SUPER_MODULE = "); } else { field.append(" MODULE = "); }
            field.append(javaFileGenerator.importIfPossible(CoreSubjectModuleBuilder.class));
            field.append(".");
            field.append(types.join(Brackets.POINTY));
            field.append("withService(SERVICE).withSubjectFactory(");
            field.append(typeInformation.getSimpleNameOfGeneratedSubclass()).append("::new"); // TODO: Maybe rather make the subject factory optional because the supertype does not need it? SuperType::of fails because it throws a DatabaseException.
            field.append(").withEntityTable(");
            field.append(javaFileGenerator.importIfPossible("net.digitalid.core.entity." + Strings.substringUntilFirst(types.get(0), '<') + "Converter")).append(".INSTANCE");
            field.append(").withCoreSubjectTable(");
            field.append(javaFileGenerator.importIfPossible(ProcessingUtility.getQualifiedName(subjectType) + "Converter"));
            field.append(".INSTANCE).build()");
            javaFileGenerator.addField(field.toString());
        }
        
        @Override
        @PureWithSideEffects
        public void generateFieldsRequiredByMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull TypeInformation typeInformation) {
            final @Nonnull DeclaredType subjectType = typeInformation.getType();
            final @Nullable DeclaredType coreSubjectType = ProcessingUtility.getSupertype(subjectType, CoreSubject.class);
            if (coreSubjectType == null) { ProcessingLog.error("The type $ is not a subtype of CoreSubject.", ProcessingUtility.getQualifiedName(subjectType)); return; }
            
            final @Nonnull DeclaredType superType = (DeclaredType) StaticProcessingEnvironment.getTypeUtils().directSupertypes(subjectType).get(0);
            if (ProcessingUtility.hasAnnotation(superType.asElement(), GenerateTableConverter.class)) { generateModuleField(javaFileGenerator, typeInformation, coreSubjectType, superType, true); }
            generateModuleField(javaFileGenerator, typeInformation, coreSubjectType, subjectType, false);
        }
        
    }
    
}
