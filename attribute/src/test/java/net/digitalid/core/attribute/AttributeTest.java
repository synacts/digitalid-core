package net.digitalid.core.attribute;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.logging.exceptions.ExternalException;

import net.digitalid.database.core.Database;
import net.digitalid.database.annotations.transaction.Committing;

import net.digitalid.core.cache.Cache;
import net.digitalid.core.cache.exceptions.AttributeNotFoundException;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.server.IdentitySetup;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Attribute attribute} with its {@link Action actions}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AttributeTest extends IdentitySetup {
    
    private static final @Nonnull String NAME = "Test Person";
        
    @Test
    @Committing
    public void _01_testValueReplace() throws DatabaseException, InvalidEncodingException {
        print("_01_testValueReplace");
        try {
            final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeTypes.NAME);
            attribute.setValue(new UncertifiedAttributeValue(StringWrapper.encodeNonNullable(AttributeTypes.NAME, NAME)));
            attribute.reset(); // Not necessary but I want to test the database state.
            final @Nullable AttributeValue attributeValue = attribute.getValue();
            Assert.assertNotNull(attributeValue);
            Assert.assertEquals(NAME, StringWrapper.decodeNonNullable(attributeValue.getContent()));
            Database.commit();
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Committing
    @Test(expected = AttributeNotFoundException.class)
    public void _02_testNonPublicAccess() throws ExternalException {
        print("_02_testNonPublicAccess");
        try {
            Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
            Database.commit();
        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
            if (!(exception instanceof AttributeNotFoundException)) { exception.printStackTrace(); }
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _03_testVisibilityReplace() throws ExternalException {
        print("_03_testVisibilityReplace");
        try {
            final @Nonnull PassiveExpression passiveExpression = new PassiveExpression(getRole(), "everybody");
            final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeTypes.NAME);
            attribute.setVisibility(passiveExpression);
            attribute.reset(); // Not necessary but I want to test the database state.
            Assert.assertEquals(passiveExpression, attribute.getVisibility());
            Database.commit();
        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _04_testPublicAccess() throws ExternalException {
        print("_04_testPublicAccess");
        try {
            final @Nonnull Block content = Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
            Assert.assertEquals(NAME, StringWrapper.decodeNonNullable(content));
            Database.commit();
        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
