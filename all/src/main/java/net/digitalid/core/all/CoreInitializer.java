package net.digitalid.core.all;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.unit.Unit;

import net.digitalid.core.account.AccountOpenConverter;
import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.resolution.handlers.IdentityQueryConverter;

/**
 * This class initializes the core classes.
 */
@Utility
public abstract class CoreInitializer {
    
    /**
     * Initializes the method index.
     */
    @PureWithSideEffects
    @Initialize(target = MethodIndex.class)
    public static void initializeMethodIndex() {
        MethodIndex.add(IdentityQueryConverter.INSTANCE);
        MethodIndex.add(AccountOpenConverter.INSTANCE);
    }
    
    /**
     * Initializes the database tables.
     */
    @Committing
    @PureWithSideEffects
    @Initialize(target = SQL.class)
    public static void initializeDatabaseTables() throws DatabaseException {
        SQL.createTable(Attribute.MODULE.getSubjectConverter(), Unit.DEFAULT);
        SQL.createTable(Attribute.VALUE_TABLE.getEntryConverter(), Unit.DEFAULT);
        SQL.createTable(Attribute.VISIBILITY_TABLE.getEntryConverter(), Unit.DEFAULT);
    }
    
}
