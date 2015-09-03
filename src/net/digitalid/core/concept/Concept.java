package net.digitalid.core.concept;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.OnlyForClients;
import net.digitalid.core.annotations.OnlyForHosts;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.storable.SimpleFactory;
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for concepts.
     */
    @Immutable
    public static abstract class Factory<C extends Concept<C, E, K>, E extends Entity, K> extends SimpleFactory<C, E> {
        
        /**
         * Stores the factory to store and restore the key.
         */
        private final @Nonnull SimpleFactory<K, E> factory;
        
        /**
         * Creates a new concept factory based on the given key factory.
         * 
         * @param factory the factory to store and restore the key.
         */
        protected Factory(@Nonnull SimpleFactory<K, E> factory) {
            super(factory.getType(), factory.getColumns().toArray());
            
            this.factory = factory;
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
        public @Nonnull Block encodeNonNullable(@Nonnull C concept) {
            return factory.encodeNonNullable(concept.getKey());
        }
        
        @Pure
        @Override
        public @Nonnull C decodeNonNullable(@Nonnull E entity, @Nonnull Block block) throws InvalidEncodingException {
            return create(entity, factory.decodeNonNullable(entity, block));
        }
        
        @Pure
        @Override
        public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull C concept) {
            return factory.getValues(concept.getKey());
        }
        
        @Override
        @NonCommitting
        public void setNonNullable(@Nonnull C concept, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            factory.setNonNullable(concept.getKey(), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable C getNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final @Nullable K key = factory.getNullable(entity, resultSet, columnIndex);
            return key == null ? null : create(entity, key);
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull Factory<C, E, K> getFactory();
    
}
