package ch.virtualid;

import ch.virtualid.expression.Expression;
import ch.xdf.exceptions.InvalidEncodingException;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.StringWrapper;
import java.sql.SQLException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit testing of the class {@link Expression}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.9
 */
public class ExpressionTest {
    
    /**
     * Test the parsing of several expressions.
     */
    @Test
    public void testParsing() throws InvalidEncodingException, SQLException, Exception {
        String[] strings = new String[] {"", " ( everybody)", "person@test.virtualid.ch + person@test.virtualid.ch - person@test.virtualid.ch", "person@test.virtualid.ch + (person@test.virtualid.ch - person@test.virtualid.ch)", "person@test.virtualid.ch + name@virtualid.ch * person@test.virtualid.ch"};
        for (String string : strings) {
//            System.out.println(string);
            Expression expression = Expression.parse(string);
//            System.out.println(expression);
//            System.out.println();
        }
        
        Block attribute = new SelfcontainedWrapper("name@virtualid.ch", new StringWrapper("Person").toBlock()).getBlock();
        strings = new String[] {"", "name@virtualid.ch", "(name@virtualid.ch=\"person\" ) ", "name@virtualid.ch \\ \"on\"", "name@virtualid.ch ≠ \" ) \""};
        for (String string : strings) {
//            System.out.println(string);
            Expression expression = Expression.parse(string);
//            System.out.println(expression);
//            System.out.println();
            assertTrue(expression.matches(attribute));
        }
    }
    
}