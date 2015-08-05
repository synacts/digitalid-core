package net.digitalid.core.credential;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.cryptography.Exponent;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.EntityClass;
import net.digitalid.core.entity.NonHostAccount;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.host.Host;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.module.HostModule;
import net.digitalid.core.packet.Packet;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.service.Service;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.ListWrapper;
import net.digitalid.core.wrappers.SignatureWrapper;
import net.digitalid.core.wrappers.TupleWrapper;

/**
 * This class provides database access to the {@link Credential credentials} issued by a {@link Host host}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
    public void createTables(@Nonnull Site site) throws SQLException {
        try (@Nonnull Statement statement = Database.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + site + "credential (time " + Time.FORMAT + " NOT NULL, entity " + EntityClass.FORMAT + " NOT NULL, e " + Exponent.FORMAT + " NOT NULL, i " + Exponent.FORMAT + " NOT NULL, v " + Exponent.FORMAT + ", signature " + Block.FORMAT + " NOT NULL, PRIMARY KEY (time), FOREIGN KEY (entity) " + site.getEntityReference() + ")");
            Mapper.addReference(site + "credential", "entity");
            Database.addRegularPurging(site + "credential", Time.TROPICAL_YEAR);
        }
    }
    
    @Override
    @NonCommitting
    public void deleteTables(@Nonnull Site site) throws SQLException {
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
    private static final @Nonnull SemanticType MODULE_ENTRY = SemanticType.map("entry.host.credential.module@core.digitalid.net").load(TupleWrapper.TYPE, Time.TYPE, InternalNonHostIdentity.IDENTIFIER, E, I, V, Packet.SIGNATURE);
    
    /**
     * Stores the semantic type {@code host.credential.module@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULE_FORMAT = SemanticType.map("host.credential.module@core.digitalid.net").load(ListWrapper.TYPE, MODULE_ENTRY);
    
    @Pure
    @Override
    public @Nonnull SemanticType getModuleFormat() {
        return MODULE_FORMAT;
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull Block exportModule(@Nonnull Host host) throws SQLException {
        final @Nonnull String SQL = "SELECT time, entity, e, i, v, signature FROM " + host + "credential";
        try (@Nonnull Statement statement = Database.createStatement(); @Nonnull ResultSet resultSet = statement.executeQuery(SQL)) {
            final @Nonnull FreezableList<Block> entries = new FreezableLinkedList<>();
            while (resultSet.next()) {
                final @Nonnull Time time = Time.get(resultSet, 1);
                final @Nonnull Identity identity = IdentityClass.getNotNull(resultSet, 2);
                final @Nonnull Block e = Block.getNotNull(E, resultSet, 3);
                final @Nonnull Block i = Block.getNotNull(I, resultSet, 4);
                final @Nullable Block v = Block.get(V, resultSet, 5);
                final @Nonnull Block signature = Block.getNotNull(Packet.SIGNATURE, resultSet, 6);
                entries.add(new TupleWrapper(MODULE_ENTRY, time.toBlock(), identity.toBlock(InternalNonHostIdentity.IDENTIFIER), e, i, v, signature).toBlock());
            }
            return new ListWrapper(MODULE_FORMAT, entries.freeze()).toBlock();
        }
    }
    
    @Override
    @NonCommitting
    public void importModule(@Nonnull Host host, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(getModuleFormat()) : "The block is based on the format of this module.";
        
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement("INSERT INTO " + host + "credential (time, entity, e, i, v, signature) VALUES (?, ?, ?, ?, ?, ?)")) {
            final @Nonnull ReadOnlyList<Block> entries = new ListWrapper(block).getElementsNotNull();
            for (final @Nonnull Block entry : entries) {
                final @Nonnull TupleWrapper tuple = new TupleWrapper(entry);
                new Time(tuple.getNonNullableElement(0)).set(preparedStatement, 1);
                IdentityClass.create(tuple.getNonNullableElement(1)).toInternalNonHostIdentity().set(preparedStatement, 2);
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
    public static void store(@Nonnull NonHostAccount account, @Nonnull Exponent e, @Nonnull Exponent i, @Nullable Exponent v, @Nonnull SignatureWrapper signature) throws SQLException {
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
