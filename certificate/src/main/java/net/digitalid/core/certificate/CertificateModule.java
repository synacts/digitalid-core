package net.digitalid.core.certificate;

import java.sql.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;

import net.digitalid.core.attribute.AttributeValue;

import net.digitalid.service.core.dataservice.StateModule;

import net.digitalid.core.entity.NonHostEntity;

import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.host.Host;

import net.digitalid.core.state.Service;

/**
 * This class provides database access to the {@link AttributeValue certificates} of the core service.
 */
@Stateless
public final class CertificateModule implements StateModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final CertificateModule MODULE = new CertificateModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws DatabaseException {
//        try (@Nonnull Statement statement = Database.createStatement()) {
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS certificate (issuer BIGINT NOT NULL, recipient BIGINT NOT NULL, type BIGINT NOT NULL, value LONGBLOB NOT NULL, issuance BIGINT, PRIMARY KEY (issuer, recipient, type), FOREIGN KEY (issuer) REFERENCES general_identity (identity), FOREIGN KEY (recipient) REFERENCES general_identity (identity), FOREIGN KEY (type) REFERENCES general_identity (identity))");
//        }
        
        // TODO: Split into certificate_value and certificate_issuance? The latter is not needed on the client-side!
//        
//        Mapper.addReference("certificate", "recipient");
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Delete the tables of this module.
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.certificates.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.certificates.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, net.digitalid.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code certificates.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("certificates.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws DatabaseException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve all the entries from the database table(s).
        }
        return ListWrapper.encode(MODULE_FORMAT, entries.freeze());
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws DatabaseException, InvalidEncodingException {
        Require.that(block.getType().isBasedOn(getModuleFormat())).orThrow("The block is based on the format of this module.");
        
        final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block entry : entries) {
            // TODO: Add all entries to the database table(s).
        }
    }
    
    
    /**
     * Stores the semantic type {@code entry.certificates.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_ENTRY = SemanticType.map("entry.certificates.state@core.digitalid.net").load(TupleWrapper.XDF_TYPE, net.digitalid.core.identity.SemanticType.UNKNOWN);
    
    /**
     * Stores the semantic type {@code certificates.state@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType STATE_FORMAT = SemanticType.map("certificates.state@core.digitalid.net").load(ListWrapper.XDF_TYPE, STATE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getStateFormat() {
        return STATE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block getState(@Nonnull NonHostEntity entity, @Nonnull ReadOnlyAgentPermissions permissions, @Nonnull Restrictions restrictions, @Nullable Agent agent) throws DatabaseException {
        final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Retrieve the entries of the given entity from the database table(s).
        }
        return ListWrapper.encode(STATE_FORMAT, entries.freeze());
    }
    
    @Override
    @NonCommitting
    public void addState(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, InvalidEncodingException {
        Require.that(block.getType().isBasedOn(getStateFormat())).orThrow("The block is based on the indicated type.");
        
        final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
        for (final @Nonnull Block entry : entries) {
            // TODO: Add the entries of the given entity to the database table(s).
        }
    }
    
    @Override
    @NonCommitting
    public void removeState(@Nonnull NonHostEntity entity) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            // TODO: Remove the entries of the given entity from the database table(s).
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
