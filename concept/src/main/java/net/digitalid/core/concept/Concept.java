package net.digitalid.core.concept;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.client.annotations.Clients;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.host.annotations.Hosts;
import net.digitalid.core.property.ConceptProperty;
import net.digitalid.core.property.ConceptPropertyTable;

/**
 * This class models a concept in the {@link Database database}.
 * A concept always belongs to an {@link Entity entity}.
 * 
 * @param <C> the type of the concept class that extends this class.
 * @param <E> either {@link Entity} for a general concept or {@link NonHostEntity} for a concept that exists only for non-hosts.
 *            (The type has to be a supertype of {@link NonHostEntity}, which cannot be declared in Java, unfortunately!)
 * @param <K> the type of the key which identifies an instance among all instances of a concept at the same entity.
 */
public abstract class Concept<E extends Entity, K> extends RootClass {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Stores the entity to which this concept belongs.
     */
    private final @Nonnull E entity;
    
    /**
     * Returns the entity to which this concept belongs.
     * 
     * @return the entity to which this concept belongs.
     */
    @Pure
    public final @Nonnull E getEntity() {
        return entity;
    }
    
    /* -------------------------------------------------- On Host -------------------------------------------------- */
    
    /**
     * Returns whether this concept is on a host.
     * 
     * @return whether this concept is on a host.
     */
    @Pure
    public final boolean isOnHost() {
        return entity instanceof Account;
    }
    
    /**
     * Returns the account to which this concept belongs.
     * 
     * @return the account to which this concept belongs.
     */
    @Pure
    @Hosts
    public final @Nonnull Account getAccount() {
        Require.that(isOnHost()).orThrow("This concept is on a host.");
        
        return (Account) entity;
    }
    
    /* -------------------------------------------------- On Client -------------------------------------------------- */
    
    /**
     * Returns whether this concept is on a client.
     * 
     * @return whether this concept is on a client.
     */
    @Pure
    public final boolean isOnClient() {
        return entity instanceof Role;
    }
    
    /**
     * Returns the role to which this concept belongs.
     * 
     * @return the role to which this concept belongs.
     */
    @Pure
    @Clients
    public final @Nonnull Role getRole() {
        assert isOnClient(): "This concept is on a client.";
        
        return (Role) entity;
    }
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Stores the key which identifies this concept.
     */
    private final @Nonnull K key;
    
    /**
     * Returns the key which identifies this concept.
     * 
     * @return the key which identifies this concept.
     */
    @Pure
    public final @Nonnull K getKey() {
        return key;
    }
    
    /* -------------------------------------------------- Setup -------------------------------------------------- */
    
    /**
     * Stores the setup of this concept.
     */
    private final @Nonnull ConceptSetup<C, E, K> setup;
    
    /**
     * Returns the setup of this concept.
     * 
     * @return the setup of this concept.
     */
    @Pure
    public final @Nonnull ConceptSetup<C, E, K> getSetup() {
        return setup;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new concept with the given entity, key and setup.
     * 
     * @param entity the entity to which the new concept belongs.
     * @param key the key which identifies the new concept.
     * @param setup the setup of the new concept.
     */
    protected Concept(@Nonnull E entity, @Nonnull K key, @Nonnull ConceptSetup<C, E, K> setup) {
        this.entity = entity;
        this.key = key;
        this.setup = setup;
    }
    
    /* -------------------------------------------------- Properties -------------------------------------------------- */
    
    /**
     * Stores the properties of this concept.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableList<ConceptProperty<?, C, E>> properties = FreezableLinkedList.get();
    
    /**
     * Registers the given property at this concept.
     * 
     * @param property the property to be registered.
     * 
     * @require property.getConcept() == this : "The given property belongs to this concept.";
     */
    public void register(@Nonnull ConceptProperty<?, C, E> property) {
        Require.that(property.getConcept() == this).orThrow("The given property belongs to this concept.");
        
        properties.add(property);
    }
    
    /**
     * Returns the properties of this concept.
     * 
     * @return the properties of this concept.
     */
    @Pure
    public final @Nonnull @NonNullableElements ReadOnlyList<ConceptProperty<?, C, E>> getProperties() {
        return properties;
    }
    
    /**
     * Returns the property of this concept with the given table.
     * 
     * @return the property of this concept with the given table.
     */
    @Pure
    public final @Nonnull ConceptProperty<?, C, E> getProperty(@Nonnull ConceptPropertyTable<?, C, E> table) throws DatabaseException { // TODO: Change the parameter to ConceptPropertySetup!
        for (final @Nonnull ConceptProperty<?, C, E> property : properties) {
            if (property.getConceptPropertySetup().getPropertyTable().equals(table)) { return property; }
        }
        throw DatabaseException.get("No property is registered for the given table.");
    }
    
    /**
     * Resets the property of this concept with the given table.
     * 
     * @param table the table which initiated the reset of its properties.
     */
    @Locked
    @NonCommitting
    public void reset(@Nonnull ConceptPropertyTable<?, C, E> table) throws DatabaseException {
        getProperty(table).reset();
    }
    
    /**
     * Resets the properties of this concept.
     */
    @Locked
    @NonCommitting
    public void resetAll() throws DatabaseException {
        for (final @Nonnull ConceptProperty<?, C, E> property : properties) { property.reset(); }
    }
    
    /* -------------------------------------------------- Factory -------------------------------------------------- */
    
    /**
     * The factory for concepts.
     */
    @Immutable
    protected static abstract class Factory<C extends Concept<C, E, K>, E extends Entity, K> {
        
        /**
         * Returns a new instance of the generic concept class.
         * 
         * @param entity the entity to which the concept belongs.
         * @param key the key which denotes the returned concept.
         * 
         * @return a new instance of the generic concept class.
         */
        @Pure
        public abstract @Nonnull C create(@Nonnull E entity, @Nonnull K key);
        
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
        
    @Pure
    @Override
    public final @Nonnull ConceptXDFConverter<C, E, K> getXDFConverter() {
        return setup.getXDFConverter();
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull ConceptSQLConverter<C, E, K> getSQLConverter() {
        return setup.getSQLConverter();
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null) { return false; }
        if (!object.getClass().equals(getClass())) { return false; }
        @SuppressWarnings("rawtypes")
        final @Nonnull Concept<?, ?, ?> other = (Concept) object;
        return this.getEntity().equals(other.getEntity()) && this.getKey().equals(other.getKey());
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return 41 * getEntity().hashCode() + getKey().hashCode();
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return "The " + getClass().getSimpleName() + " of " + getEntity().getIdentity().getAddress() + ".";
    }
    
}
