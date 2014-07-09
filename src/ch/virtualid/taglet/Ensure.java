package ch.virtualid.taglet;

import java.util.Map;

/**
 * This class defines a custom block tag for constructor and method post-conditions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Ensure extends Taglet {
    
    public static void register(Map<String, Taglet> map) {
        Taglet.register(map, new Ensure());
    }
    
    @Override
    public boolean inConstructor() {
        return true;
    }
    
    @Override
    public boolean inMethod() {
        return true;
    }
    
    @Override
    public String getName() {
        return "ensure";
    }
    
    @Override
    public String getTitle() {
        return "Ensures";
    }
    
}
