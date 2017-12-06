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
package net.digitalid.core.property.set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.PersistentProperty;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.subject.CoreSubject;

/**
 * This class implements the {@link Converter} for {@link SetPropertyInternalAction}.
 */
@Immutable
@GenerateSubclass
public abstract class SetPropertyInternalActionConverter<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable VALUE, @Specifiable PROVIDED_FOR_VALUE> extends RootClass implements Converter<SetPropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE>, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity>> {
    
    /* -------------------------------------------------- Property Table -------------------------------------------------- */
    
    /**
     * Returns the property table, which contains the various converters.
     */
    @Pure
    protected abstract @Nonnull SynchronizedSetPropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> getPropertyTable();
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    protected void initialize() {
        super.initialize();
        
        MethodIndex.add(this, getPropertyTable().getActionType());
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super SetPropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE>> getType() {
        return SetPropertyInternalAction.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "SetPropertyInternalAction";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.property.set";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull SynchronizedSetPropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(propertyTable.getParentModule().getSubjectTable()), "subject", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(propertyTable.getValueConverter()), "value", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.BOOLEAN, "added")
        );
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull SetPropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE> action, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        final @Nonnull SynchronizedSetPropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        encoder.encodeObject(propertyTable.getParentModule().getCoreSubjectTable(), action.getProperty().getSubject());
        encoder.encodeObject(propertyTable.getValueConverter(), action.getValue());
        encoder.encodeBoolean(action.isAdded());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull SetPropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity> pair) throws EXCEPTION, RecoveryException {
        final @Nullable Signature<Compression<Pack>> signature = pair.get0();
        final @Nonnull Entity entity = pair.get1();
        
        if (!(entity instanceof NonHostEntity)) { throw RecoveryExceptionBuilder.withMessage("Internal actions can only be recovered for non-host entities.").build(); }
        
        final @Nonnull SynchronizedSetPropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        final @Nonnull SUBJECT subject = decoder.decodeObject(propertyTable.getParentModule().getCoreSubjectTable(), (ENTITY) entity);
        final @Nonnull PersistentProperty<?, ?> persistentProperty = subject.getProperty(propertyTable);
        
        if (!(persistentProperty instanceof WritableSynchronizedSetProperty)) { throw RecoveryExceptionBuilder.withMessage("The recovered persistent property is not a synchronized set property.").build(); }
        
        final @Nonnull WritableSynchronizedSetProperty<ENTITY, KEY, SUBJECT, VALUE, ?, ?> synchronizedProperty = (WritableSynchronizedSetProperty<ENTITY, KEY, SUBJECT, VALUE, ?, ?>) persistentProperty;
        
        final @Nonnull VALUE value = decoder.decodeObject(propertyTable.getValueConverter(), propertyTable.getProvidedObjectExtractor().evaluate(subject));
        if (!propertyTable.getValueValidator().evaluate(value))  { throw RecoveryExceptionBuilder.withMessage(Strings.format("The value $ is not valid.", value)).build(); }
        
        final boolean added = decoder.decodeBoolean();
        
        return new SetPropertyInternalActionSubclass<>(signature, synchronizedProperty, value, added);
    }
    
}
