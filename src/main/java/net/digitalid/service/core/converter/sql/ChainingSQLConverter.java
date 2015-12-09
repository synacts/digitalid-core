package net.digitalid.service.core.converter.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.AbstractSQLConverter;
import net.digitalid.database.core.declaration.Declaration;
import net.digitalid.database.core.exceptions.operation.FailedValueRestoringException;
import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;
import net.digitalid.database.core.exceptions.state.CorruptStateException;
import net.digitalid.database.core.exceptions.state.MaskingCorruptStateException;
import net.digitalid.database.core.exceptions.state.value.CorruptParameterValueException;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.entity.annotations.Matching;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.utility.annotations.reference.NonCapturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.system.exceptions.InternalException;

/**
 * This class implements an SQL converter that is based on another SQL converter.
 * 
 * @param <O> the type of the objects that this converter can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 * @param <K> the type of the objects that the other converter stores and restores (as a key for this converter's objects).
 * @param <D> the type of the external object that is needed to recover the key, which is quite often an {@link Entity}.
 *            In case no external information is needed for the recovery of the key, declare it as an {@link Object}.
 * 
 * @see XDFConverterBasedSQLConverter
 */
@Immutable
public class ChainingSQLConverter<O, E, K, D> extends AbstractSQLConverter<O, E> {
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * Stores the key converter used to convert and recover the object.
     */
    private final @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> keyConverter;
    
    /**
     * Returns the key converter used to convert and recover the object.
     * 
     * @return the key converter used to convert and recover the object.
     */
    @Pure
    public final @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> getKeyConverter() {
        return keyConverter;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the SQL converter used to store and restore the object's key.
     */
    private final @Nonnull AbstractSQLConverter<K, ? super D> SQLConverter;
    
    /**
     * Returns the SQL converter used to store and restore the object's key.
     * 
     * @return the SQL converter used to store and restore the object's key.
     */
    @Pure
    public final @Nonnull AbstractSQLConverter<K, ? super D> getSQLConverter() {
        return SQLConverter;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new chaining SQL converter with the given converters.
     * 
     * @param declaration the declaration of the new chaining SQL converter.
     * @param keyConverter the key converter used to convert and recover the object.
     * @param SQLConverter the SQL converter used to store and restore the object's key.
     */
    protected ChainingSQLConverter(@Nonnull @Matching Declaration declaration, @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull AbstractSQLConverter<K, ? super D> SQLConverter) {
        super(declaration);
        
        assert declaration.matches(SQLConverter.getDeclaration()) : "The declaration matches the declaration of the SQL converter.";
        
        this.keyConverter = keyConverter;
        this.SQLConverter = SQLConverter;
    }
    
    /**
     * Creates a new chaining SQL converter with the given converters.
     * 
     * @param declaration the declaration of the new chaining SQL converter.
     * @param keyConverter the key converter used to convert and recover the object.
     * @param SQLConverter the SQL converter used to store and restore the object's key.
     * 
     * @return a new chaining SQL converter with the given converters.
     */
    @Pure
    public static @Nonnull <O, E, K, D> ChainingSQLConverter<O, E, K, D> get(@Nonnull @Matching Declaration declaration, @Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull AbstractSQLConverter<K, ? super D> SQLConverter) {
        return new ChainingSQLConverter<>(declaration, keyConverter, SQLConverter);
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
    public static @Nonnull <O, E, K, D> ChainingSQLConverter<O, E, K, D> get(@Nonnull AbstractNonRequestingKeyConverter<O, ? super E, K, D> keyConverter, @Nonnull AbstractSQLConverter<K, ? super D> SQLConverter) {
        return new ChainingSQLConverter<>(SQLConverter.getDeclaration(), keyConverter, SQLConverter);
    }
    
    /* -------------------------------------------------- Storing (with Statement) -------------------------------------------------- */
    
    @Override
    public final void storeNonNullable(@Nonnull O object, @NonCapturable @Nonnull @NonFrozen FreezableArray<String> values, @Nonnull MutableIndex index) {
        SQLConverter.storeNonNullable(keyConverter.convert(object), values, index);
    }
    
    /* -------------------------------------------------- Storing (with PreparedStatement) -------------------------------------------------- */
    
    @Override
    @NonCommitting
    public final void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws FailedValueStoringException {
        SQLConverter.storeNonNullable(keyConverter.convert(object), preparedStatement, parameterIndex);
    }
    
    /* -------------------------------------------------- Restoring -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O restoreNullable(@Nonnull E external, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws FailedValueRestoringException, CorruptStateException, InternalException {
        final @Nullable K key = SQLConverter.restoreNullable(keyConverter.decompose(external), resultSet, columnIndex);
        if (key == null) { return null; }
        if (!keyConverter.isValid(key)) { throw CorruptParameterValueException.get("key", key); }
        try {
            return keyConverter.recover(external, key);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw MaskingCorruptStateException.get(exception);
        }
    }
    
}
