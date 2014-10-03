package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.errors.ShouldNeverHappenError;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.interfaces.Immutable;
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
     * Creates a new host identity with the given identifier.
     * 
     * @param identifier the identifier of the new host identity.
     * 
     * @require Database.isMainThread(): "This method may only be called in the main thread.";
     */
    private static @Nonnull HostIdentity create(@Nonnull HostIdentifier identifier) {
        assert Database.isMainThread(): "This method may only be called in the main thread.";
        
        try {
            return Mapper.mapHostIdentity(identifier);
        } catch (@Nonnull SQLException exception) {
            throw new InitializationError("The host identity with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Stores the host identity of {@code virtualid.ch}.
     */
    public static final @Nonnull HostIdentity VIRTUALID = HostIdentity.create(HostIdentifier.VIRTUALID);
    
    
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
    
    @Pure
    @Override
    public boolean hasBeenMerged() {
        return false;
    }
    
}
