package net.digitalid.core.settings;


import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.Subject;
import net.digitalid.database.testing.SQLTestBase;
import net.digitalid.database.testing.TestSite;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;

import org.junit.BeforeClass;
import org.junit.Test;

@Immutable
@GenerateBuilder
@GenerateSubclass
interface TestNonHostEntity extends NonHostEntity {
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}

public class PasswordTest extends SQLTestBase {
    
    private static final @Nonnull String VALUE = ""; // TODO: Choose a non-default password like "Pa$$word" once properties can be loaded from the database.
        
    @Impure
    @BeforeClass
    public static void createTables() throws Exception {
        SQL.create(Settings.PASSWORD_TABLE.getEntryConverter(), Subject.DEFAULT_SITE);
    }
    
    @Test
    public void _01_testValueReplace() throws DatabaseException {
        try {
            final @Nonnull Settings settings = Settings.of(TestNonHostEntityBuilder.withSite(TestSite.INSTANCE).withKey(0).withIdentity(SemanticType.map("test@core.digitalid.net")).build());
            settings.password().set(VALUE);
            settings.password().reset(); // Not necessary but I want to test the database state.
            assertEquals(VALUE, settings.password().get());
            Database.commit();
        } catch (@Nonnull DatabaseException exception) {
            exception.printStackTrace();
            Database.rollback();
            throw exception;
        }
    }
    
}
