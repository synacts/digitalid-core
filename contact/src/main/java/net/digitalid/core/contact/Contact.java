package net.digitalid.core.contact;

import java.sql.SQLException;
import java.sql.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.concurrent.ConcurrentHashMap;
import net.digitalid.utility.collections.concurrent.ConcurrentMap;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.Database;
import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identification.identifier.IdentifierImplementation;
import net.digitalid.core.identification.identity.ExternalPerson;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.IdentityImplementation;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.Person;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.resolution.Mapper;

import net.digitalid.service.core.block.Blockable;
import net.digitalid.service.core.concept.Aspect;
import net.digitalid.service.core.concept.Instance;
import net.digitalid.service.core.concept.NonHostConcept;
import net.digitalid.service.core.concept.Observer;
import net.digitalid.service.core.database.SQLizable;

/**
 * Contacts have certain {@link FreezableNodePermissions permissions} and {@link FreezableAuthentications authentications}.
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
        Require.that(isInternal()).orThrow("This contact is internal.");
        
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
        Require.that(isExternal()).orThrow("This contact is external.");
        
        return (ExternalPerson) person;
    }
    
    /* -------------------------------------------------- Permissions -------------------------------------------------- */
    
    /**
     * Returns the permissions of this contact.
     * 
     * @return the permissions of this contact.
     */
    @NonCommitting
    public @Nonnull ReadOnlyNodePermissions getPermissions() throws DatabaseException {
        return FreezableNodePermissions.NONE; // TODO
    }
    
    /* -------------------------------------------------- Authentications -------------------------------------------------- */
    
    /**
     * Returns the authentications of this contact.
     * 
     * @return the authentications of this contact.
     */
    @NonCommitting
    public @Nonnull ReadOnlyAuthentications getAuthentications() throws DatabaseException {
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
    public static @Nonnull Contact get(@Nonnull NonHostEntity entity, @Nonnull Block block) throws ExternalException {
        Require.that(block.getType().isBasedOn(TYPE)).orThrow("The block is based on the indicated type.");
        
        return get(entity, IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).getIdentity().castTo(Person.class));
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
    public static @Nullable Contact get(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
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
    public static @Nonnull Contact getNotNull(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        final @Nonnull Identity identity = IdentityImplementation.getNotNull(resultSet, columnIndex);
        if (identity instanceof Person) { return get(entity, (Person) identity); }
        else { throw new SQLException("A non-person was stored as a contact."); }
    }
    
    @Override
    @NonCommitting
    public void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
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
    public static void set(@Nullable Contact contact, @NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
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
