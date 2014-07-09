package ch.virtualid.identity;

import javax.annotation.Nonnull;
import org.junit.Test;

/**
 * Code stub for testing email address verification.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class EmailPersonTest {
    
    @Test
    public void test() {
        final @Nonnull String[] emails = new String[] {"kasparetter@gmail.com", "kaspar.etter@virtualid.ch", "kasparetter@hotmail.com", "kasparetter@gmx.net", "kaspar.etter@entrepreneur-club.org", "kaspar.etter@www.virtualid.ch", "kaspar.etter@wwww.virtualid.ch", "kaspar.etter@virtual-id.ch", "kaspar.etter@domaindoesnotexist.ch"};
        for (@Nonnull String email : emails) {
            System.out.println(email + ": " + (EmailPerson.providerExists(new NonHostIdentifier(email)) ? "yes" : "no"));
        }
    }
    
}
