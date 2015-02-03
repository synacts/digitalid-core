package ch.virtualid.identity;

import ch.virtualid.annotations.NonCommitting;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models a type.
 * 
 * @see SyntacticType
 * @see SemanticType
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Type extends NonHostIdentityClass implements InternalNonHostIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code type@virtualid.ch}.
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
    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws SQLException {
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
     * Returns whether the type declaration is not loaded.
     * 
     * @return whether the type declaration is not loaded.
     */
    public final boolean isNotLoaded() {
        return !loaded;
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
    abstract void load() throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Ensures that the type declaration is loaded.
     * 
     * @ensure isLoaded() : "The type declaration is loaded.";
     */
    @NonCommitting
    public final void ensureLoaded() throws SQLException, IOException, PacketException, ExternalException {
        if (!loaded) load();
    }
    
}
