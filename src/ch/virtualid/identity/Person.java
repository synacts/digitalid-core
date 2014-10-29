package ch.virtualid.identity;

import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import java.io.IOException;
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
 * @version 1.8
 */
public abstract class Person extends NonHostIdentity implements Immutable {
    
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
    
    
    @Override
    public final boolean hasBeenMerged() throws SQLException, IOException, PacketException, ExternalException  {
        final @Nullable NonHostIdentifier successor = Mapper.getSuccessor(getAddress());
        if (successor != null) {
            final long number = getNumber();
            successor.getIdentity();
            if (number != getNumber()) {
                // The number and address got updated 'automatically' (because this is the 'official' identity obtained through the mapper).
                return true;
            } else {
                // The number and address might need to be updated 'manually' (because this is an 'inofficial' identity obtained through calling Identity.create(...) directly).
                final @Nonnull Identity identity = getAddress().getIdentity();
                assert identity instanceof Person : "The relocated identity should still be a person.";
                // TODO: In case of external persons, only update the number and leave the address as is? Probably yes, but set the successor accordingly.
//                update(identity.number, ((Person) identity).getNonHostAddress());
                // TODO: The following line is wrong (always returns false) and the whole method should be improved!
                return getNumber() != identity.getNumber();
            }
        } else {
            return false;
        }
    }
    
}
