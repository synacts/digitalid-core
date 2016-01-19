package net.digitalid.core.settings;

import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.Committing;

import net.digitalid.core.settings.Settings;

import net.digitalid.core.server.IdentitySetup;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Settings password} with its {@link Action actions}.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class PasswordTest extends IdentitySetup {
    
    private static final @Nonnull String VALUE = "Pa$$word";
        
    @Test
    @Committing
    public void _01_testValueReplace() throws DatabaseException {
        print("_01_testValueReplace");
        try {
            final @Nonnull Settings password = Settings.get(getRole());
            password.setValue(VALUE);
            Settings.reset(getRole()); // Not necessary but I want to test the database state.
            Assert.assertEquals(VALUE, password.getValue());
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
