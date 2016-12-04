package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.interfaces.Site;

import net.digitalid.core.identification.identity.InternalNonHostIdentity;

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
    public static @Nonnull NonHostEntity with(@Nonnull Site site, long key) /*throws DatabaseException */{
        // TODO: Think about how to recover entities. Maybe make it configurable?
//        if (site instanceof Host) {
//            return Account.getNotNull((Host) site, resultSet, columnIndex);
//        } else if (site instanceof Client) {
//            return Role.getNotNull((Client) site, resultSet, columnIndex);
//        } else {
//            throw UnexpectedValueException.with("A site is either a host or a client.");
//        }
        throw new RuntimeException();
    }
    
}
