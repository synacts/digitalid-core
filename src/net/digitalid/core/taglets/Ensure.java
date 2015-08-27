package net.digitalid.core.taglets;

import java.util.Map;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;

/**
 * This class defines a custom block tag for constructor and method post-conditions.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class Ensure extends Taglet {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Registration –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Registers this taglet at the given map.
     * 
     * @param map the map at which this taglet is registered.
     */
    public static void register(@Nonnull Map<String, Taglet> map) {
        Taglet.register(map, new Ensure());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Overrides –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean inConstructor() {
        return true;
    }
    
    @Override
    public boolean inMethod() {
        return true;
    }
    
    @Override
    public @Nonnull String getName() {
        return "ensure";
    }
    
    @Override
    public @Nonnull String getTitle() {
        return "Ensures";
    }
    
}
