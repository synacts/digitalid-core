package net.digitalid.core.identification.identity;

import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.resolution.Mapper;
import net.digitalid.core.resolution.Successor;

/**
 * This class models a person.
 * <p>
 * <em>Important:</em> Do not rely on the hash of persons because it may change at any time with mergers!
 * 
 * @see InternalPerson
 * @see ExternalPerson
 */
@Immutable
public abstract class Person extends NonHostIdentityImplementation {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new person with the given key.
     * 
     * @param key the number that represents this identity.
     */
    Person(long key) {
        super(key);
    }
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Sets the address of this person.
     * 
     * @param address the new address of this person.
     */
    public abstract void setAddress(@Nonnull Mapper.Key key, @Nonnull InternalNonHostIdentifier address);
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    @Override
    @NonCommitting
    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException {
        final @Nullable InternalNonHostIdentifier successor = Successor.get(getAddress());
        if (successor != null && successor.isMapped()) {
            final @Nonnull InternalNonHostIdentity person = successor.getMappedIdentity();
            setAddress(person.getAddress());
            setKey(person.getKey());
            return true;
        } else {
            Mapper.unmap(this);
            throw exception;
        }
    }
    
}
