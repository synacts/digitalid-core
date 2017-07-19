package net.digitalid.core.unit;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClassWithException;
import net.digitalid.utility.storage.Module;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.dialect.statement.schema.SQLCreateSchemaStatementBuilder;
import net.digitalid.database.interfaces.Database;

/**
 * A core unit is either a host or a client.
 * 
 * @invariant isHost() != isClient() : "This unit is either a host or a client.";
 */
@Immutable
public abstract class CoreUnit extends RootClassWithException<ExternalException> implements Unit {
    
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
    protected void initialize() throws ExternalException {
        super.initialize();
        
        Database.instance.get().execute(SQLCreateSchemaStatementBuilder.build(), this);
        MODULE.accept(table -> SQL.createTable(table, this));
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
