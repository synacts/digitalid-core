package ch.virtualid.taglet;

import java.util.Map;

/**
 * This class defines a custom block tag for class (and field) invariants.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Invariant extends Taglet {
    
    public static void register(Map<String, Taglet> map) {
        Taglet.register(map, new Invariant());
    }
    
    @Override
    public boolean inField() {
        return true;
    }
    
    @Override
    public boolean inType() {
        return true;
    }
    
    @Override
    public String getName() {
        return "invariant";
    }
    
    @Override
    public String getTitle() {
        return "Invariant";
    }
    
}
