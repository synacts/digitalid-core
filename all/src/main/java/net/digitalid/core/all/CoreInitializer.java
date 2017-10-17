package net.digitalid.core.all;

import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.account.OpenAccount;
import net.digitalid.core.account.OpenAccountConverter;
import net.digitalid.core.authorization.CredentialInternalQueryConverter;
import net.digitalid.core.cache.attributes.AttributesQueryConverter;
import net.digitalid.core.cache.attributes.AttributesReplyConverter;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.handler.reply.instances.EmptyReplyConverter;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
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
        MethodIndex.add(OpenAccountConverter.INSTANCE);
        MethodIndex.add(AttributesQueryConverter.INSTANCE);
        MethodIndex.add(CredentialInternalQueryConverter.INSTANCE);
        
        SemanticType.map(AttributesReplyConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build()); // TODO: Load the right attributes.
        SemanticType.map(EmptyReplyConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build()); // TODO: Load the right attributes.
        OpenAccount.TYPE.isLoaded(); // Maps the type in the main thread.
    }
    
    /**
     * Initializes the database tables.
     */
    @Committing
    @PureWithSideEffects
    @Initialize(target = SQL.class)
    public static void initializeDatabaseTables() throws DatabaseException {
//        SQL.createTable(Attribute.MODULE.getSubjectTable(), Unit.DEFAULT);
//        SQL.createTable(Attribute.VALUE_TABLE, Unit.DEFAULT);
//        SQL.createTable(Attribute.VISIBILITY_TABLE, Unit.DEFAULT);
    }
    
}
