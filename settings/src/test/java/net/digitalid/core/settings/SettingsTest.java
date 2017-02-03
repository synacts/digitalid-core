package net.digitalid.core.settings;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.asymmetrickey.CryptographyTestBase;
import net.digitalid.core.entity.CoreUnit;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;

import org.junit.BeforeClass;
import org.junit.Test;

@Immutable
@GenerateBuilder
@GenerateSubclass
abstract class TestUnit extends CoreUnit {}

@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
interface TestNonHostEntity extends NonHostEntity<TestUnit> {
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}

public class SettingsTest extends CryptographyTestBase {
    
    private static final @Nonnull String VALUE = ""; // TODO: Choose a non-default password like "Pa$$word" once properties can be loaded from the database.
    
    private static final @Nonnull TestUnit UNIT = TestUnitBuilder.withName("default").withHost(true).withClient(false).build();
    
    @Impure
    @BeforeClass
    public static void createTables() throws Exception {
        SQL.createTable(SettingsSubclass.PASSWORD_TABLE.getEntryConverter(), UNIT);
    }
    
    @Test
    public void _01_testValueReplace() throws DatabaseException, RecoveryException {
        try {
            final @Nonnull Settings settings = Settings.of(TestNonHostEntityBuilder.withUnit(UNIT).withKey(0).withIdentity(SemanticType.map("test@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build())).build());
            settings.password().set(VALUE);
            settings.password().reset(); // Not necessary but I want to test the database state.
            assertEquals(VALUE, settings.password().get());
            Database.instance.get().commit();
        } catch (@Nonnull DatabaseException | RecoveryException exception) {
            Database.instance.get().rollback();
            throw exception;
        }
    }
    
}
