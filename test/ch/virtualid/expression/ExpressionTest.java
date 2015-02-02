package ch.virtualid.expression;

import ch.virtualid.annotations.EndsCommitted;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import java.sql.SQLException;
import org.junit.Test;

/**
 * Unit testing of the class {@link Expression}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.1
 */
public final class ExpressionTest {
    
    /**
     * Test the parsing of several expressions.
     */
    @Test
    @EndsCommitted
    public void testParsing() throws InvalidEncodingException, SQLException, Exception {
//        String[] strings = new String[] {"", " ( everybody)", "person@test.virtualid.ch + person@test.virtualid.ch - person@test.virtualid.ch", "person@test.virtualid.ch + (person@test.virtualid.ch - person@test.virtualid.ch)", "person@test.virtualid.ch + name@virtualid.ch * person@test.virtualid.ch"};
//        for (String string : strings) {
////            System.out.println(string);
//            Expression expression = Expression.parse(string);
////            System.out.println(expression);
////            System.out.println();
//        }
//        
//        Block attribute = new SelfcontainedWrapper("name@virtualid.ch", new StringWrapper("Person").toBlock()).getBlock();
//        strings = new String[] {"", "name@virtualid.ch", "(name@virtualid.ch=\"person\" ) ", "name@virtualid.ch \\ \"on\"", "name@virtualid.ch ≠ \" ) \""};
//        for (String string : strings) {
////            System.out.println(string);
//            Expression expression = Expression.parse(string);
////            System.out.println(expression);
////            System.out.println();
//            assertTrue(expression.matches(attribute));
//        }
    }
    
}