package net.digitalid.service.core.concepts.contact;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.Blockable;
import net.digitalid.service.core.concept.Aspect;
import net.digitalid.service.core.concept.Instance;
import net.digitalid.service.core.concept.NonHostConcept;
import net.digitalid.service.core.concept.Observer;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.IdentifierImplementation;
import net.digitalid.service.core.identity.ExternalPerson;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.IdentityImplementation;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.Person;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * Contacts have certain {@link FreezableContactPermissions permissions} and {@link FreezableAuthentications authentications}.
 */
@Immutable
public final class Contact extends NonHostConcept implements Blockable, SQLizable {
    
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code contact@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("contact@core.digitalid.net").load(Person.IDENTIFIER);
    
    
    /* -------------------------------------------------- Person -------------------------------------------------- */
    
    /**
     * Stores the person of this contact.
     */
    private final @Nonnull Person person;
    
    /**
     * Returns the person of this contact.
     * 
     * @return the person of this contact.
     */
    public @Nonnull Person getPerson() {
        return person;
    }
    
    /**
     * Returns whether this contact is internal.
     * 
     * @return whether this contact is internal.
     */
    public boolean isInternal() {
        return person instanceof InternalPerson;
    }
    
    /**
     * Returns the internal person of this contact.
     * 
     * @return the internal person of this contact.
     * 
     * @require isInternal() : "This contact is internal.";
     */
    public @Nonnull InternalPerson getInternalPerson() {
        assert isInternal() : "This contact is internal.";
        
        return (InternalPerson) person;
    }
    
    /**
     * Returns whether this contact is external.
     * 
     * @return whether this contact is external.
     */
    public boolean isExternal() {
        return person instanceof ExternalPerson;
    }
    
    /**
     * Returns the external person of this contact.
     * 
     * @return the external person of this contact.
     * 
     * @require isExternal() : "This contact is external.";
     */
    public @Nonnull ExternalPerson getExternalPerson() {
        assert isExternal() : "This contact is external.";
        
        return (ExternalPerson) person;
    }
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions of this contact.
     * 
     * @return the permissions of this contact.
     */
    @NonCommitting
    public @Nonnull ReadOnlyContactPermissions getPermissions() throws AbortException {
        return FreezableContactPermissions.NONE; // TODO
    }
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Returns the authentications of this contact.
     * 
     * @return the authentications of this contact.
     */
    @NonCommitting
    public @Nonnull ReadOnlyAuthentications getAuthentications() throws AbortException {
        return FreezableAuthentications.IDENTITY_BASED; // TODO
    }
    
    // TODO: Include methods to aggregate the permissions and authentications over the contexts.
    
    /* -------------------------------------------------- Indexing -------------------------------------------------- */
    
    /**
     * Caches contacts given their entity and person.
     */
    private static final @Nonnull ConcurrentMap<NonHostEntity, ConcurrentMap<Person, Contact>> index = new ConcurrentHashMap<>();
    
    static {
        if (Database.isSingleAccess()) {
            Instance.observeAspects(new Observer() {
                @Override public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) { index.remove(instance); }
            }, Entity.DELETED);
        }
    }
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    /**
     * Creates a new contact with the given entity and person.
     * 
     * @param entity the entity to which this contact belongs.
     * @param person the person that is a contact of the entity.
     */
    private Contact(@Nonnull NonHostEntity entity, @Nonnull Person person) {
        super(entity);
        
        this.person = person;
    }
    
    /**
     * Returns the (locally cached) contact of the given person.
     * 
     * @param entity the entity to which the contact belongs.
     * @param person the person that is behind the contact.
     * 
     * @return a new or existing contact with the given entity and person.
     */
    @Pure
    public static @Nonnull Contact get(@Nonnull NonHostEntity entity, @Nonnull Person person) {
        if (Database.isSingleAccess()) {
            @Nullable ConcurrentMap<Person, Contact> map = index.get(entity);
            if (map == null) { map = index.putIfAbsentElseReturnPresent(entity, new ConcurrentHashMap<Person, Contact>()); }
            @Nullable Contact contact = map.get(person);
            if (contact == null) { contact = map.putIfAbsentElseReturnPresent(person, new Contact(entity, person)); }
            return contact;
        } else {
            return new Contact(entity, person);
        }
    }
    
    /* -------------------------------------------------- Blockable -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return person.toBlock(TYPE);
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
    @NonCommitting
    public static @Nonnull Contact get(@Nonnull NonHostEntity entity, @Nonnull Block block) throws AbortException, PacketException, ExternalException, NetworkException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        return get(entity, IdentifierImplementation.create(block).getIdentity().toPerson());
    }
    
    /* -------------------------------------------------- SQLizable -------------------------------------------------- */
    
    /**
     * Stores the data type used to reference instances of this class.
     */
    public static final @Nonnull String FORMAT = Mapper.FORMAT;
    
    /**
     * Stores the foreign key constraint used to reference instances of this class.
     */
    public static final @Nonnull String REFERENCE = Mapper.REFERENCE;
    
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
    @NonCommitting
    public static @Nullable Contact get(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws AbortException {
        final @Nullable Identity identity = IdentityImplementation.get(resultSet, columnIndex);
        if (identity == null) { return null; }
        if (identity instanceof Person) { return get(entity, (Person) identity); }
        else { throw new SQLException("A non-person was stored as a contact."); }
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
    @NonCommitting
    public static @Nonnull Contact getNotNull(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws AbortException {
        final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, columnIndex);
        if (identity instanceof Person) { return get(entity, (Person) identity); }
        else { throw new SQLException("A non-person was stored as a contact."); }
    }
    
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws AbortException {
        preparedStatement.setLong(parameterIndex, person.getKey());
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given contact.
     * 
     * @param contact the contact to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public static void set(@Nullable Contact contact, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws AbortException {
        if (contact == null) { preparedStatement.setNull(parameterIndex, Types.BIGINT); }
        else { contact.set(preparedStatement, parameterIndex); }
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof Contact)) { return false; }
        final @Nonnull Contact other = (Contact) object;
        return this.person.equals(other.person) && this.getEntity().equals(other.getEntity());
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + getPerson().hashCode();
        hash = 89 * hash + getEntity().hashCode();
        return hash;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return person.toString();
    }
    
}
