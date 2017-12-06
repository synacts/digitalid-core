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

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.conversion.exceptions.ConversionException;
import net.digitalid.utility.rootclass.RootClassWithException;
import net.digitalid.utility.storage.Module;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.dialect.statement.schema.SQLCreateSchemaStatementBuilder;
import net.digitalid.database.interfaces.Database;

/**
 * A core unit is either a host or a client.
 * 
 * @invariant isHost() != isClient() : "This unit is either a host or a client.";
 */
@Immutable
public abstract class CoreUnit extends RootClassWithException<ConversionException> implements Unit {
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    /**
     * Returns whether this unit is a host.
     */
    @Pure
    public abstract boolean isHost();
    
    /**
     * Returns whether this unit is a client.
     */
    @Pure
    public abstract boolean isClient();
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * All modules and tables that have to be created on each unit are added to this module.
     */
    public static final @Nonnull Module MODULE = CoreModuleBuilder.withName("unit").build();
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    @Committing
    protected void initialize() throws ConversionException {
        super.initialize();
        
        Database.instance.get().execute(SQLCreateSchemaStatementBuilder.build(), this); // TODO: Make the Unit a class with a builder once it can be above the generator again and move this line to the initialize method there.
        MODULE.accept(table -> SQL.createTable(table, this));
        Database.commit();
    }
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Validate.that(isHost() != isClient()).orThrow("This unit $ has to be either a host or a client.", this);
    }
    
}
