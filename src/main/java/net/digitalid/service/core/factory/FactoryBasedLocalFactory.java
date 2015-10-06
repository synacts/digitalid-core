package net.digitalid.service.core.factory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class implements a local factory that is based on another local factory.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class FactoryBasedLocalFactory<O, E, K> extends LocalFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to store and restore the key.
     */
    private final @Nonnull LocalFactory<K, E> factory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new factory based local factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param factory the factory used to store and restore the object's key.
     * 
     * @require type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
     */
    protected FactoryBasedLocalFactory(@Nonnull @Loaded SemanticType type, @Nonnull LocalFactory<K, E> factory) {
        super(type, factory.getColumns().toArray());
        
        assert type.isBasedOn(factory.getType()) : "The given type is based on the type of the factory.";
        
        this.factory = factory;
    }
    
    /**
     * Creates a new factory based local factory with the given parameter.
     * 
     * @param factory the factory used to store and restore the object's key.
     */
    protected FactoryBasedLocalFactory(@Nonnull LocalFactory<K, E> factory) {
        this(factory.getType(), factory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Abstract –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the key of the given object.
     * 
     * @param object the object whose key is to be returned.
     * 
     * @return the key of the given object.
     */
    @Pure
    public abstract @Nonnull K getKey(@Nonnull O object);
    
    /**
     * Returns the object with the given key.
     * 
     * @param entity the entity needed to reconstruct the object.
     * @param key the key which denotes the returned object.
     * 
     * @return the object with the given key.
     */
    @Pure
    public abstract @Nonnull O getObject(@Nonnull E entity, @Nonnull K key);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull Block encodeNonNullable(@Nonnull O object) {
        return factory.encodeNonNullable(getKey(object)).setType(getType());
    }
    
    @Pure
    @Override
    public @Nonnull O decodeNonNullable(@Nonnull E entity, @Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(getType()) : "The block is based on the type of this factory.";
        
        return getObject(entity, factory.decodeNonNullable(entity, block));
    }
    
    @Pure
    @Override
    public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return factory.getValues(getKey(object));
    }
    
    @Override
    @NonCommitting
    public final void setNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        factory.setNonNullable(getKey(object), preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O getNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable K key = factory.getNullable(entity, resultSet, columnIndex);
        return key == null ? null : getObject(entity, key);
    }
    
}
