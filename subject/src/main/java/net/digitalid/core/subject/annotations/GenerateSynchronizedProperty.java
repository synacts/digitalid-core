/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.subject.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.circumfixes.Brackets;
import net.digitalid.utility.circumfixes.Quotes;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.map.FreezableLinkedHashMapBuilder;
import net.digitalid.utility.collections.set.FreezableLinkedHashSetBuilder;
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
import net.digitalid.utility.validation.annotations.generation.Provide;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.property.annotations.GeneratePersistentProperty;

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
    @TODO(task = "Use proper inheritance without so much code duplication.", date = "2017-08-19", author = Author.KASPAR_ETTER)
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
            
            final @Nonnull String propertyName = method.getName();
            final @Nonnull String upperCasePropertyName = propertyName.toUpperCase();
            
            final @Nullable TypeMirror returnTypeMirror = method.getReturnType();
            if (returnTypeMirror == null) { ProcessingLog.error("The return type of the annotated method may not be void."); return; }
            if (returnTypeMirror.getKind() != TypeKind.DECLARED) { ProcessingLog.error("The return type of the annotated method has to be a declared type but was $.", ProcessingUtility.getQualifiedName(returnTypeMirror)); return; }
            final @Nonnull DeclaredType returnType = (DeclaredType) returnTypeMirror;
            final @Nonnull List<@Nonnull ? extends TypeMirror> returnTypeArguments = returnType.getTypeArguments();
            
            final @Nonnull String propertyPackage = ProcessingUtility.getQualifiedPackageName(returnType.asElement());
            final @Nonnull String propertyType = Strings.substringFromLast(ProcessingUtility.getSimpleName(returnType), "Persistent");
            final @Nonnull String nonSimplePropertyType = propertyType.replace("Simple", "");
            
            final @Nullable DeclaredType coreSubjectType = ProcessingUtility.getSupertype(typeInformation.getType(), CoreSubject.class);
            if (coreSubjectType == null) { ProcessingLog.error("The type $ is not a subtype of CoreSubject.", ProcessingUtility.getQualifiedName(typeInformation.getType())); return; }
            final @Nonnull DeclaredType subjectType = (DeclaredType) returnTypeArguments.get(0);
            
            final @Nullable String qualifiedKeyConverterName;
            final @Nonnull String qualifiedValueConverterName;
            
            final @Nonnull FiniteIterable<@Nonnull TypeMirror> valueTypes;
            final @Nonnull FiniteIterable<@Nonnull String> externallyProvidedTypes;
            
            if (propertyType.startsWith("Map")) {
                final @Nonnull TypeMirror keyType = returnTypeArguments.get(1);
                final @Nonnull String simpleKeyTypeName = ProcessingUtility.getSimpleName(keyType);
                final @Nonnull String qualifiedKeyTypeName = ProcessingUtility.getQualifiedName(keyType);
                if (qualifiedKeyTypeName.startsWith("java.lang.")) {
                    qualifiedKeyConverterName = "net.digitalid.utility.conversion.converters." + simpleKeyTypeName + "Converter";
                } else {
                    qualifiedKeyConverterName = qualifiedKeyTypeName + "Converter";
                }
                
                final @Nonnull TypeMirror valueType = returnTypeArguments.get(2);
                final @Nonnull String simpleValueTypeName = ProcessingUtility.getSimpleName(valueType);
                final @Nonnull String qualifiedValueTypeName = ProcessingUtility.getQualifiedName(valueType);
                if (qualifiedValueTypeName.startsWith("java.lang.")) {
                    qualifiedValueConverterName = "net.digitalid.utility.conversion.converters." + simpleValueTypeName + "Converter";
                } else {
                    qualifiedValueConverterName = qualifiedValueTypeName + "Converter";
                }
                
                valueTypes = FiniteIterable.of(keyType, valueType);
                
                // TODO: The following code does not yet work for value converters that do not yet exist (i.e. will be generated in the same round).
                final @Nullable TypeElement keyConverterElement = StaticProcessingEnvironment.getElementUtils().getTypeElement(qualifiedKeyConverterName);
                if (keyConverterElement == null) { ProcessingLog.warning("No type element was found for $, which might be because that type will only be generated in this round.", keyConverterElement); }
                final @Nullable DeclaredType keyConverterType = keyConverterElement == null ? null : ProcessingUtility.getSupertype((DeclaredType) keyConverterElement.asType(), Converter.class);
                final @Nullable TypeElement valueConverterElement = StaticProcessingEnvironment.getElementUtils().getTypeElement(qualifiedValueConverterName);
                if (valueConverterElement == null) { ProcessingLog.warning("No type element was found for $, which might be because that type will only be generated in this round.", valueConverterElement); }
                final @Nullable DeclaredType valueConverterType = valueConverterElement == null ? null : ProcessingUtility.getSupertype((DeclaredType) valueConverterElement.asType(), Converter.class);
                externallyProvidedTypes = FiniteIterable.of(getExternallyProvidedType(javaFileGenerator, simpleKeyTypeName, keyConverterType), getExternallyProvidedType(javaFileGenerator, simpleValueTypeName, valueConverterType));
            } else {
                qualifiedKeyConverterName = null;
                final @Nonnull TypeMirror valueType = returnTypeArguments.get(1);
                final @Nonnull String simpleValueTypeName = ProcessingUtility.getSimpleName(valueType);
                final @Nonnull String qualifiedValueTypeName = ProcessingUtility.getQualifiedName(valueType);
                if (qualifiedValueTypeName.startsWith("java.lang.")) {
                    qualifiedValueConverterName = "net.digitalid.utility.conversion.converters." + simpleValueTypeName + "Converter";
                } else {
                    qualifiedValueConverterName = qualifiedValueTypeName + "Converter";
                }
                valueTypes = FiniteIterable.of(valueType);
                
                // TODO: The following code does not yet work for value converters that do not yet exist (i.e. will be generated in the same round).
                final @Nullable TypeElement valueConverterElement = StaticProcessingEnvironment.getElementUtils().getTypeElement(qualifiedValueConverterName);
                if (valueConverterElement == null) { ProcessingLog.warning("No type element was found for $, which might be because that type will only be generated in this round.", valueConverterElement); }
                final @Nullable DeclaredType valueConverterType = valueConverterElement == null ? null : ProcessingUtility.getSupertype((DeclaredType) valueConverterElement.asType(), Converter.class);
                externallyProvidedTypes = FiniteIterable.of(getExternallyProvidedType(javaFileGenerator, simpleValueTypeName, valueConverterType));
            }
            
            final @Nonnull FiniteIterable<@Nonnull String> tableGenericTypes = FiniteIterable.of(coreSubjectType.getTypeArguments()).combine(FiniteIterable.of(subjectType)).combine(valueTypes).map(javaFileGenerator::importIfPossible).combine(externallyProvidedTypes).evaluate();
            final @Nonnull FiniteIterable<@Nonnull String> propertyGenericTypes = FiniteIterable.of(returnTypeArguments).map(javaFileGenerator::importIfPossible).evaluate();
            
            
            final @Nonnull StringBuilder tableField = new StringBuilder("static final @");
            
            tableField.append(javaFileGenerator.importIfPossible(Nonnull.class));
            tableField.append(" ");
            tableField.append(javaFileGenerator.importIfPossible(propertyPackage.replace("database", "core") + ".Synchronized" + nonSimplePropertyType + "Table"));
            tableField.append(tableGenericTypes.join(Brackets.POINTY));
            tableField.append(" ");
            tableField.append(upperCasePropertyName);
            tableField.append("_TABLE = ");
            
            tableField.append(javaFileGenerator.importIfPossible(propertyPackage.replace("database", "core") + ".Synchronized" + nonSimplePropertyType + "TableBuilder"));
            tableField.append(".");
            tableField.append(tableGenericTypes.join(Brackets.POINTY));
            tableField.append("withName(");
            tableField.append(Quotes.inDouble(method.getName()));
            
            tableField.append(").withParentModule(");
            if (!subjectType.equals(typeInformation.getType())) { tableField.append("SUPER_"); }
            tableField.append("MODULE).withRequiredAuthorization(");
            tableField.append(upperCasePropertyName);
            
            tableField.append(").withActionType(");
            tableField.append(javaFileGenerator.importIfPossible(SemanticType.class));
            tableField.append(".map(\"");
            tableField.append(method.getName());
            tableField.append(".");
            tableField.append(typeInformation.getName().toLowerCase());
            tableField.append("@core.digitalid.net\"))");
            
            if (qualifiedKeyConverterName != null) {
                tableField.append(".withKeyConverter(");
                tableField.append(javaFileGenerator.importIfPossible(qualifiedKeyConverterName));
                tableField.append(".INSTANCE)");
            }
            tableField.append(".withValueConverter(");
            tableField.append(javaFileGenerator.importIfPossible(qualifiedValueConverterName));
            tableField.append(".INSTANCE");
            if (propertyType.startsWith("Value")) { tableField.append(").withDefaultValue(").append(method.hasAnnotation(Default.class) ? method.getAnnotation(Default.class).value() : "null"); }
            if (method.hasAnnotation(Provide.class)) { tableField.append(").withProvidedObjectExtractor(").append(method.getAnnotation(Provide.class).value()); }
            tableField.append(").build()");
            
            javaFileGenerator.addField(tableField.toString());
            
            
            final @Nonnull StringBuilder propertyField = new StringBuilder("private final @");
            
            propertyField.append(javaFileGenerator.importIfPossible(Nonnull.class));
            propertyField.append(" ");
            propertyField.append(javaFileGenerator.importIfPossible(propertyPackage + ".WritablePersistent" + propertyType));
            propertyField.append(propertyGenericTypes.join(Brackets.POINTY));
            propertyField.append(" ");
            propertyField.append(method.getName());
            propertyField.append(" = ");
            
            propertyField.append(javaFileGenerator.importIfPossible(propertyPackage.replace("database", "core") + ".WritableSynchronized" + nonSimplePropertyType + "Builder"));
            propertyField.append(".<");
            propertyField.append(tableGenericTypes.limit(4).join());
            if (!propertyType.startsWith("Value")) {
                if (propertyType.startsWith("Simple")) {
                    if (nonSimplePropertyType.startsWith("Set")) {
                        final @Nonnull String valueType = propertyGenericTypes.get(1);
                        propertyField.append(", ReadOnlySet<").append(valueType).append(">, FreezableSet<").append(valueType).append(">");
                    } else {
                        final @Nonnull String keyType = propertyGenericTypes.get(1);
                        final @Nonnull String valueType = propertyGenericTypes.get(2);
                        propertyField.append(", ReadOnlyMap<").append(keyType).append(", ").append(valueType).append(">, FreezableMap<").append(keyType).append(", ").append(valueType).append(">");
                    }
                } else {
                    propertyField.append(propertyGenericTypes.skip(2).join(", ", ""));
                }
            }
            propertyField.append(">withSubject(this)");
            if (!propertyType.startsWith("Value")) {
                if (method.hasAnnotation(Default.class)) { propertyField.append(".with").append(propertyType.substring(0, 3)).append(Brackets.inRound(method.getAnnotation(Default.class).value())); }
                else if (nonSimplePropertyType.startsWith("Set")) { propertyField.append(".withSet(").append(javaFileGenerator.importIfPossible(FreezableLinkedHashSetBuilder.class)).append(".build())"); }
                else if (nonSimplePropertyType.startsWith("Map")) { propertyField.append(".withMap(").append(javaFileGenerator.importIfPossible(FreezableLinkedHashMapBuilder.class)).append(".build())"); }
            }
            propertyField.append(".withTable(");
            propertyField.append(upperCasePropertyName).append("_TABLE");
            propertyField.append(").build()");
            
            javaFileGenerator.addField(propertyField.toString());
        }
        
    }
    
}
