package ch.virtualid.identity;

import ch.virtualid.annotation.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.exception.InvalidDeclarationException;
import ch.virtualid.interfaces.BlockableObject;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class represents identifiers and provides useful auxiliary functions.
 * 
 * @see HostIdentifier
 * @see NonHostIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Identifier extends BlockableObject implements Immutable, SQLizable {
    
    /**
     * The pattern that valid identifiers have to match.
     */
    private static final Pattern pattern = Pattern.compile("(?:(?:[a-z0-9]+(?:[._-][a-z0-9]+)*)?@)?[a-z0-9]+(?:[.-][a-z0-9]+)*\\.(?:[a-z][a-z]+)");
    
    /**
     * Returns whether the given string is a valid identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.length() <= 100 && pattern.matcher(string).matches();
    }
    
    /**
     * Returns whether the given string could denote a host identifier.
     * Only checks whether the string contains an '@' without verifying its validity.
     *
     * @param string the string to check.
     * 
     * @return whether the given string could denote a host identifier.
     */
    @Pure
    public static boolean isHost(@Nonnull String string) {
        return !string.contains("@");
    }
    
    /**
     * Returns a new identifier with the given string.
     * 
     * @param string the string of the new identifier.
     * 
     * @return a new identifier with the given string.
     * 
     * @require Identifier.isValid(string) : "The string is a valid identifier.";
     */
    @Pure
    public static @Nonnull Identifier create(@Nonnull String string) {
        assert Identifier.isValid(string) : "The string is a valid identifier.";
        
        if (Identifier.isHost(string)) {
            return new HostIdentifier(string);
        } else {
            return new NonHostIdentifier(string);
        }
    }
    
    /**
     * Returns a new identifier from the given block.
     * Please note that the block is recast to either a host or non-host identifier.
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
        if (Identifier.isHost(string)) {
            return new HostIdentifier(new Block(HostIdentity.IDENTIFIER, block), string);
        } else {
            return new NonHostIdentifier(new Block(NonHostIdentity.IDENTIFIER, block), string);
        }
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
     * @require Identifier.isValid(string) : "The string is a valid identifier.";
     */
    protected Identifier(@Nonnull String string) {
        assert Identifier.isValid(string) : "The string is a valid identifier.";
        
        this.string = string;
    }
    
    /**
     * Creates an identifier with the given block and string.
     * 
     * @param block the block of the identifier.
     * @param string the string of the identifier.
     * 
     * @throws InvalidEncodingException if the given string is not a valid identifier.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    protected Identifier(@Nonnull Block block, @Nonnull String string) throws InvalidEncodingException {
        super(block);
        
        this.string = string;
        if (!Identifier.isValid(string)) throw new InvalidEncodingException("" + this + " is not a valid identifier.");
    }
    
    @Pure
    @Override
    public final @Nonnull Block encode() {
        return new StringWrapper(getType(), string).toBlock();
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
     * Returns the host part of this identifier.
     * 
     * @return the host part of this identifier.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getHostIdentifier();
    
    /**
     * Returns the identity of this identifier.
     * 
     * @return the identity of this identifier.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Pure
    public abstract @Nonnull Identity getIdentity() throws SQLException, FailedIdentityException, InvalidDeclarationException;
    
    /**
     * Returns whether an identity with this identifier exists.
     * Please note that a negative answer can also be caused by network problems (in case the identifier is not yet mapped).
     * 
     * @return whether an identity with this identifier exists.
     */
    @Pure
    public final boolean exists() throws SQLException {
        try {
            Mapper.getIdentity(this);
            return true;
        } catch (@Nonnull FailedIdentityException exception) {
            return false;
        }
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
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "VARCHAR(100) COLLATE " + Database.getConfiguration().BINARY();
    
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
        return create(resultSet.getString(columnIndex));
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
        @Nonnull Identifier other = (Identifier) object;
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
