package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.identification.identity.InternalNonHostIdentity;
import net.digitalid.core.unit.CoreUnit;

/**
 * This interface models a non-host entity.
 */
@Immutable
@GenerateConverter
public interface NonHostEntity extends Entity {
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public @Nonnull InternalNonHostIdentity getIdentity();
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    @Pure
    @Recover
    @NonCommitting
    public static @Nonnull NonHostEntity with(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException {
        final @Nonnull Entity entity = Entity.with(unit, key);
        if (entity instanceof NonHostEntity) { return (NonHostEntity) entity; }
        else { throw RecoveryExceptionBuilder.withMessage("The key " + key + " denotes a host identity.").build(); }
    }
    
}
