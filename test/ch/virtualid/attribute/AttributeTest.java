package ch.virtualid.attribute;

import ch.virtualid.client.Cache;
import ch.virtualid.exceptions.external.AttributeNotFoundException;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.setup.IdentitySetup;
import ch.xdf.StringWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Attribute attribute} with its {@link Action actions}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class AttributeTest extends IdentitySetup {
    
    @Test
    public void _01_testValueReplace() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull String name = "Test Person";
        final @Nonnull Attribute attribute = Attribute.get(getRole(), AttributeType.NAME);
        attribute.setValue(new UncertifiedAttributeValue(new StringWrapper(AttributeType.NAME, name)));
        attribute.reset(); // Not necessary but I want to test the database state.
        final @Nullable AttributeValue attributeValue = attribute.getValue();
        Assert.assertNotNull(attributeValue);
        Assert.assertEquals(name, new StringWrapper(attributeValue.getContent()).getString());
    }
    
    @Test(expected = AttributeNotFoundException.class)
    public void _02_testNonPublicAccess() throws SQLException, IOException, PacketException, ExternalException {
        Cache.getFreshAttributeContent(getSubject(), getRole(), AttributeType.NAME, false);
    }
    
}
