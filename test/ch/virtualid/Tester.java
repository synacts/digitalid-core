package ch.virtualid;

import ch.virtualid.agent.Agent;
import ch.virtualid.concepts.Attribute;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.util.FreezableLinkedHashMap;
import ch.virtualid.util.FreezableMap;
import org.junit.Test;

/**
 * Code stub for testing arbitrary code snippets.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class Tester extends DatabaseSetup {
    
    @Test
    public void test() {
        final FreezableMap<SemanticType, String> map = new FreezableLinkedHashMap<SemanticType, String>();
        map.put(Attribute.TYPE, "Attribute");
        map.put(Agent.TYPE, "Agent");
        System.out.println(map.size());
    }
    
}
