package net.digitalid.core.settings;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.property.RequiredAuthorization;
import net.digitalid.core.property.RequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsBuilder;
import net.digitalid.core.subject.CoreServiceCoreSubject;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;
import net.digitalid.core.subject.utility.None;

/**
 * This class models the settings of a digital identity.
 */
@Immutable
@GenerateSubclass
@GenerateTableConverter
public abstract class Settings extends CoreServiceCoreSubject<NonHostEntity, None> {
    
    @Pure
    @Override
    public abstract @Nonnull None getKey();
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the potentially cached settings of the given entity after having inserted it into its database table.
     */
    @Pure
    @Recover
    public static @Nonnull Settings of(@Nonnull NonHostEntity entity) throws DatabaseException {
        return SettingsSubclass.MODULE.getSubjectIndex().get(entity, None.INSTANCE);
    }
    
    /* -------------------------------------------------- Password -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the password.
     */
    static final @Nonnull RequiredAuthorization<NonHostEntity, None, Settings, String> PASSWORD = RequiredAuthorizationBuilder.<NonHostEntity, None, Settings, String>withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withOnlyForClients(true).withWriteToNode(true).build()).withRequiredRestrictionsToSeeMethod((concept, value) -> Restrictions.ONLY_FOR_CLIENTS).build();
    
    /**
     * Returns the password property of these settings.
     */
    @Pure
    @Default("\"\"")
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Settings, @Nonnull @MaxSize(50) String> password();
    
}
