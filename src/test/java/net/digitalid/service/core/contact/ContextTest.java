package net.digitalid.service.core.contact;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.concepts.contact.Contact;
import net.digitalid.service.core.concepts.contact.Context;
import net.digitalid.service.core.concepts.contact.FreezableContacts;
import net.digitalid.service.core.setup.IdentitySetup;
import net.digitalid.utility.database.annotations.Committing;
import net.digitalid.utility.database.configuration.Database;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Context context} with its {@link Action actions}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class ContextTest extends IdentitySetup {
    
    private static @Nonnull Context context;
    
    private static @Nonnull Contact contact;
    
    @BeforeClass
    public static void setUpContext() {
        print("setUpContext");
        context = Context.getRoot(getRole());
        contact = Contact.get(getRole(), getSubject());
    }
    
    @Test
    @Committing
    public void _01_testEmptyContext() throws DatabaseException {
        print("_01_testEmptyContext");
        try {
            Assert.assertTrue(context.getContacts().isEmpty());
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _02_testAddContact() throws DatabaseException {
        print("_02_testAddContact");
        try {
            context.addContacts(new FreezableContacts(contact).freeze());
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _03_testContainsContact() throws DatabaseException {
        print("_03_testContainsContact");
        try {
            Assert.assertTrue(context.contains(contact));
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _04_testRemoveContact() throws DatabaseException {
        print("_04_testRemoveContact");
        try {
            context.removeContacts(new FreezableContacts(contact).freeze());
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
    @Test
    @Committing
    public void _05_testEmptyContext() throws DatabaseException {
        print("_05_testEmptyContext");
        try {
            Assert.assertTrue(context.getContacts().isEmpty());
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
