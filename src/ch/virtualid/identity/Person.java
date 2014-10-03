package ch.virtualid.identity;

import ch.virtualid.exceptions.external.IdentityNotFoundException;
import ch.virtualid.interfaces.Immutable;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import todo;

/**
 * This class models the person virtual identities.
 * <p>
 * <em>Important:</em> Do not rely on the hash of persons because it may change at any time with mergers!
 * 
 * @see NaturalPerson
 * @see ArtificialPerson
 * @see EmailPerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.8
 */
public abstract class Person extends NonHostIdentity implements Immutable {
    
    /**
     * Stores the semantic type {@code person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("person@virtualid.ch").load(NonHostIdentity.IDENTIFIER);
    
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     */
    Person(long number, @Nonnull NonHostIdentifier address) {
        super(number, address);
    }
    
    
    /**
     * Sets the number that represents this person.
     * 
     * @param number the new number of this identity.
     */
    final void setNumber(long number) {
        this.number = number;
    }
    
    
    @Override
    public final boolean hasBeenMerged() throws SQLException {
        final @Nullable NonHostIdentifier successor = Mapper.getSuccessor((NonHostIdentifier) address);
        if (successor != null) {
            try {
                final long number = this.number;
                successor.getIdentity();
                if (number != this.number) {
                    // The number and address got updated 'automatically' (because this is the 'official' identity obtained through the mapper).
                    return true;
                } else {
                    // The number and address might need to be updated 'manually' (because this is an 'inofficial' identity obtained through calling Identity.create(...) directly).
                    final @Nonnull Identity identity = address.getIdentity();
                    assert identity instanceof Person : "The relocated identity should still be a person.";
                    update(identity.number, ((Person) identity).getNonHostAddress());
                    // TODO: The following line is wrong (always returns false) and the whole method should be improved!
                    return this.number != identity.number;
                }
            } catch (@Nonnull IdentityNotFoundException exception) { return false; }
        } else {
            return false;
        }
    }
    
}
