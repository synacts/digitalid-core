package net.digitalid.core.settings;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.testing.SQLTestBase;

import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;

import org.junit.BeforeClass;
import org.junit.Test;

@Immutable
@GenerateBuilder
@GenerateSubclass
abstract class TestSite extends CoreSite<TestSite> {
    
    @Pure
    @Override
    public @Nonnull TestSite getSite() {
        return this;
    }
    
}

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
interface TestNonHostEntity extends NonHostEntity<TestSite> {
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}

public class SettingsTest extends SQLTestBase {
    
    private static final @Nonnull String VALUE = ""; // TODO: Choose a non-default password like "Pa$$word" once properties can be loaded from the database.
    
    private static final @Nonnull TestSite SITE = TestSiteBuilder.withSchemaName("default").withHost(true).withClient(false).build();
        
    @Impure
    @BeforeClass
    public static void createTables() throws Exception {
        SQL.create(SettingsSubclass.PASSWORD_TABLE.getEntryConverter(), SITE);
    }
    
    @Test
    public void _01_testValueReplace() throws DatabaseException {
        try {
            final @Nonnull Settings settings = Settings.of(TestNonHostEntityBuilder.withSite(SITE).withKey(0).withIdentity(SemanticType.map("test@core.digitalid.net")).build());
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
