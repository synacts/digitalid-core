package net.digitalid.service.core.concept;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.annotations.OnlyForClients;
import net.digitalid.service.core.annotations.OnlyForHosts;
import net.digitalid.service.core.encoding.AbstractEncodingFactory;
import net.digitalid.service.core.encoding.Encodable;
import net.digitalid.service.core.encoding.FactoryBasedEncodingFactory;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.property.ConceptProperty;
import net.digitalid.service.core.property.ConceptPropertyTable;
import net.digitalid.service.core.storing.AbstractStoringFactory;
import net.digitalid.service.core.storing.FactoryBasedStoringFactory;
import net.digitalid.service.core.storing.Storable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableLinkedList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class models a concept in the {@link Database database}.
 * A concept always belongs to an {@link Entity entity}.
 * 
 * @param <C> the type of the concept class that extends this class.
 * @param <E> either {@link Entity} for a general concept or {@link NonHostEntity} for a concept that exists only for non-hosts.
 *            (The type has to be a supertype of {@link NonHostEntity}, which cannot be declared in Java, unfortunately!)
 * @param <K> the type of the key which identifies an instance among all instances of a concept at the same entity.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
public abstract class Concept<C extends Concept<C, E, K>, E extends Entity<E>, K> implements Encodable<C, E>, Storable<C, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Entity –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– On Host –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    @OnlyForHosts
    public final @Nonnull Account getAccount() {
        assert isOnHost() : "This concept is on a host.";
        
        return (Account) entity;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– On Client –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    @OnlyForClients
    public final @Nonnull Role getRole() {
        assert isOnClient(): "This concept is on a client.";
        
        return (Role) entity;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Key –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new concept with the given entity.
     * 
     * @param entity the entity to which the new concept belongs.
     * @param key the key which identifies the new concept.
     */
    protected Concept(@Nonnull E entity, @Nonnull K key) {
        this.entity = entity;
        this.key = key;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Properties –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the properties of this concept.
     */
    private final @Nonnull @NonNullableElements @NonFrozen FreezableList<ConceptProperty<C>> properties = FreezableLinkedList.get();
    
    /**
     * Registers the given property at this concept.
     * 
     * @param property the property to be registered.
     * 
     * @require property.getConcept() == this : "The given property belongs to this concept.";
     */
    public void register(@Nonnull ConceptProperty<C> property) {
        assert property.getConcept() == this : "The given property belongs to this concept.";
        
        properties.add(property);
    }
    
    /**
     * Returns the properties of this concept.
     * 
     * @return the properties of this concept.
     */
    @Pure
    public final @Nonnull @NonNullableElements ReadOnlyList<ConceptProperty<C>> getProperties() {
        return properties;
    }
    
    /**
     * Returns the property of this concept with the given table.
     * 
     * @return the property of this concept with the given table.
     */
    @Pure
    public final @Nonnull ConceptProperty<C> getProperty(@Nonnull ConceptPropertyTable<?, C, E> table) throws SQLException {
        for (final @Nonnull ConceptProperty<C> property : properties) {
            if (property.getTable().equals(table)) return property;
        }
        throw new SQLException("No property is registered for the given table.");
    }
    
    /**
     * Resets the property of this concept with the given table.
     * 
     * @param table the table which initiated the reset of its properties.
     */
    @Locked
    @NonCommitting
    public void reset(@Nonnull ConceptPropertyTable<?, C, E> table) throws SQLException {
        getProperty(table).reset();
    }
    
    /**
     * Resets the properties of this concept.
     */
    @Locked
    @NonCommitting
    public void resetAll() throws SQLException {
        for (final @Nonnull ConceptProperty<C> property : properties) property.reset();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for concepts.
     */
    @Immutable
    protected static abstract class Factory<C extends Concept<C, E, K>, E extends Entity<E>, K> {
        
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
        
    /**
     * The encoding factory for concepts.
     */
    @Immutable
    public static abstract class EncodingFactory<C extends Concept<C, E, K>, E extends Entity<E>, K> extends FactoryBasedEncodingFactory<C, E, K> {
        
        /**
         * Stores the index that caches existing concepts.
         */
        private final @Nonnull Index<C, E, K> index;
        
        /**
         * Returns the index that caches existing concepts.
         * 
         * @return the index that caches existing concepts.
         */
        @Pure
        public final @Nonnull Index<C, E, K> getIndex() {
            return index;
        }
        
        /**
         * Creates a new encoding factory based on the given key factory.
         * 
         * @param type the semantic type that corresponds to the encoding class.
         * @param keyFactory the factory to encode and decode the key.
         * @param index the index that caches existing concepts.
         */
        protected EncodingFactory(@Nonnull @Loaded SemanticType type, @Nonnull AbstractEncodingFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
            super(type, keyFactory);
            
            this.index = index;
        }
        
        /**
         * Creates a new encoding factory based on the given key factory.
         * 
         * @param keyFactory the factory to encode and decode the key.
         * @param index the index that caches existing concepts.
         */
        protected EncodingFactory(@Nonnull AbstractEncodingFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
            super(keyFactory);
            
            this.index = index;
        }
        
        @Pure
        @Override
        public final @Nonnull K getKey(@Nonnull C concept) {
            return concept.getKey();
        }
        
        @Pure
        @Override
        public final @Nonnull C getObject(@Nonnull E entity, @Nonnull K key) {
            return index.get(entity, key);
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull EncodingFactory<C, E, K> getEncodingFactory();

    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for concepts.
     */
    @Immutable
    public static abstract class StoringFactory<C extends Concept<C, E, K>, E extends Entity<E>, K> extends FactoryBasedStoringFactory<C, E, K> {
        
        /**
         * Stores the index that caches existing concepts.
         */
        private final @Nonnull Index<C, E, K> index;
        
        /**
         * Returns the index that caches existing concepts.
         * 
         * @return the index that caches existing concepts.
         */
        @Pure
        public final @Nonnull Index<C, E, K> getIndex() {
            return index;
        }
        
        /**
         * Creates a new storing factory based on the given key factory.
         * 
         * @param keyFactory the factory to store and restore the key.
         * @param index the index that caches existing concepts.
         */
        protected StoringFactory(@Nonnull AbstractStoringFactory<K, E> keyFactory, @Nonnull Index<C, E, K> index) {
            super(keyFactory);
            
            this.index = index;
        }
        
        @Pure
        @Override
        public final @Nonnull K getKey(@Nonnull C concept) {
            return concept.getKey();
        }
        
        @Pure
        @Override
        public final @Nonnull C getObject(@Nonnull E entity, @Nonnull K key) {
            return index.get(entity, key);
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull StoringFactory<C, E, K> getStoringFactory();

}
