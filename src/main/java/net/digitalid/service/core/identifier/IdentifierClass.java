package net.digitalid.service.core.identifier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.Mapper;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.StringWrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models identifiers.
 * 
 * @see InternalIdentifier
 * @see ExternalIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class IdentifierClass implements Identifier {
    
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
    IdentifierClass(@Nonnull String string) {
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
    
    
    @Pure
    @Override
    public final @Nonnull String getString() {
        return string;
    }
    
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public final boolean isMapped() throws SQLException {
        return Mapper.isMapped(this);
    }
    
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier toNonHostIdentifier() throws InvalidEncodingException {
        if (this instanceof NonHostIdentifier) return (NonHostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to NonHostIdentifier.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull InternalIdentifier toInternalIdentifier() throws InvalidEncodingException {
        if (this instanceof InternalIdentifier) return (InternalIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull HostIdentifier toHostIdentifier() throws InvalidEncodingException {
        if (this instanceof HostIdentifier) return (HostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to HostIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentifier toInternalNonHostIdentifier() throws InvalidEncodingException {
        if (this instanceof InternalNonHostIdentifier) return (InternalNonHostIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to InternalNonHostIdentifier.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull ExternalIdentifier toExternalIdentifier() throws InvalidEncodingException {
        if (this instanceof ExternalIdentifier) return (ExternalIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to ExternalIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull EmailIdentifier toEmailIdentifier() throws InvalidEncodingException {
        if (this instanceof EmailIdentifier) return (EmailIdentifier) this;
        throw new InvalidEncodingException("" + this + " is a " + this.getClass().getSimpleName() + " and cannot be cast to EmailIdentifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull MobileIdentifier toMobileIdentifier() throws InvalidEncodingException {
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
    @NonCommitting
    public static @Nonnull Identifier get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull String string = resultSet.getString(columnIndex);
        if (!isValid(string)) throw new SQLException("'" + string + "' is not a valid identifier.");
        return create(string);
    }
    
    @Override
    @NonCommitting
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setString(parameterIndex, string);
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given identifier.
     * 
     * @param identifier the identifier to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public static void set(@Nullable Identifier identifier, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        if (identifier == null) preparedStatement.setNull(parameterIndex, Types.VARCHAR);
        else identifier.set(preparedStatement, parameterIndex);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof IdentifierClass)) return false;
        final @Nonnull IdentifierClass other = (IdentifierClass) object;
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
//        return "\"" + string + "\""; // TODO: Can PostgreSQL be set up to work with real quotes?
    }
    
}
