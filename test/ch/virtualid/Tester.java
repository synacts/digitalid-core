package ch.virtualid;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import javax.annotation.Nonnull;
import org.junit.Test;

/**
 * Code stub for testing arbitrary code snippets.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class Tester {
    
    @Test
    public void test() {
        final @Nonnull BlockingDeque<String> strings = new LinkedBlockingDeque<String>();
        strings.add("Hello");
        strings.add("World");
        
        for (final @Nonnull String string : strings) {
            System.out.println(string);
        }
    }
    
}
