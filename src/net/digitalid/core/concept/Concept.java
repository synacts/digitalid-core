package net.digitalid.core.concept;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Locked;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.OnlyForClients;
import net.digitalid.core.annotations.OnlyForHosts;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableLinkedList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadOnlyList;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.property.ConceptProperty;
import net.digitalid.core.storable.FactoryBasedGlobalFactory;
import net.digitalid.core.storable.GlobalFactory;
import net.digitalid.core.storable.LocalFactory;
import net.digitalid.core.storable.Storable;
import net.digitalid.core.wrappers.Block;

/**
 * This class models a concept in the {@link Database database}.
 * A concept always belongs to an {@link Entity entity}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class Concept<C extends Concept<C, E, K>, E extends Entity, K> implements Storable<C, E> {
    
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
     */
    public void register(@Nonnull ConceptProperty<C> property) {
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
     * Resets the properties of this concept.
     */
    @Locked
    @NonCommitting
    public void reset() throws SQLException {
        for (final @Nonnull ConceptProperty<C> property : properties) property.reset();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The global factory for concepts.
     */
    @Immutable
    public static abstract class IndexBasedGlobalFactory<C extends Concept<C, E, K>, E extends Entity, K> extends FactoryBasedGlobalFactory<C, E, K> {
        
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
         * Creates a new concept factory based on the given factory.
         * 
         * @param factory the factory to store and restore the key.
         * @param index the index that caches existing concepts.
         */
        protected IndexBasedGlobalFactory(@Nonnull GlobalFactory<K, E> factory, @Nonnull Index<C, E, K> index) {
            super(factory);
            
            this.index = index;
        }
        
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
    
    /**
     * The local factory for concepts.
     */
    @Immutable
    public static abstract class IndexBasedLocalFactory<C extends Concept<C, E, K>, E extends Entity, K> extends IndexBasedGlobalFactory<C, E, K> {
        
        /**
         * Stores the factory to store and restore the key.
         */
        private final @Nonnull LocalFactory<K, E> factory;
        
        /**
         * Creates a new concept factory based on the given key factory.
         * 
         * @param factory the factory to store and restore the key.
         * @param index the index that caches existing concepts.
         */
        protected IndexBasedLocalFactory(@Nonnull LocalFactory<K, E> factory, @Nonnull Index<C, E, K> index) {
            super(factory, index);
            
            this.factory = factory;
        }
        
        @Pure
        @Override
        public final @Nonnull C decodeNonNullable(@Nonnull E entity, @Nonnull Block block) throws InvalidEncodingException {
            return getIndex().get(entity, factory.decodeNonNullable(entity, block));
        }
        
        @Pure
        @Override
        public final @Nullable C decodeNullable(@Nonnull E entity, @Nullable Block block) throws InvalidEncodingException {
            if (block != null) return decodeNonNullable(entity, block);
            else return null;
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull IndexBasedGlobalFactory<C, E, K> getFactory();
    
}
