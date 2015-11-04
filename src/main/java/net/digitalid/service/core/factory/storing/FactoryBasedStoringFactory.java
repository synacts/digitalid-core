package net.digitalid.service.core.factory.storing;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.storing.AbstractStoringFactory;

/**
 * This class implements a storing factory that is based on another storing factory.
 * 
 * @param <O> the type of the objects that this factory can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an entity.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other factory stores and restores (usually as a key for the objects of this factory).
 */
@Immutable
public abstract class FactoryBasedStoringFactory<O, E, K> extends AbstractStoringFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factory –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to store and restore the key.
     */
    private final @Nonnull AbstractStoringFactory<K, E> keyFactory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new factory based storing factory with the given key factory.
     * 
     * @param keyFactory the factory used to store and restore the object's key.
     */
    protected FactoryBasedStoringFactory(@Nonnull AbstractStoringFactory<K, E> keyFactory) {
        super(keyFactory.getColumns());
        
        this.keyFactory = keyFactory;
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
     * @param entity the entity needed to restore the object.
     * @param key the key which denotes the returned object.
     * 
     * @return the object with the given key.
     */
    @Pure
    public abstract @Nonnull O getObject(@Nonnull E entity, @Nonnull K key);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return keyFactory.getValues(getKey(object));
    }
    
    @Override
    @NonCommitting
    public final void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        keyFactory.storeNonNullable(getKey(object), preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O restoreNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable K key = keyFactory.restoreNullable(entity, resultSet, columnIndex);
        return key == null ? null : getObject(entity, key);
    }
    
}
