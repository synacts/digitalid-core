package net.digitalid.core.storable;

import java.io.IOException;
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
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This factory allows to store and restore objects of non-concepts.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public abstract class NonConceptFactory<O> extends GeneralConceptFactory<O> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Decoding –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Decodes the given non-nullable block.
     * 
     * @param block the non-nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the non-nullable block.
     * 
     * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
     */
    @Pure
    @NonCommitting
    public abstract @Nonnull O decodeNonNullable(@Nonnull @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException;
    
    @Pure
    @Override
    @NonCommitting
    public final @Nonnull O decodeNonNullable(@Nonnull Entity entity, @Nonnull @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException  {
        return decodeNonNullable(block);
    }
    
    /**
     * Decodes the given nullable block.
     * 
     * @param block the nullable block which is to be decoded.
     * 
     * @return the object that was encoded in the nullable block.
     * 
     * @require block == null || block.getType().isBasedOn(getType()) : "The block is either null or based on the indicated type.";
     */
    @Pure
    @NonCommitting
    public final @Nullable O decodeNullable(@Nullable @NonEncoding Block block) throws SQLException, IOException, PacketException, ExternalException {
        if (block != null) return decodeNonNullable(block);
        else return null;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns a nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public abstract @Nullable O getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException;
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O getNullable(@Nonnull Entity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return getNullable(resultSet, columnIndex);
    }
    
    /**
     * Returns a non-nullable object from the given columns of the result set.
     * The number of columns that are read is given by {@link #getNumberOfColumns()}.
     * 
     * @param resultSet the result set from which the data is to be retrieved.
     * @param columnIndex the starting index of the columns containing the data.
     * 
     * @return a non-nullable object from the given columns of the result set.
     */
    @Pure
    @NonCommitting
    public final @Nonnull O getNonNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nullable O object = getNullable(resultSet, columnIndex);
        if (object == null) throw new SQLException("An object which should not be null was null.");
        return object;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new non-concept factory with the given parameters.
     * 
     * @param type the semantic type that corresponds to the storable class.
     * @param columns the columns used to store objects of the storable class.
     */
    protected NonConceptFactory(@Nonnull @Loaded SemanticType type, @Nonnull @NonNullableElements Column... columns) {
        super(type, columns);
    }
    
}
