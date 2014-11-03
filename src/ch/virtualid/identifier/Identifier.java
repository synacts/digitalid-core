package ch.virtualid.identifier;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class represents identifiers.
 * 
 * @see InternalIdentifier
 * @see ExternalIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Identifier implements Immutable, Blockable, SQLizable {
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return string.length() < 64;
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.contains(":") ? ExternalIdentifier.isValid(string) : InternalIdentifier.isValid(string);
    }
    
    /**
     * Returns a new identifier with the given string.
     * 
     * @param string the string of the new identifier.
     * 
     * @return a new identifier with the given string.
     * 
     * @require isValid(string) : "The string is a valid identifier.";
     */
    @Pure
    public static @Nonnull Identifier create(@Nonnull String string) {
        assert isValid(string) : "The string is a valid identifier.";
        
        return string.contains(":") ? ExternalIdentifier.create(string) : InternalIdentifier.create(string);
    }
    
    /**
     * Returns a new identifier from the given block.
     * 
     * @param block the block containing the identifier.
     * 
     * @return a new identifier from the given block.
     * 
     * @require block.getType().isBasedOn(Identity.IDENTIFIER) : "The block is based on the identifier type.";
     */
    @Pure
    public static @Nonnull Identifier create(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(Identity.IDENTIFIER) : "The block is based on the identifier type.";
        
        final @Nonnull String string = new StringWrapper(block).getString();
        if (!isValid(string)) throw new InvalidEncodingException("'" + string + "' is not a valid identifier.");
        return create(string);
    }
    
    
    /**
     * Stores the string of this identifier.
     * 
     * @invariant Identifier.isValid(string) : "The string is a valid identifier.";
     */
    private final @Nonnull String string;
    
    /**
     * Creates an identifier with the given string.
     * 
     * @param string the string of the identifier.
     * 
     * @require isValid(string) : "The string is a valid identifier.";
     */
    Identifier(@Nonnull String string) {
        assert isValid(string) : "The string is a valid identifier.";
        
        this.string = string;
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return Identity.IDENTIFIER;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return new StringWrapper(Identity.IDENTIFIER, string).toBlock();
    }
    
    
    /**
     * Returns the string of this identifier.
     * 
     * @return the string of this identifier.
     * 
     * @ensure isValid(string) : "The returned string is a valid identifier.";
     */
    @Pure
    public final @Nonnull String getString() {
        return string;
    }
    
    
    /**
     * Returns the identity of this identifier.
     * 
     * @return the identity of this identifier.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Pure
    public abstract @Nonnull Identity getIdentity() throws SQLException, IOException, PacketException, ExternalException;
    
    
    /**
     * Returns this identifier as an {@link InternalIdentifier}.
     * 
     * @return this identifier as an {@link InternalIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link InternalIdentifier}.
     */
    @Pure
    public @Nonnull InternalIdentifier toInternalIdentifier() throws InvalidEncodingException {
        if (this instanceof InternalIdentifier) return (InternalIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalIdentifier.");
    }
    
    /**
     * Returns this identifier as a {@link HostIdentifier}.
     * 
     * @return this identifier as a {@link HostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link HostIdentifier}.
     */
    @Pure
    public @Nonnull HostIdentifier toHostIdentifier() throws InvalidEncodingException {
        if (this instanceof HostIdentifier) return (HostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to HostIdentifier.");
    }
    
    /**
     * Returns this identifier as a {@link NonHostIdentifier}.
     * 
     * @return this identifier as a {@link NonHostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link NonHostIdentifier}.
     */
    @Pure
    public @Nonnull NonHostIdentifier toNonHostIdentifier() throws InvalidEncodingException {
        if (this instanceof NonHostIdentifier) return (NonHostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostIdentifier.");
    }
    
    
    /**
     * Returns this identifier as an {@link ExternalIdentifier}.
     * 
     * @return this identifier as an {@link ExternalIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link ExternalIdentifier}.
     */
    @Pure
    public @Nonnull ExternalIdentifier toExternalIdentifier() throws InvalidEncodingException {
        if (this instanceof ExternalIdentifier) return (ExternalIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to ExternalIdentifier.");
    }
    
    /**
     * Returns this identifier as an {@link EmailIdentifier}.
     * 
     * @return this identifier as an {@link EmailIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link EmailIdentifier}.
     */
    @Pure
    public @Nonnull EmailIdentifier toEmailIdentifier() throws InvalidEncodingException {
        if (this instanceof EmailIdentifier) return (EmailIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to EmailIdentifier.");
    }
    
    /**
     * Returns this identifier as a {@link MobileIdentifier}.
     * 
     * @return this identifier as a {@link MobileIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link MobileIdentifier}.
     */
    @Pure
    public @Nonnull MobileIdentifier toMobileIdentifier() throws InvalidEncodingException {
        if (this instanceof MobileIdentifier) return (MobileIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to MobileIdentifier.");
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "VARCHAR(63) COLLATE " + Database.getConfiguration().BINARY();
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Identifier get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull String string = resultSet.getString(columnIndex);
        if (!isValid(string)) throw new SQLException("'" + string + "' is not a valid identifier.");
        return create(string);
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setString(parameterIndex, string);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Identifier)) return false;
        final @Nonnull Identifier other = (Identifier) object;
        return string.equals(other.string);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return string.hashCode();
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return "'" + string + "'";
    }
    
}
