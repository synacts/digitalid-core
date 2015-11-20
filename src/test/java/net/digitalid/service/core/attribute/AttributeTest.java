package net.digitalid.service.core.attribute;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.cache.Cache;
import net.digitalid.service.core.concepts.attribute.Attribute;
import net.digitalid.service.core.concepts.attribute.AttributeTypes;
import net.digitalid.service.core.concepts.attribute.AttributeValue;
import net.digitalid.service.core.concepts.attribute.UncertifiedAttributeValue;
import net.digitalid.service.core.exceptions.external.notfound.AttributeNotFoundException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.expression.PassiveExpression;
import net.digitalid.service.core.setup.IdentitySetup;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.configuration.Database;
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
            attribute.setValue(new UncertifiedAttributeValue(new StringWrapper(AttributeTypes.NAME, NAME)));
            attribute.reset(); // Not necessary but I want to test the database state.
            final @Nullable AttributeValue attributeValue = attribute.getValue();
            Assert.assertNotNull(attributeValue);
            Assert.assertEquals(NAME, new StringWrapper(attributeValue.getContent()).getString());
            Database.commit();
        } catch (@Nonnull SQLException | InvalidEncodingException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Committing
    @Test(expected = AttributeNotFoundException.class)
    public void _02_testNonPublicAccess() throws DatabaseException, PacketException, ExternalException, NetworkException {
        print("_02_testNonPublicAccess");
        try {
            Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
            Database.commit();
        } catch (@Nonnull DatabaseException | PacketException | ExternalException | NetworkException exception) {
            if (!(exception instanceof AttributeNotFoundException)) { exception.printStackTrace(); }
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _03_testVisibilityReplace() throws DatabaseException, PacketException, ExternalException, NetworkException {
        print("_03_testVisibilityReplace");
        try {
            final @Nonnull PassiveExpression passiveExpression = new PassiveExpression(getRole(), "everybody");
            final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeTypes.NAME);
            attribute.setVisibility(passiveExpression);
            attribute.reset(); // Not necessary but I want to test the database state.
            Assert.assertEquals(passiveExpression, attribute.getVisibility());
            Database.commit();
        } catch (@Nonnull DatabaseException | PacketException | ExternalException | NetworkException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _04_testPublicAccess() throws DatabaseException, PacketException, ExternalException, NetworkException {
        print("_04_testPublicAccess");
        try {
            final @Nonnull Block content = Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
            Assert.assertEquals(NAME, new StringWrapper(content).getString());
            Database.commit();
        } catch (@Nonnull DatabaseException | PacketException | ExternalException | NetworkException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
