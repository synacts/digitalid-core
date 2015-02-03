package ch.virtualid.password;

import ch.virtualid.annotations.Committing;
import ch.virtualid.database.Database;
import ch.virtualid.password.Password;
import ch.virtualid.setup.IdentitySetup;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Unit testing of the {@link Password password} with its {@link Action actions}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class PasswordTest extends IdentitySetup {
    
    private static final @Nonnull String VALUE = "Pa$$word";
        
    @Test
    @Committing
    public void _01_testValueReplace() throws SQLException {
        print("_01_testValueReplace");
        try {
            final @Nonnull Password password = Password.get(getRole());
            password.setValue(VALUE);
            Password.reset(getRole()); // Not necessary but I want to test the database state.
            Assert.assertEquals(VALUE, password.getValue());
            Database.commit();
        } catch (@Nonnull SQLException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
