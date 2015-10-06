package net.digitalid.core.factory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.annotations.reference.Capturable;
import net.digitalid.annotations.state.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.database.annotations.NonCommitting;
import net.digitalid.collections.annotations.freezable.NonFrozen;
import net.digitalid.core.annotations.NonNullableElements;
import net.digitalid.annotations.state.Pure;
import net.digitalid.core.auxiliary.None;
import net.digitalid.collections.freezable.FreezableArray;
import net.digitalid.database.column.Column;
import net.digitalid.database.column.SQLType;
import net.digitalid.database.configuration.Database;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.wrappers.Block;

/**
 * This class implements the methods that all global factories which store their data as a {@link Block block} in the {@link Database database} share.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class BlockBasedGlobalFactory<O extends Storable<O, E>, E> extends GlobalFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Column –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the column of this factory.
     */
    private static final @Nonnull Column COLUMN = Column.get("block", SQLType.BLOB);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new factory with the given type.
     * 
     * @param type the semantic type that corresponds to the storable class.
     */
    protected BlockBasedGlobalFactory(@Nonnull @Loaded SemanticType type) {
        super(type, COLUMN);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return FreezableArray.getNonNullable(Block.fromNonNullable(object).toString());
    }
    
    @Override
    @NonCommitting
    public final void setNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        Database.setNonNullable(Block.fromNonNullable(object), preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O getNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            final @Nullable Block block = Block.FACTORY.getNullable(None.OBJECT, resultSet, columnIndex);
            return block == null ? null : decodeNonNullable(entity, block.setType(getType()));
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("Could not decode a block from the database.", exception);
        }
    }
    
}
