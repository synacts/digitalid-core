package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class represents non-host identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class NonHostIdentifier extends Identifier implements Immutable {
    
    /**
     * Verifies that the given string is a valid non-host identifier and throws an {@link InvalidEncodingException} otherwise.
     * 
     * @param string the string to verify.
     * 
     * @ensure Identifier.isValid(string) : "The string is a valid identifier.";
     * @ensure !Identifier.isHost(string) : "The string does not denote a host.";
     */
    public static void verify(@Nonnull String string) throws InvalidEncodingException {
        if (!Identifier.isValid(string) || Identifier.isHost(string)) throw new InvalidEncodingException("'" + string + "' is not a valid non-host identifier.");
    }
    
    /**
     * Creates a non-host identifier with the given string.
     * 
     * @param string the string of the non-host identifier.
     * 
     * @require Identifier.isValid(string) : "The string is a valid identifier.";
     * @require !Identifier.isHost(string) : "The string does not denote a host.";
     */
    public NonHostIdentifier(@Nonnull String string) {
        super(string);
        
        assert !Identifier.isHost(string) : "The string does not denote a host.";
    }
    
    /**
     * Creates a non-host identifier from the given block.
     * 
     * @param block the block containing the non-host identifier.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    public NonHostIdentifier(@Nonnull Block block) throws InvalidEncodingException {
        this(block, new StringWrapper(block).getString());
    }
    
    /**
     * Creates a non-host identifier from the given block and string.
     * 
     * @param block the block containing the non-host identifier.
     * @param string the string of the non-host identifier.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    NonHostIdentifier(@Nonnull Block block, @Nonnull String string) throws InvalidEncodingException {
        super(block, string);
        
        if (Identifier.isHost(string)) throw new InvalidEncodingException("" + this + " is not a non-host identifier.");
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return NonHostIdentity.IDENTIFIER;
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return new HostIdentifier(getString().substring(getString().indexOf("@") + 1));
    }
    
    @Pure
    @Override
    public @Nonnull NonHostIdentity getIdentity() throws SQLException, FailedIdentityException, InvalidDeclarationException {
        final @Nonnull Identity identity = Mapper.getIdentity(this);
        if (identity instanceof Type) ((Type) identity).ensureLoaded();
        if (identity instanceof NonHostIdentity) return (NonHostIdentity) identity;
        throw new ShouldNeverHappenError("Could not cast the identity of " + this + " to a non-host identity.");
    }
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull NonHostIdentifier get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return new NonHostIdentifier(resultSet.getString(columnIndex));
    }
    
}
