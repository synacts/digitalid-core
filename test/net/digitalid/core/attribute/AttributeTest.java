package net.digitalid.core.attribute;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Committing;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.database.Database;
import net.digitalid.core.exceptions.external.AttributeNotFoundException;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.setup.IdentitySetup;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.StringWrapper;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Attribute attribute} with its {@link Action actions}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AttributeTest extends IdentitySetup {
    
    private static final @Nonnull String NAME = "Test Person";
        
    @Test
    @Committing
    public void _01_testValueReplace() throws SQLException, InvalidEncodingException {
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
    public void _02_testNonPublicAccess() throws SQLException, IOException, PacketException, ExternalException {
        print("_02_testNonPublicAccess");
        try {
            Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
            Database.commit();
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            if (!(exception instanceof AttributeNotFoundException)) exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _03_testVisibilityReplace() throws SQLException, IOException, PacketException, ExternalException {
        print("_03_testVisibilityReplace");
        try {
            final @Nonnull PassiveExpression passiveExpression = new PassiveExpression(getRole(), "everybody");
            final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeTypes.NAME);
            attribute.setVisibility(passiveExpression);
            attribute.reset(); // Not necessary but I want to test the database state.
            Assert.assertEquals(passiveExpression, attribute.getVisibility());
            Database.commit();
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _04_testPublicAccess() throws SQLException, IOException, PacketException, ExternalException {
        print("_04_testPublicAccess");
        try {
            final @Nonnull Block content = Cache.getReloadedAttributeContent(getSubject(), getRole(), AttributeTypes.NAME, false);
            Assert.assertEquals(NAME, new StringWrapper(content).getString());
            Database.commit();
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
