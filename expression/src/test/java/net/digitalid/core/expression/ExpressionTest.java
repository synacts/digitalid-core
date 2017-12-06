/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// TODO

//package net.digitalid.core.expression;
//
//import java.sql.SQLException;
//
//import net.digitalid.database.annotations.transaction.Committing;
//
//import org.junit.Test;
//
//public final class ExpressionTest {
//    
//    /**
//     * Test the parsing of several expressions.
//     */
//    @Test
//    @Committing
//    public void testParsing() throws InvalidEncodingException, InternalException, SQLException, Exception {
////        String[] strings = new String[] {"", " ( everybody)", "person@test.digitalid.net + person@test.digitalid.net - person@test.digitalid.net", "person@test.digitalid.net + (person@test.digitalid.net - person@test.digitalid.net)", "person@test.digitalid.net + name@core.digitalid.net * person@test.digitalid.net"};
////        for (final @Nonnull String string : strings) {
//////            System.out.println(string);
////            Expression expression = Expression.parse(string);
//////            System.out.println(expression);
//////            System.out.println();
////        }
////        
////        Block attribute = SelfcontainedWrapper.encodeNonNullable("name@core.digitalid.net", StringWrapper.encodeNonNullable("Person")).getBlock();
////        strings = new String[] {"", "name@core.digitalid.net", "(name@core.digitalid.net=\"person\" ) ", "name@core.digitalid.net \\ \"on\"", "name@core.digitalid.net ≠ \" ) \""};
////        for (final @Nonnull String string : strings) {
//////            System.out.println(string);
////            Expression expression = Expression.parse(string);
//////            System.out.println(expression);
//////            System.out.println();
////            assertTrue(expression.matches(attribute));
////        }
//    }
//    
//}