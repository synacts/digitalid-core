package net.digitalid.core.contact;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identity.InternalPerson;

/**
 * An internal contact represents an {@link InternalPerson internal person}.
 */
@Immutable
// TODO: @GenerateSubclass
@GenerateConverter
public abstract class InternalContact extends Contact {
    
    /* -------------------------------------------------- Person -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull InternalPerson getPerson();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns the potentially cached contact of the given entity and person that might not yet exist in the database.
     */
    @Pure
    @Recover
    public static @Nonnull InternalContact of(@Nonnull NonHostEntity entity, @Nonnull InternalPerson person) {
        return null; // TODO
    }
    
}
