package net.digitalid.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Database;
import net.digitalid.core.errors.InitializationError;
import net.digitalid.core.identifier.HostIdentifier;

/**
 * This class models a host identity.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public final class HostIdentity extends IdentityClass implements InternalIdentity {
    
    /**
     * Stores the semantic type {@code host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("host@core.digitalid.net").load(Identity.IDENTIFIER);
    
    
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
            throw new InitializationError("The host identity with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Stores the host identity of {@code digitalid.net}.
     */
    public static final @Nonnull HostIdentity DIGITALID = HostIdentity.create(HostIdentifier.DIGITALID);
    
    
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
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws SQLException {
        Mapper.unmap(this);
        throw exception;
    }
    
}
