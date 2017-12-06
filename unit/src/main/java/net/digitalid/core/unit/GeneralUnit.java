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
package net.digitalid.core.unit;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.dialect.statement.schema.SQLCreateSchemaStatementBuilder;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;

/**
 * This class provides a general database unit.
 */
@Utility
public abstract class GeneralUnit {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    /**
     * Stores the database unit for non-unit-specific tables.
     */
    public static final @Nonnull Unit INSTANCE = () -> "general";
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores a dummy configuration in order to have an initialization target.
     */
    public static final @Nonnull Configuration<Boolean> configuration = Configuration.with(Boolean.TRUE);
    
    /* -------------------------------------------------- Creation -------------------------------------------------- */
    
    /**
     * Creates the database table.
     */
    @Committing
    @PureWithSideEffects
    @TODO(task = "Remove this initializer again once a unit creates itself.", date = "2017-07-25", author = Author.KASPAR_ETTER)
    @Initialize(target = GeneralUnit.class, dependencies = Database.class)
    public static void createTable() throws DatabaseException {
        Database.instance.get().execute(SQLCreateSchemaStatementBuilder.build(), INSTANCE);
    }
    
}
