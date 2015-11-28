package net.digitalid.service.core.expression;

import java.sql.SQLException;
import net.digitalid.database.core.annotations.Committing;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import org.junit.Test;

/**
 * Unit testing of the class {@link Expression}.
 */
public final class ExpressionTest {
    
    /**
     * Test the parsing of several expressions.
     */
    @Test
    @Committing
    public void testParsing() throws InvalidEncodingException, InternalException, SQLException, Exception {
//        String[] strings = new String[] {"", " ( everybody)", "person@test.digitalid.net + person@test.digitalid.net - person@test.digitalid.net", "person@test.digitalid.net + (person@test.digitalid.net - person@test.digitalid.net)", "person@test.digitalid.net + name@core.digitalid.net * person@test.digitalid.net"};
//        for (final @Nonnull String string : strings) {
////            System.out.println(string);
//            Expression expression = Expression.parse(string);
////            System.out.println(expression);
////            System.out.println();
//        }
//        
//        Block attribute = SelfcontainedWrapper.encodeNonNullable("name@core.digitalid.net", StringWrapper.encodeNonNullable("Person")).getBlock();
//        strings = new String[] {"", "name@core.digitalid.net", "(name@core.digitalid.net=\"person\" ) ", "name@core.digitalid.net \\ \"on\"", "name@core.digitalid.net ≠ \" ) \""};
//        for (final @Nonnull String string : strings) {
////            System.out.println(string);
//            Expression expression = Expression.parse(string);
////            System.out.println(expression);
////            System.out.println();
//            assertTrue(expression.matches(attribute));
//        }
    }
    
}