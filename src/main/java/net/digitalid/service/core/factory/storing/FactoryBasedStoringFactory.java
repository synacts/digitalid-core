package net.digitalid.service.core.factory.storing;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.factory.object.AbstractNonRequestingObjectFactory;
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
public final class FactoryBasedStoringFactory<O, E, K> extends AbstractStoringFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factory used to transform and reconstruct the object.
     */
    private final @Nonnull AbstractNonRequestingObjectFactory<O, ? super E, K> objectFactory;
    
    /**
     * Stores the factory used to store and restore the key.
     */
    private final @Nonnull AbstractStoringFactory<K, E> keyFactory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new factory-based storing factory with the given factories.
     * 
     * @param objectFactory the factory used to transform and reconstruct the object.
     * @param keyFactory the factory used to store and restore the object's key.
     */
    protected FactoryBasedStoringFactory(@Nonnull AbstractNonRequestingObjectFactory<O, ? super E, K> objectFactory, @Nonnull AbstractStoringFactory<K, E> keyFactory) {
        super(keyFactory.getColumns());
        
        this.objectFactory = objectFactory;
        this.keyFactory = keyFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Methods –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return keyFactory.getValues(objectFactory.getKey(object));
    }
    
    @Override
    @NonCommitting
    public final void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        keyFactory.storeNonNullable(objectFactory.getKey(object), preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O restoreNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable K key = keyFactory.restoreNullable(entity, resultSet, columnIndex);
        if (key == null) return null;
        try {
            if (!objectFactory.isValid(key)) throw new InvalidEncodingException("The restored key '" + key + "' is invalid.");
            return  objectFactory.getObject(entity, key);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException(exception);
        }
    }
    
}
