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
package net.digitalid.core.subject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.interceptors.Cached;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.storage.TableImplementation;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.property.subject.SubjectModule;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.unit.CoreUnit;

/**
 * This class converts a {@link CoreSubject core subject} with its {@link Entity entity} and is used as the {@link SubjectModule#getSubjectTable() subject converter} of {@link PersistentProperty persistent properties}.
 */
@Immutable
@GenerateSubclass
public abstract class CoreSubjectTable<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>> extends TableImplementation<SUBJECT, @Nonnull CoreUnit> {
    
    /* -------------------------------------------------- Parent Module -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull CoreSubjectModule<ENTITY, KEY, SUBJECT> getParentModule();
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super SUBJECT> getType() {
        return getParentModule().getCoreSubjectTable().getType();
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return getParentModule().getCoreSubjectTable().getTypeName();
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return getParentModule().getCoreSubjectTable().getTypePackage();
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Cached
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        return ImmutableList.withElements(CustomField.with(CustomType.TUPLE.of(getParentModule().getEntityTable()), "entity", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(getParentModule().getCoreSubjectTable()), "key", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)/* TODO: Pass them? Probably pass the whole custom field instead. */))
        );
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@Nonnull SUBJECT subject, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        getParentModule().getEntityTable().convert(subject.getEntity(), encoder);
        getParentModule().getCoreSubjectTable().convert(subject, encoder);
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable <@Unspecifiable EXCEPTION extends ConnectionException> @Nullable SUBJECT recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nonnull CoreUnit unit) throws EXCEPTION, RecoveryException {
        final @Nonnull ENTITY entity = getParentModule().getEntityTable().recover(decoder, unit);
        return getParentModule().getCoreSubjectTable().recover(decoder, entity);
    }
    
}
