package ch.virtualid.taglets;

import java.util.Map;

/**
 * This class defines a custom block tag for constructor and method preconditions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Require extends Taglet {
    
    public static void register(Map<String, Taglet> map) {
        Taglet.register(map, new Require());
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
        return "require";
    }
    
    @Override
    public String getTitle() {
        return "Requires";
    }
    
}
