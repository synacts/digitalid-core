package net.digitalid.core.settings;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.type.Embedded;
import net.digitalid.database.auxiliary.None;
import net.digitalid.database.property.value.WritablePersistentValueProperty;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.property.value.ValuePropertyRequiredAuthorization;
import net.digitalid.core.property.value.ValuePropertyRequiredAuthorizationBuilder;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.restrictions.RestrictionsBuilder;
import net.digitalid.core.subject.CoreServiceCoreSubject;
import net.digitalid.core.subject.annotations.GenerateSynchronizedProperty;

/**
 * This class models the settings of a digital identity.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class Settings extends CoreServiceCoreSubject<NonHostEntity<?>, None> {
    
    @Pure
    @Override
    @Embedded // TODO: Depends on the key type!
    @TODO(task = "Why aren't the annotations inherited and included in the generated converter otherwise? (Remove this method once this issue is fixed.)", date = "2016-11-14", author = Author.KASPAR_ETTER)
    public abstract @Nonnull None getKey();
    
    /* -------------------------------------------------- Index -------------------------------------------------- */
    
    /**
     * Returns the potentially cached settings of the given entity that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull Settings of(@Nonnull NonHostEntity<?> entity) {
        return SettingsSubclass.MODULE.getConceptIndex().get(entity, None.INSTANCE);
    }
    
    /* -------------------------------------------------- Password -------------------------------------------------- */
    
    /**
     * Stores the required authorization to change the password.
     */
    static final @Nonnull ValuePropertyRequiredAuthorization<NonHostEntity<?>, None, Settings, String> PASSWORD = ValuePropertyRequiredAuthorizationBuilder.<NonHostEntity<?>, None, Settings, String>withRequiredRestrictionsToExecuteMethod((concept, value) -> RestrictionsBuilder.withOnlyForClients(true).withWriteToNode(true).build()).withRequiredRestrictionsToSeeMethod((concept, value) -> Restrictions.ONLY_FOR_CLIENTS).build();
    
    /**
     * Returns the password property of these settings.
     */
    @Pure
    @Default("\"\"")
    @GenerateSynchronizedProperty
    public abstract @Nonnull WritablePersistentValueProperty<Settings, @Nonnull @MaxSize(50) String> password();
    
}
