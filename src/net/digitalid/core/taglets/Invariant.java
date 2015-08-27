package net.digitalid.core.taglets;

import java.util.Map;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Stateless;

/**
 * This class defines a custom block tag for class (and field) invariants.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Stateless
public final class Invariant extends Taglet {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Registration –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Registers this taglet at the given map.
     * 
     * @param map the map at which this taglet is registered.
     */
    public static void register(@Nonnull Map<String, Taglet> map) {
        Taglet.register(map, new Invariant());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Overrides –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public boolean inField() {
        return true;
    }
    
    @Pure
    @Override
    public boolean inType() {
        return true;
    }
    
    @Pure
    @Override
    public @Nonnull String getName() {
        return "invariant";
    }
    
    @Pure
    @Override
    public @Nonnull String getTitle() {
        return "Invariant";
    }
    
}
