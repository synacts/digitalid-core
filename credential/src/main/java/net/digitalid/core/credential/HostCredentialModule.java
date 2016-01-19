package net.digitalid.core.credential;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.validation.state.Stateless;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.table.Site;

import net.digitalid.core.service.CoreService;

import net.digitalid.service.core.auxiliary.Time;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;

import net.digitalid.service.core.cryptography.Exponent;

import net.digitalid.core.entity.EntityImplementation;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityImplementation;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.resolution.Mapper;

import net.digitalid.core.packet.Packet;

import net.digitalid.core.host.Host;

import net.digitalid.core.state.HostModule;
import net.digitalid.core.state.Service;

/**
 * This class provides database access to the {@link Credential credentials} issued by a {@link Host host}.
 */
@Stateless
public final class HostCredentialModule implements HostModule {
    
    /**
     * Stores an instance of this module.
     */
    public static final HostCredentialModule MODULE = new HostCredentialModule();
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return CoreService.SERVICE;
    }
    
    @Override
    @NonCommitting
    public void createTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "credential (time " + Time.FORMAT + " NOT NULL, entity " + EntityImplementation.FORMAT + " NOT NULL, e " + Exponent.FORMAT + " NOT NULL, i " + Exponent.FORMAT + " NOT NULL, v " + Exponent.FORMAT + ", signature " + Block.FORMAT + " NOT NULL, PRIMARY KEY (time), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
            Mapper.addReference(site + "credential", "entity");
            Database.addRegularPurging(site + "credential", Time.TROPICAL_YEAR);
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws DatabaseException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            Database.removeRegularPurging(site + "credential");
            Mapper.removeReference(site + "credential", "entity");
            statement.executeUpdate("DROP TABLE IF EXISTS " + site + "credential");
        }
    }
    
    
    /**
     * Stores the semantic type {@code e.entry.host.credential.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType E = SemanticType.map("e.entry.host.credential.module@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code i.entry.host.credential.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType I = SemanticType.map("i.entry.host.credential.module@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code v.entry.host.credential.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType V = SemanticType.map("v.entry.host.credential.module@core.digitalid.net").load(Exponent.TYPE);
    
    /**
     * Stores the semantic type {@code entry.host.credential.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.host.credential.module@core.digitalid.net").load(TupleWrapper.XDF_TYPE, Time.TYPE, InternalNonHostIdentity.IDENTIFIER, E, I, V, Packet.SIGNATURE);
    
    /**
     * Stores the semantic type {@code host.credential.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("host.credential.module@core.digitalid.net").load(ListWrapper.XDF_TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws DatabaseException {
        final @Nonnull String SQL = "SELECT time, entity, e, i, v, signature FROM " + host + "credential";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
            while (resultSet.next()) {
                final @Nonnull Time time = Time.get(resultSet, 1);
                final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, 2);
                final @Nonnull Block e = Block.getNotNull(E, resultSet, 3);
                final @Nonnull Block i = Block.getNotNull(I, resultSet, 4);
                final @Nullable Block v = Block.get(V, resultSet, 5);
                final @Nonnull Block signature = Block.getNotNull(Packet.SIGNATURE, resultSet, 6);
                entries.add(TupleWrapper.encode(MODULE_ENTRY, time.toBlock(), identity.toBlock(InternalNonHostIdentity.IDENTIFIER), e, i, v, signature));
            }
            return ListWrapper.encode(MODULE_FORMAT, entries.freeze());
        }
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement("INSERT INTO " + host + "credential (time, entity, e, i, v, signature) VALUES (?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = ListWrapper.decodeNonNullableElements(block);
            for (final @Nonnull Block entry : entries) {
                final @Nonnull TupleWrapper tuple = TupleWrapper.decode(entry);
                Time.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(0)).set(preparedStatement, 1);
                IdentityImplementation.create(tuple.getNonNullableElement(1)).castTo(InternalNonHostIdentity.class).set(preparedStatement, 2);
                tuple.getNonNullableElement(2).set(preparedStatement, 3);
                tuple.getNonNullableElement(3).set(preparedStatement, 4);
                Block.set(tuple.getNullableElement(4), preparedStatement, 5);
                tuple.getNonNullableElement(5).set(preparedStatement, 6);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
    
    /**
     * Stores the given parameters in the database.
     * 
     * @param account the account which issued the credential.
     * @param e the certifying exponent of the issued credential.
     * @param i the serial number of the issued credential.
     * @param v the hash of restrictions or the subject's identifier.
     * @param signature the signature of the credential request.
     */
    @NonCommitting
    public static void store(@Nonnull NonHostAccount account, @Nonnull Exponent e, @Nonnull Exponent i, @Nullable Exponent v, @Nonnull SignatureWrapper signature) throws DatabaseException {
        final @Nonnull Site site = account.getSite();
        final @Nonnull String TIME = Database.getConfiguration().GREATEST() + "(COALESCE(MAX(time), 0) + 1, " + Database.getConfiguration().CURRENT_TIME() + ")";
        final @Nonnull String SQL = "INSERT INTO " + site + "credential (time, entity, e, i, v, signature) SELECT " + TIME + ", ?, ?, ?, ?, ? FROM " + site + "credential";
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            account.set(preparedStatement, 1);
            e.set(preparedStatement, 2);
            i.set(preparedStatement, 3);
            Exponent.set(v, preparedStatement, 4);
            signature.toBlock().set(preparedStatement, 5);
            preparedStatement.executeUpdate();
        }
    }
    
    static { CoreService.SERVICE.add(MODULE); }
    
}
