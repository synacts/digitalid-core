package ch.virtualid.contact;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Concept;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.javatuples.Pair;

/**
 * Contacts have certain {@link Permissions permissions} and {@link Authentications authentications}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.6
 */
public final class Contact extends Concept implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code contact@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("contact@virtualid.ch").load(Person.IDENTIFIER);
    
    
    /**
     * Stores the person of this contact.
     */
    private final @Nonnull Person person;
    
    /**
     * Creates a new contact with the given entity and person.
     * 
     * @param entity the entity to which this contact belongs.
     * @param person the person that is a contact of the entity.
     */
    private Contact(@Nonnull Entity entity, @Nonnull Person person) {
        super(entity);
        
        this.person = person;
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
    
    
    /**
     * Returns the permissions of this contact.
     * 
     * @return the permissions of this contact.
     */
    public @Nonnull ReadonlyContactPermissions getPermissions() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Returns the authentications of this contact.
     * 
     * @return the authentications of this contact.
     */
    public @Nonnull ReadonlyAuthentications getAuthentications() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    // TODO: Include methods to aggregate the permissions and authentications over the contexts.
    
    
    /**
     * Caches contacts given their entity and person.
     */
    private static final @Nonnull Map<Pair<Entity, Person>, Contact> index = new HashMap<Pair<Entity, Person>, Contact>();
    
    // TODO: Make similar changes as in the Context class.
    
    /**
     * Returns a (locally cached) contact of the given person.
     * 
     * @param entity the entity to which the contact belongs.
     * @param person the person that is behind the contact.
     * 
     * @return a new or existing contact with the given entity and person.
     */
    @Pure
    public static @Nonnull Contact get(@Nonnull Entity entity, @Nonnull Person person) {
        if (Database.isSingleAccess()) {
            synchronized(index) {
                final @Nonnull Pair<Entity, Person> pair = new Pair<Entity, Person>(entity, person);
                @Nullable Contact contact = index.get(pair);
                if (contact == null) {
                    contact = new Contact(entity, person);
                    index.put(pair, contact);
                }
                return contact;
            }
        } else {
            return new Contact(entity, person);
        }
    }
    
    /**
     * Returns the contact with the person given by the block.
     * 
     * @param entity the entity to which the context belongs.
     * @param block a block containing the person of the contact.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    public static @Nonnull Contact get(@Nonnull Entity entity, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        return get(entity, new NonHostIdentifier(block).getIdentity().toPerson());
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the contact belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Contact get(@Nonnull Entity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException, InvalidEncodingException {
        return get(entity, Identity.get(resultSet, columnIndex).toPerson());
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, person.getNumber());
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Contact)) return false;
        final @Nonnull Contact other = (Contact) object;
        return this.person.equals(other.person) && this.getEntityNotNull().equals(other.getEntityNotNull());
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.getPerson());
        hash = 89 * hash + Objects.hashCode(this.getEntity());
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return person.toString();
    }
    
}
