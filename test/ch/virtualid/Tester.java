package ch.virtualid;

import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.junit.Test;

/**
 * Code stub for testing arbitrary code snippets.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public class Tester {
    
    /**
     * The pattern that valid client names have to match.
     */
    private static final @Nonnull Pattern pattern = Pattern.compile("[a-z][a-z0-9_$]+", Pattern.CASE_INSENSITIVE);
    
    void test(int a) throws ExternalException {
        if (a < 0) throw new InvalidEncodingException("The parameter is invalid.");
        else throw new InvalidSignatureException("Invalid signature.");
    }
    
    @Test
    public void test() {
        System.out.println(pattern.matcher("awe0932").matches());
        System.out.println(pattern.matcher("a_e0$32").matches());
        
//        try {
//            test(-3);
//        } catch (ExternalException ex) {
//            System.out.println(ex);
//        }
    }
    
}
