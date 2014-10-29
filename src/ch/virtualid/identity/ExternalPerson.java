package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class ExternalPerson extends Person implements ExternalIdentity, Immutable {
    
    /**
     * Stores the semantic type {@code external.person@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("external.person@virtualid.ch").load(Person.IDENTIFIER);
    
    
    /**
     * Stores the successor of this external person or null if there is none.
     */
    private @Nullable InternalPerson successor = null;
    
    /**
     * Creates a new external person with the given number.
     * 
     * @param number the number that represents this identity.
     */
    ExternalPerson(long number) {
        super(number);
    }
    
    @Pure
    @Override
    public final @Nullable InternalPerson getSuccessor() {
        return successor;
    }
    
    /**
     * Sets the successor of this external person.
     * 
     * @param successor the new successor of this person.
     */
    final void setSuccessor(@Nonnull InternalPerson successor) {
        this.successor = successor;
    }
    
}
