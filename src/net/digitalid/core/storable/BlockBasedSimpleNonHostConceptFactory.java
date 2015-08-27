package net.digitalid.core.storable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Capturable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.collections.FreezableArray;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.Database;
import net.digitalid.core.database.SQLType;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This class implements the methods that all simple non-host concept classes whose storable mechanisms use a {@link Block block} share.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class BlockBasedSimpleNonHostConceptFactory<O extends Storable<O>> extends SimpleNonHostConceptFactory<O> {
    
    /**
     * Stores the column of this factory.
     */
    private static final @Nonnull Column COLUMN = Column.get("block", SQLType.BLOB);
    
    /**
     * Creates a new factory with the given type.
     * 
     * @param type the semantic type that corresponds to the storable class.
     */
    protected BlockBasedSimpleNonHostConceptFactory(@Nonnull @Loaded SemanticType type) {
        super(type, COLUMN);
    }
    
    @Pure
    @Override
    protected final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return FreezableArray.getNonNullable(Block.fromNonNullable(object).toString());
    }
    
    @Override
    @NonCommitting
    public final void setNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        Database.setNonNullable(Block.fromNonNullable(object), preparedStatement, parameterIndex);
    }
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O getNullable(@Nonnull NonHostEntity entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            final @Nullable Block block = Block.FACTORY.getNullable(resultSet, columnIndex);
            return block == null ? null : decodeNonNullable(entity, block.setType(getType()));
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("Could not decode a block from the database.", exception);
        }
    }
    
}
