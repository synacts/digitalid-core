package net.digitalid.core.interfaces;

import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * Classes that support the encoding of their objects as a {@link Block} can indicate this by implementing this interface.
 * Classes that implement this interface should also provide a constructor that takes only a {@link Block} as an argument.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public interface Blockable {
    
    /**
     * Returns the semantic type that corresponds to this class.
     * 
     * @return the semantic type that corresponds to this class.
     * 
     * @ensure return.isLoaded() : "The type declaration is loaded.";
     */
    @Pure
    public @Nonnull SemanticType getType();
    
    /**
     * Returns this object encoded as a block.
     * 
     * @return this object encoded as a block.
     * 
     * @ensure return.getType().equals(getType()) : "The returned block has the indicated type.";
     */
    @Pure
    public @Nonnull Block toBlock();
    
    
    // TODO
    public static class Factory {
        
    }
    
}
