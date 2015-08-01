package net.digitalid.core.storable;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonEncoding;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Column;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public abstract class GeneralConceptFactory<O> extends NonHostConceptFactory<O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the given non-nullable block.
     * 
     * @param entity the entity to which the block belongs.
     * @param block the non-nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the non-nullable block.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    @Pure
    public abstract @Nonnull O decodeNonNullable(@Nonnull Entity entity, @Nonnull @NonEncoding Block block) throws InvalidEncodingException;
    
    @Pure
    @Override
    public final @Nonnull O decodeNonNullable(@Nonnull NonHostEntity entity, @Nonnull @NonEncoding Block block) throws InvalidEncodingException {
        return decodeNonNullable((Entity) entity, block);
    }
    
    /**
     * Decodes the given nullable block.
     * 
     * @param entity the entity to which the block belongs.
     * @param block the nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the nullable block.
     * 
     * @require block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    @Pure
    public final @Nullable O decodeNullable(@Nonnull Entity entity, @Nullable @NonEncoding Block block) throws InvalidEncodingException {
        if (block != null) return decodeNonNullable(entity, block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param entity the entity to which the returned object belongs.
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public abstract @Nullable O getNullable(@Nonnull Entity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O getNullable(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return getNullable((Entity) entity, resultSet, columnIndex);
    }
    
    /**
     * Returns a non-nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param entity the entity to which the returned object belongs.
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a non-nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public final @Nonnull O getNonNullable(@Nonnull Entity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable O object = getNullable(entity, resultSet, columnIndex);
        if (object == null) throw new SQLException("An object which should not be null was null.");
        return object;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new general concept factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param columns the columns used to store objects of the storable class.
     */
    protected GeneralConceptFactory(@Nonnull @Loaded SemanticType type, @Nonnull @NonNullableElements Column... columns) {
        super(type, columns);
    }
    
}
