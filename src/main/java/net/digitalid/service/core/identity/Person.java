package net.digitalid.service.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.identifier.InternalNonHostIdentifier;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.service.core.identity.resolution.Successor;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.database.annotations.NonCommitting;

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
    
    /**
     * Stores the semantic type {@code person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("person@core.digitalid.net").load(NonHostIdentity.IDENTIFIER);
    
    
    /**
     * Creates a new person with the given internal number.
     * 
     * @param number the number that represents this identity.
     */
    Person(long number) {
        super(number);
    }
    
    
    /**
     * Sets the address of this person.
     * 
     * @param address the new address of this person.
     */
    abstract void setAddress(@Nonnull InternalNonHostIdentifier address);
    
    @Override
    @NonCommitting
    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws AbortException {
        final @Nullable InternalNonHostIdentifier successor = Successor.get(getAddress());
        if (successor != null && successor.isMapped()) {
            final @Nonnull InternalNonHostIdentity person = successor.getMappedIdentity();
            setAddress(person.getAddress());
            setNumber(person.getNumber());
            return true;
        } else {
            Mapper.unmap(this);
            throw exception;
        }
    }
    
}
