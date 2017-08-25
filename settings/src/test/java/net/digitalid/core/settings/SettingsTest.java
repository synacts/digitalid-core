package net.digitalid.core.settings;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;
import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.NonHostEntityConverter;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;
import net.digitalid.core.testing.CoreTest;
import net.digitalid.core.unit.CoreUnit;

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
interface TestNonHostEntity extends NonHostEntity {
    
    @Pure
    @Override
    @PrimaryKey
    public long getKey();
    
    @Pure
    @Override
    // TODO: The generated builder cannot handle this: @NonRepresentative
    public @Nonnull InternalNonHostIdentity getIdentity();
    
}

@TODO(task = "This test case does not make much sense: If the password value is not empty, then the synchronizer tries to send an internal action, which cannot succeed at this point.", date = "2017-08-16", author = Author.KASPAR_ETTER)
public class SettingsTest extends CoreTest {
    
    private static final @Nonnull String PASSWORD_VALUE = ""; // TODO: Choose a non-default password like "Pa$$word" once properties can be loaded from the database.
    
    private static final @Nonnull TestUnit UNIT;
    
    static {
        try {
            UNIT = TestUnitBuilder.withName("default").withHost(false).withClient(true).build();
        } catch (@Nonnull ExternalException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    private static final @Nonnull SemanticType TYPE = SemanticType.map("test@core.digitalid.net").load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).build());
    
    private static final @Nonnull TestNonHostEntity ENTITY = TestNonHostEntityBuilder.withUnit(UNIT).withKey(0).withIdentity(TYPE).build();
    
    @Impure
    @BeforeClass
    public static void createTables() throws ExternalException {
        SQL.createTable(NonHostEntityConverter.INSTANCE, UNIT);
        SQL.createTable(SettingsSubclass.MODULE.getSubjectTable(), UNIT);
        SettingsSubclass.MODULE.accept(table -> SQL.createTable(table, UNIT));
        SQL.insertOrAbort(NonHostEntityConverter.INSTANCE, ENTITY, UNIT);
        Database.commit();
    }
    
    @Test
    public void testSynchronizedValueProperty() throws DatabaseException, RecoveryException {
        try {
            final @Nonnull Settings settings = Settings.of(ENTITY);
            settings.password().set(PASSWORD_VALUE);
            settings.password().reset(); // Not necessary but I want to test the database state.
            assertThat(settings.password().get()).isEqualTo(PASSWORD_VALUE);
            Database.commit();
        } catch (@Nonnull DatabaseException | RecoveryException exception) {
            Database.rollback();
            throw exception;
        }
    }
    
}
