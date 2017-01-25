package net.digitalid.core.subject.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.meta.Interceptor;
import net.digitalid.utility.generator.information.method.MethodInformation;
import net.digitalid.utility.generator.information.type.TypeInformation;
import net.digitalid.utility.processing.logging.ProcessingLog;
import net.digitalid.utility.processing.utility.ProcessingUtility;
import net.digitalid.utility.processing.utility.StaticProcessingEnvironment;
import net.digitalid.utility.processor.generator.JavaFileGenerator;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.subject.annotations.GeneratePersistentProperty;

import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.subject.CoreSubject;

/**
 * This method interceptor generates a persistent property with the corresponding property table.
 * 
 * @see GenerateCoreSubjectModule
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Interceptor(GenerateSynchronizedProperty.Interceptor.class)
public @interface GenerateSynchronizedProperty {
    
    /**
     * This class generates the interceptor for the surrounding annotation.
     */
    @Stateless
    public static class Interceptor extends GeneratePersistentProperty.Interceptor {
        
        @Pure
        @Override
        @TODO(task = "Implement the value validation part as well!", date = "2016-12-11", author = Author.KASPAR_ETTER)
        public void generateFieldsRequiredByMethod(@Nonnull JavaFileGenerator javaFileGenerator, @Nonnull MethodInformation method, @Nonnull TypeInformation typeInformation) {
//            final @Nonnull FreezableArrayList<@Nonnull Contract> contracts = FreezableArrayList.withNoElements();
//            
//            final @Nullable TypeMirror componentType = ProcessingUtility.getComponentType(method.getReturnType());
//            final @Nullable TypeElement typeElement = ProcessingUtility.getTypeElement(componentType);
//            final @Modifiable @Nonnull Map<@Nonnull AnnotationMirror, @Nonnull ValueAnnotationValidator> valueValidators = AnnotationHandlerUtility.getValueValidators(typeElement);
//            for (Map.@Nonnull Entry<@Nonnull AnnotationMirror, @Nonnull ValueAnnotationValidator> valueValidatorEntry : valueValidators.entrySet()) {
//                final @Nullable Contract contract = valueValidatorEntry.getValue().generateContract(typeElement, valueValidatorEntry.getKey(), javaFileGenerator);
//                contracts.add(contract);
//            }
//            
//            final @Nonnull String validationContent = contracts.map(contract -> javaFileGenerator.importIfPossible(Require.class) + ".that" + Brackets.inRound(contract.getCondition()) + ".orThrow" + Brackets.inRound(contract.getMessage() + ", " + contract.getArguments().join()) + ";").join("\n");
//            
//            javaFileGenerator.addField("private static final @" + javaFileGenerator.importIfPossible(Nonnull.class) + " " + javaFileGenerator.importIfPossible(FailableConsumer.class) + Brackets.inPointy(javaFileGenerator.importIfPossible(String.class) + ", " + javaFileGenerator.importIfPossible(PreconditionViolationException.class)) + method.getName().toUpperCase() + "_VALIDATOR = new " + javaFileGenerator.importIfPossible(FailableConsumer.class) + Brackets.inPointy(javaFileGenerator.importIfPossible(String.class) + ", " + javaFileGenerator.importIfPossible(PreconditionViolationException.class)) + "() {\n\n " +
//                    
//                    "@" + javaFileGenerator.importIfPossible(Impure.class) + "\n" +
//                    "@" + javaFileGenerator.importIfPossible(Override.class) + "\n" +
//                    "public void consume(@" + javaFileGenerator.importIfPossible(Captured.class) + javaFileGenerator.importIfPossible(String.class) + " password) throws " + javaFileGenerator.importIfPossible(PreconditionViolationException.class) + " {\n" + validationContent + "\n}" +
//            "}\n}");
            
            // TODO: Clean up the following mess!
            
            final @Nonnull String upperCasePropertyName = method.getName().toUpperCase();
            final @Nonnull String propertyPackage = ProcessingUtility.getQualifiedPackageName(((DeclaredType) method.getReturnType()).asElement());
            final @Nonnull String propertyType = Strings.substringFromLast(ProcessingUtility.getSimpleName(method.getReturnType()), "Persistent");
            
            final @Nullable DeclaredType subjectType = ProcessingUtility.getSupertype(typeInformation.getType(), CoreSubject.class);
            if (subjectType == null) { ProcessingLog.error("The type $ is not a subtype of CoreSubject.", ProcessingUtility.getQualifiedName(typeInformation.getType())); }
            final @Nonnull TypeMirror valueType = ((DeclaredType) method.getReturnType()).getTypeArguments().get(1);
            
            // TODO: The following code does not yet work for value converters that do not yet exist (i.e. will be generated in the same round).
            final @Nonnull String valueConverterName = ProcessingUtility.getQualifiedName(valueType) + "Converter";
            final @Nullable TypeElement valueConverterElement = StaticProcessingEnvironment.getElementUtils().getTypeElement(valueConverterName);
            if (valueConverterElement == null) { ProcessingLog.warning("No type element was found for $, which might be because that type will only be generated in this round.", valueConverterElement); }
            final @Nullable DeclaredType valueConverterType = valueConverterElement == null ? null : ProcessingUtility.getSupertype((DeclaredType) valueConverterElement.asType(), Converter.class);
            
            final @Nonnull String externallyProvidedType;
            if (valueConverterType != null) {
                externallyProvidedType = javaFileGenerator.importIfPossible(valueConverterType.getTypeArguments().get(1));
            } else {
                externallyProvidedType = "Void";
            }
            
            final @Nonnull FiniteIterable<@Nonnull String> types = FiniteIterable.of(subjectType.getTypeArguments()).combine(FiniteIterable.of(typeInformation.getType(), valueType)).map(javaFileGenerator::importIfPossible).evaluate().combine(FiniteIterable.of(externallyProvidedType));
            
            final @Nonnull String valueConverter = (types.get(3).equals("String") ? javaFileGenerator.importIfPossible("net.digitalid.utility.conversion.converters.StringConverter") : javaFileGenerator.importIfPossible(ProcessingUtility.getQualifiedName(valueType) + "Converter")) + ".INSTANCE";
            
            javaFileGenerator.addField("/* TODO: private */ static final @" + javaFileGenerator.importIfPossible(Nonnull.class) + " " + javaFileGenerator.importIfPossible(propertyPackage.replace("database", "core") + ".Synchronized" + propertyType + "Table") + types.join(Brackets.POINTY) + " " + upperCasePropertyName + "_TABLE = " + javaFileGenerator.importIfPossible(propertyPackage.replace("database", "core") + ".Synchronized" + propertyType + "TableBuilder") + "." + types.join(Brackets.POINTY) + "withName" + Brackets.inRound(Quotes.inDouble(method.getName())) + ".withValueConverter" + Brackets.inRound(valueConverter) + ".withDefaultValue" + Brackets.inRound(method.hasAnnotation(Default.class) ? method.getAnnotation(Default.class).value() : "null") + ".withParentModule(MODULE).withRequiredAuthorization(" + upperCasePropertyName + ").withActionType(" + javaFileGenerator.importIfPossible(SemanticType.class) + ".map(\"" + method.getName() + "." + typeInformation.getName().toLowerCase() + "@core.digitalid.net\")).build()");
            
            javaFileGenerator.addField("private final @" + javaFileGenerator.importIfPossible(Nonnull.class) + " " + javaFileGenerator.importIfPossible(propertyPackage + ".WritablePersistent" + propertyType) + Brackets.inPointy(types.get(2) + ", " + types.get(3)) + " " + method.getName() + " = " + javaFileGenerator.importIfPossible(propertyPackage.replace("database", "core") + ".WritableSynchronized" + propertyType + "Builder") + "." + types.limit(4).join(Brackets.POINTY) + "withConcept(this).withTable" + Brackets.inRound(upperCasePropertyName + "_TABLE") + ".build()");
        }
        
    }
    
}
