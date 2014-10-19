package ch.xdf;

import ch.virtualid.setup.DatabaseSetup;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import javax.annotation.Nonnull;
import org.junit.Test;

/**
 * Unit testing of the class {@link BooleanWrapper}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class EmptyWrapperTest extends DatabaseSetup {
    
    @Test
    public void testWrapping() throws InvalidEncodingException {
        final @Nonnull SemanticType TYPE = SemanticType.create("empty@syntacts.com").load(EmptyWrapper.TYPE);
        new EmptyWrapper(new EmptyWrapper(TYPE).toBlock());
    }
    
}
