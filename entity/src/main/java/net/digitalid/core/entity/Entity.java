package net.digitalid.core.entity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.factories.AccountFactory;
import net.digitalid.core.entity.factories.RoleFactory;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.unit.CoreUnit;
import net.digitalid.core.unit.annotations.CoreUnitBased;

/**
 * An entity captures the {@link CoreUnit unit} and the {@link Identity identity} of a core subject or handler.
 * 
 * @see NonHostEntity
 */
@Immutable
public interface Entity extends CoreUnitBased {
    
    /* -------------------------------------------------- Unit -------------------------------------------------- */
    
    /**
     * Returns the unit to which this entity belongs.
     */
    @Pure
    @Provided
    public @Nonnull CoreUnit getUnit();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Returns the number that represents this entity in the database.
     */
    @Pure
    public long getKey();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    /**
     * Returns the identity of this entity.
     */
    @Pure
    @NonRepresentative
    public @Nonnull InternalIdentity getIdentity();
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    @Pure
    @Override
    public default boolean isOnHost() {
        return getUnit().isHost();
    }
    
    @Pure
    @Override
    public default boolean isOnClient() {
        return getUnit().isClient();
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Recover
    @NonCommitting
    @SuppressWarnings("unchecked")
    public static @Nonnull Entity with(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException {
        if (unit.isHost()) {
            final @Nonnull Identity identity = IdentifierResolver.configuration.get().load(key);
            if (identity instanceof InternalIdentity) { return AccountFactory.create(unit, (InternalIdentity) identity); }
            else { throw RecoveryExceptionBuilder.withMessage("The key " + key + " does not belong to an internal identity.").build(); }
        } else if (unit.isClient()) {
            return RoleFactory.create(unit, key);
        } else {
            throw CaseExceptionBuilder.withVariable("unit").withValue(unit).build();
        }
    }
    
}
