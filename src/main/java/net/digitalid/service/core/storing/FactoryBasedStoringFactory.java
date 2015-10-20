package net.digitalid.service.core.storing;

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

/**
 * This class implements a global factory that is based on another global factory.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
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
     * Creates a new factory based storable factory with the given parameters.
     * 
     * @param keyFactory the factory used to store and restore the object's key.
     */
    protected FactoryBasedStoringFactory(@Nonnull AbstractStoringFactory<K, E> keyFactory) {
        super(keyFactory.getColumns().toArray());
        
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
