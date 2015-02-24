package net.digitalid.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.interfaces.Immutable;

/**
 * This class models a person.
 * <p>
 * <em>Important:</em> Do not rely on the hash of persons because it may change at any time with mergers!
 * 
 * @see InternalPerson
 * @see ExternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Person extends NonHostIdentityClass implements Immutable {
    
    /**
     * Stores the semantic type {@code person@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("person@core.digitalid.net").load(NonHostIdentity.IDENTIFIER);
    
    
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
    public final boolean hasBeenMerged(@Nonnull SQLException exception) throws SQLException {
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
