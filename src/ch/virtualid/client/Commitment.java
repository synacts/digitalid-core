package ch.virtualid.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.cryptography.Element;
import ch.virtualid.cryptography.Exponent;
import ch.virtualid.cryptography.PublicKey;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.IdentityClass;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.virtualid.server.Server;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models the commitment of a client.
 * 
 * @see SecretCommitment
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public class Commitment implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code host.commitment.client@virtualid.ch}.
     */
    private static final @Nonnull SemanticType HOST = SemanticType.create("host.commitment.client@virtualid.ch").load(HostIdentity.IDENTIFIER);
    
    /**
     * Stores the semantic type {@code time.commitment.client@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TIME = SemanticType.create("time.commitment.client@virtualid.ch").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code value.commitment.client@virtualid.ch}.
     */
    private static final @Nonnull SemanticType VALUE = SemanticType.create("value.commitment.client@virtualid.ch").load(Element.TYPE);
    
    /**
     * Stores the semantic type {@code commitment.client@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("commitment.client@virtualid.ch").load(TupleWrapper.TYPE, HOST, TIME, VALUE);
    
    
    /**
     * Stores the host at which this commitment was made.
     */
    private final @Nonnull HostIdentity host;
    
    /**
     * Stores the time at which this commitment was made.
     */
    private final @Nonnull Time time;
    
    /**
     * Stores the value of this commitment.
     */
    private final @Nonnull Element value;
    
    /**
     * Stores the public key of this commitment.
     */
    private final @Nonnull PublicKey publicKey;
    
    /**
     * Creates a new commitment with the given host, time and value.
     * 
     * @param host the host at which this commitment was made.
     * @param time the time at which this commitment was made.
     * @param value the value of this commitment.
     */
    public Commitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull BigInteger value) throws SQLException, IOException, PacketException, ExternalException {
        this.host = host;
        this.time = time;
        this.publicKey = Cache.getPublicKeyChain(host).getKey(time);
        this.value = publicKey.getCompositeGroup().getElement(value);
    }
    
    /**
     * Creates a new commitment with the given host, time, value and public key.
     * 
     * @param host the host at which this commitment was made.
     * @param time the time at which this commitment was made.
     * @param value the value of this commitment.
     * @param publicKey the public key of this commitment.
     */
    Commitment(@Nonnull HostIdentity host, @Nonnull Time time, @Nonnull Element value, @Nonnull PublicKey publicKey) {
        this.host = host;
        this.time = time;
        this.value = value;
        this.publicKey = publicKey;
    }
    
    /**
     * Creates a new commitment from the given block.
     * 
     * @param block the block containing the commitment.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Commitment(@Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(3);
        final @Nonnull HostIdentifier identifier = IdentifierClass.create(elements.getNotNull(0)).toHostIdentifier();
        this.host = identifier.getIdentity();
        this.time = new Time(elements.getNotNull(1));
        this.publicKey = (Server.hasHost(identifier) ? Server.getHost(identifier).getPublicKeyChain() : Cache.getPublicKeyChain(host)).getKey(time);
        this.value = publicKey.getCompositeGroup().getElement(new IntegerWrapper(elements.getNotNull(2)).getValue());
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
        elements.set(0, host.toBlock(HOST));
        elements.set(1, time.toBlock().setType(TIME));
        elements.set(2, value.toBlock().setType(VALUE));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the host at which this commitment was made.
     * 
     * @return the host at which this commitment was made.
     */
    @Pure
    public final @Nonnull HostIdentity getHost() {
        return host;
    }
    
    /**
     * Returns the time at which this commitment was made.
     * 
     * @return the time at which this commitment was made.
     */
    @Pure
    public final @Nonnull Time getTime() {
        return time;
    }
    
    /**
     * Returns the value of this commitment.
     * 
     * @return the value of this commitment.
     */
    @Pure
    public final @Nonnull Element getValue() {
        return value;
    }
    
    /**
     * Returns the public key of this commitment.
     * 
     * @return the public key of this commitment.
     */
    @Pure
    public final @Nonnull PublicKey getPublicKey() {
        return publicKey;
    }
    
    
    /**
     * Adds the given secret to this commitment.
     * 
     * @param secret the secret to be added.
     * 
     * @return the new secret commitment.
     */
    @Pure
    public final @Nonnull SecretCommitment addSecret(@Nonnull Exponent secret) throws PacketException {
        return new SecretCommitment(host, time, value, publicKey, secret);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Commitment)) return false;
        final @Nonnull Commitment other = (Commitment) object;
        return host.equals(other.host) && time.equals(other.time) && value.equals(other.value);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 97 * hash + host.hashCode();
        hash = 97 * hash + time.hashCode();
        hash = 97 * hash + value.hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "[Host: " + host.getAddress() + ", Time: " + time.asDate() + "]";
    }
    
    
    /**
     * Stores the columns used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "host " + Mapper.FORMAT + " NOT NULL, time " + Time.FORMAT + " NOT NULL, value " + Database.getConfiguration().BLOB() + " NOT NULL";
    
    /**
     * Stores the columns used to retrieve instances of this class from the database.
     */
    public static final @Nonnull String COLUMNS = "host, time, value";
    
    /**
     * Stores the condition used to retrieve instances of this class from the database.
     */
    public static final @Nonnull String CONDITION = "host = ? AND time = ? AND value = ?";
    
    /**
     * Stores the string used to update instances of this class in the database.
     */
    public static final @Nonnull String UPDATE = "host = ?, time = ?, value = ?";
    
    /**
     * Returns the foreign key constraints used by instances of this class.
     * 
     * @param site the site at which the foreign key constraint is declared.
     * 
     * @return the foreign key constraints used by instances of this class.
     */
    public static @Nonnull String getForeignKeys(@Nonnull Site site) {
        return "FOREIGN KEY (host) " + site.getEntityReference();
    }
    
    /**
     * Returns the given columns of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param startIndex the start index of the columns containing the data.
     * 
     * @return the given columns of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Commitment get(@Nonnull ResultSet resultSet, int startIndex) throws SQLException {
        try {
            final @Nonnull HostIdentity host = IdentityClass.getNotNull(resultSet, startIndex + 0).toHostIdentity();
            final @Nonnull Time time = Time.get(resultSet, startIndex + 1);
            final @Nonnull BigInteger value = new IntegerWrapper(Block.get(Element.TYPE, resultSet, startIndex + 2)).getValue();
            return new Commitment(host, time, value);
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("A problem occurred while retrieving a commitment.", exception);
        }
     }
    
    /**
     * Sets the parameters at the given start index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameters are to be set.
     * @param startIndex the start index of the parameters to set.
     */
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int startIndex) throws SQLException {
        host.set(preparedStatement, startIndex + 0);
        time.set(preparedStatement, startIndex + 1);
        value.toBlock().set(preparedStatement, startIndex + 2);
    }
    
}
