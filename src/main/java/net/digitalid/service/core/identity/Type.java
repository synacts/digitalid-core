package net.digitalid.service.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models a type.
 * 
 * @see SyntacticType
 * @see SemanticType
 */
@Immutable
public abstract class Type extends NonHostIdentityImplementation implements InternalNonHostIdentity {
    
    /**
     * Stores the semantic type {@code type@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.TYPE_IDENTIFIER;
    
    
    /**
     * Stores the presumable address of this type.
     * The address is updated when the type is relocated.
     */
    private @Nonnull InternalNonHostIdentifier address;
    
    /**
     * Creates a new type with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this type.
     */
    Type(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number);
        
        this.address = address;
    }
    
    @Pure
    @Override
    public final @Nonnull InternalNonHostIdentifier getAddress() {
        return address;
    }
    
    /**
     * Sets the address of this type.
     * 
     * @param address the new address of this type.
     */
    final void setAddress(@Nonnull InternalNonHostIdentifier address) {
        this.address = address;
    }
    
    @Pure
    @Override
    @NonCommitting
    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws AbortException {
        Mapper.unmap(this);
        throw exception;
    }
    
    
    /**
     * Stores whether the type declaration is loaded.
     * (Lazy loading is necessary for recursive type declarations.)
     */
    private boolean loaded = false;
    
    /**
     * Returns whether the type declaration is loaded.
     * 
     * @return whether the type declaration is loaded.
     */
    public final boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Sets the type declaration to already being loaded.
     * 
     * @ensure isLoaded() : "The type declaration is loaded.";
     */
    final void setLoaded() {
        loaded = true;
    }
    
    /**
     * Loads the type declaration from the cache or the network.
     * 
     * @require isNotLoaded() : "The type declaration is not loaded.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    @NonCommitting
    abstract void load() throws AbortException, PacketException, ExternalException, NetworkException;
    
    /**
     * Ensures that the type declaration is loaded.
     * 
     * @ensure isLoaded() : "The type declaration is loaded.";
     */
    @NonCommitting
    public final void ensureLoaded() throws AbortException, PacketException, ExternalException, NetworkException {
        if (!loaded) load();
    }
    
}
