package ch.virtualid.identity;

import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models a person.
 * <p>
 * <em>Important:</em> Do not rely on the hash of persons because it may change at any time with mergers!
 * 
 * @see InternalPerson
 * @see ExternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Person extends NonHostIdentityClass implements Immutable {
    
    /**
     * Stores the semantic type {@code person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("person@virtualid.ch").load(NonHostIdentity.IDENTIFIER);
    
    
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
