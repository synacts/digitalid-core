package net.digitalid.service.core.converter.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class implements an SQL converter that is based on another SQL converter.
 * 
 * @param <O> the type of the objects that this converter can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other converter stores and restores (usually as a key for the objects of this converter).
 */
@Immutable
public final class ChainingSQLConverter<O, E, K> extends AbstractSQLConverter<O, E> {
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the key converter used to convert and recover the object.
     */
    private final @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K> keyConverter;
    
    /**
     * Stores the SQL converter used to store and restore the object's key.
     */
    private final @Nonnull AbstractSQLConverter<K, E> SQLConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new chaining SQL converter with the given converters.
     * 
     * @param keyConverter the key converter used to convert and recover the object.
     * @param SQLConverter the SQL converter used to store and restore the object's key.
     */
    private ChainingSQLConverter(@Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K> keyConverter, @Nonnull AbstractSQLConverter<K, E> SQLConverter) {
        super(SQLConverter.getColumns());
        
        this.keyConverter = keyConverter;
        this.SQLConverter = SQLConverter;
    }
    
    /**
     * Creates a new chaining SQL converter with the given converters.
     * 
     * @param keyConverter the key converter used to convert and recover the object.
     * @param SQLConverter the SQL converter used to store and restore the object's key.
     * 
     * @return a new chaining SQL converter with the given converters.
     */
    @Pure
    public static @Nonnull <O, E, K> ChainingSQLConverter<O, E, K> get(@Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K> keyConverter, @Nonnull AbstractSQLConverter<K, E> SQLConverter) {
        return new ChainingSQLConverter<>(keyConverter, SQLConverter);
    }
    
    /* -------------------------------------------------- Methods -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return SQLConverter.getValues(keyConverter.convert(object));
    }
    
    @Override
    @NonCommitting
    public final void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        SQLConverter.storeNonNullable(keyConverter.convert(object), preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O restoreNullable(@Nonnull E external, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable K key = SQLConverter.restoreNullable(external, resultSet, columnIndex);
        if (key == null) { return null; }
        try {
            if (!keyConverter.isValid(key)) { throw new InvalidEncodingException("The restored key '" + key + "' is invalid."); }
            return  keyConverter.recover(external, key);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException(exception);
        }
    }
    
}
