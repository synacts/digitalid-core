package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.exception.InitializationError;
import ch.virtualid.exception.ShouldNeverHappenError;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models the host virtual identities.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class HostIdentity extends Identity implements Immutable {
    
    /**
     * Stores the semantic type {@code host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("host@virtualid.ch").load(Identity.IDENTIFIER);
    
    /**
     * Stores the host {@code virtualid.ch}.
     */
    public static final @Nonnull HostIdentity VIRTUALID = HostIdentity.create("virtualid.ch");
    
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    HostIdentity(long number, @Nonnull HostIdentifier address) {
        super(number, address);
    }
    
    /**
     * Creates a new host identity with the given identifier.
     * 
     * @param identifier the identifier of the new host identity.
     * 
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     * @require Identifier.isValid(identifier) : "The string is a valid identifier.";
     * @require Identifier.isHost(identifier) : "The string has to denote a host identifier.";
     */
    public static @Nonnull HostIdentity create(@Nonnull String identifier) {
        assert Database.isMainThread(): "This method may only be called in the main thread.";
        
        try {
            return Mapper.mapHostIdentity(new HostIdentifier(identifier));
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The host identity with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    
    /**
     * Returns the address of this host identity.
     * 
     * @return the address of this host identity.
     */
    @Pure
    public @Nonnull HostIdentifier getHostAddress() {
        try {
            return getAddress().toHostIdentifier();
        } catch (InvalidEncodingException exception) {
            throw new ShouldNeverHappenError("Could not cast the identifier " + getAddress() + " to a host identifier.", exception);
        }
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
    @Override
    public boolean hasBeenMerged() {
        return false;
    }
    
}
