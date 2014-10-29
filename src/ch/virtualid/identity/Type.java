package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.NonHostIdentifier;
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
 * @version 2.0
 */
public abstract class Type extends NonHostIdentity implements InternalNonHostIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code type@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.TYPE_IDENTIFIER;
    
    
    /**
     * Stores whether the type declaration has already been loaded.
     * (Lazy loading is necessary for recursive type declarations.)
     */
    private boolean loaded = false;
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    Type(long number, @Nonnull NonHostIdentifier address) {
        super(number, address);
    }
    
    
    @Override
    public final boolean hasBeenMerged() {
        return false;
    }
    
    
    @Pure
    @Override
    public final @Nonnull NonHostIdentifier getInternalAddress() {
        assert address instanceof NonHostIdentifier : "The address is a non-host identifier.";
        return (NonHostIdentifier) address;
    }
    
    
    /**
     * Returns whether the type declaration has already been loaded.
     * 
     * @return whether the type declaration has already been loaded.
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
     * @require !isLoaded() : "The type declaration may not yet have been loaded.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    abstract void load() throws SQLException, IOException, PacketException, ExternalException;
    
    /**
     * Ensures that the type declaration is loaded.
     * 
     * @ensure isLoaded() : "The type declaration is loaded.";
     */
    public final void ensureLoaded() throws SQLException, IOException, PacketException, ExternalException {
        if (!loaded) load();
    }
    
}
