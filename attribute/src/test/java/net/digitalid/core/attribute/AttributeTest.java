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
// TODO:

//package net.digitalid.core.attribute;
//
//import java.sql.SQLException;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.exceptions.ExternalException;
//
//import net.digitalid.database.annotations.transaction.Committing;
//import net.digitalid.database.interfaces.Database;
//
//import net.digitalid.core.expression.PassiveExpression;
//
//import org.junit.Assert;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//
///**
// * Unit testing of the {@link Attribute attribute} with its {@link Action actions}.
// */
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public final class AttributeTest extends IdentitySetup {
//    
//    private static final @Nonnull String NAME = "Test Person";
//        
//    @Test
//    @Committing
//    public void _01_testValueReplace() throws DatabaseException, InvalidEncodingException {
//        print("_01_testValueReplace");
//        try {
//            final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeTypes.NAME);
//            attribute.setValue(new UncertifiedAttributeValue(StringWrapper.encodeNonNullable(AttributeTypes.NAME, NAME)));
//            attribute.reset(); // Not necessary but I want to test the database state.
//            final @Nullable AttributeValue attributeValue = attribute.getValue();
//            Assert.assertNotNull(attributeValue);
//            Assert.assertEquals(NAME, StringWrapper.decodeNonNullable(attributeValue.getContent()));
//            Database.commit();
//        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Committing
//    @Test(expected = AttributeNotFoundException.class)
//    public void _02_testNonPublicAccess() throws ExternalException {
//        print("_02_testNonPublicAccess");
//        try {
//            Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
//            Database.commit();
//        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//            if (!(exception instanceof AttributeNotFoundException)) { exception.printStackTrace(); }
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Test
//    @Committing
//    public void _03_testVisibilityReplace() throws ExternalException {
//        print("_03_testVisibilityReplace");
//        try {
//            final @Nonnull PassiveExpression passiveExpression = new PassiveExpression(getRole(), "everybody");
//            final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeTypes.NAME);
//            attribute.setVisibility(passiveExpression);
//            attribute.reset(); // Not necessary but I want to test the database state.
//            Assert.assertEquals(passiveExpression, attribute.getVisibility());
//            Database.commit();
//        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//    @Test
//    @Committing
//    public void _04_testPublicAccess() throws ExternalException {
//        print("_04_testPublicAccess");
//        try {
//            final @Nonnull Block content = Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
//            Assert.assertEquals(NAME, StringWrapper.decodeNonNullable(content));
//            Database.commit();
//        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//            exception.printStackTrace();
//            Database.rollback();
//            throw exception;
//        }
//    }
//    
//}
