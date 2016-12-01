package net.digitalid.core.contact;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.ExternalPerson;

/**
 * An external contact represents an {@link ExternalPerson external person}.
 */
@Immutable
// TODO: @GenerateSubclass
@GenerateConverter
public abstract class ExternalContact extends Contact {
    
    /* -------------------------------------------------- Person -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull ExternalPerson getPerson();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached contact of the given entity and person that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull ExternalContact of(@Nonnull NonHostEntity entity, @Nonnull ExternalPerson person) {
        return null; // TODO
    }
    
}
