package ch.virtualid.identity;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.errors.InitializationError;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.interfaces.Immutable;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models a host identity.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class HostIdentity extends IdentityClass implements InternalIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code host@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("host@virtualid.ch").load(Identity.IDENTIFIER);
    
    
    /**
     * Creates a new host identity with the given identifier.
     * 
     * @param identifier the identifier of the new host identity.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     */
    private static @Nonnull HostIdentity create(@Nonnull HostIdentifier identifier) {
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        try {
            return Mapper.mapHostIdentity(identifier);
        } catch (@Nonnull SQLException exception) {
            try { Database.rollback(); } catch (@Nonnull SQLException exc) { throw new InitializationError("Could not rollback.", exc); }
            throw new InitializationError("The host identity with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Stores the host identity of {@code virtualid.ch}.
     */
    public static final @Nonnull HostIdentity VIRTUALID = HostIdentity.create(HostIdentifier.VIRTUALID);
    
    
    /**
     * Stores the address of this host identity.
     */
    private final @Nonnull HostIdentifier address;
    
    /**
     * Creates a new host identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the address of the new host identity.
     */
    HostIdentity(long number, @Nonnull HostIdentifier address) {
        super(number);
        
        this.address = address;
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getAddress() {
        return address;
    }
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
    @Pure
    @Override
    @DoesNotCommit
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws SQLException {
        Mapper.unmap(this);
        throw exception;
    }
    
}
