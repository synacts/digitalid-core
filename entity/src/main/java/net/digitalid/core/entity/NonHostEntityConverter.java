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
package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.unit.CoreUnit;

/**
 * This class implements the converter for {@link NonHostEntity}.
 */
@Immutable
public class NonHostEntityConverter extends AbstractEntityConverter<NonHostEntity> {
    
    public static final @Nonnull NonHostEntityConverter INSTANCE = new NonHostEntityConverter();
    
    @Pure
    @Override
    public @Nonnull Class<NonHostEntity> getType() {
        return NonHostEntity.class;
    }
    
    @Pure
    @Override
    public @Nonnull String getTypeName() {
        return "NonHostEntity";
    }
    
    @Pure
    @Override
    public @Nonnull String getTypePackage() {
        return "net.digitalid.core.entity";
    }
    
    @Pure
    @Override
    protected @Nonnull NonHostEntity recover(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException {
        return NonHostEntity.with(unit, key);
    }
    
}
