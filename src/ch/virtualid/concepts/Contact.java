package ch.virtualid.concepts;

import ch.virtualid.agent.ReadonlyAuthentications;
import ch.virtualid.agent.ReadonlyPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Concept;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Contacts have certain {@link Permissions permissions} and {@link Authentications authentications}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.1
 */
public final class Contact extends Concept implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code contact@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("contact@virtualid.ch").load(Person.IDENTIFIER);
    
    /**
     * Stores the person of this contact.
     */
    private @Nonnull Person person;
    
    /**
     * Creates a new contact with the given entity and person.
     * 
     * @param entity the entity to which this contact belongs.
     * @param person the person that is a contact of the entity.
     */
    public Contact(@Nonnull Entity entity, @Nonnull Person person) {
        super(entity);
        
        this.person = person;
    }
    
    /**
     * Creates new contact from the given block.
     * 
     * @param block the block containing the contact.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    public Contact(@Nonnull Entity entity, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
        super(entity);
        
        assert block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
        
        this.person = new NonHostIdentifier(block).getIdentity().toPerson();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return person.getAddress().toBlock().setType(TYPE);
    }
    
    /**
     * Returns the person of this contact.
     * 
     * @return the person of this contact.
     */
    public @Nonnull Person getPerson() {
        return person;
    }
    
    public @Nonnull ReadonlyPermissions getPermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public @Nonnull ReadonlyAuthentications getAuthentications() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
