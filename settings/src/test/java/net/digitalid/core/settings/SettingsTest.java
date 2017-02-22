package net.digitalid.core.settings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.entity.CoreUnit;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.testing.CoreTest;

import org.junit.BeforeClass;

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
    @PrimaryKey
    public long getKey();
    
    @Pure
    @Override
    // TODO: The generated builder cannot handle this: @NonRepresentative
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}

public class SettingsTest extends CoreTest {
    
    private static final @Nonnull String VALUE = ""; // TODO: Choose a non-default password like "Pa$$word" once properties can be loaded from the database.
    
    // nullable until createTables is called
    private static @Nullable TestUnit UNIT;
    
    private static final @Nonnull SemanticType TYPE = SemanticType.map("test@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build());
    
    private static final @Nonnull TestNonHostEntity ENTITY = TestNonHostEntityBuilder.withUnit(UNIT).withKey(0).withIdentity(TYPE).build();
    
    private static final @Nonnull Settings SETTINGS = Settings.of(ENTITY);
    
    @Impure
    @BeforeClass
    public static void createTables() throws ExternalException {
        UNIT = TestUnitBuilder.withName("default").withHost(true).withClient(false).build();
        SQL.createTable(SettingsSubclass.MODULE.getSubjectConverter(), UNIT);
        SQL.createTable(SettingsSubclass.PASSWORD_TABLE.getEntryConverter(), UNIT);
        SQL.insert(SettingsSubclass.MODULE.getSubjectConverter(), SETTINGS, UNIT);
    }
    
//    @Test
    @Pure
    @TODO(task = "Reactivate this test case. Creating the subject table failed.", date = "2017-02-07", author = Author.KASPAR_ETTER)
    public void _01_testValueReplace() throws DatabaseException, RecoveryException {
        try {
            SETTINGS.password().set(VALUE);
            SETTINGS.password().reset(); // Not necessary but I want to test the database state.
            assertThat(SETTINGS.password().get()).isEqualTo(VALUE);
            Database.instance.get().commit();
        } catch (@Nonnull DatabaseException | RecoveryException exception) {
            Database.instance.get().rollback();
            throw exception;
        }
    }
    
}
