package net.digitalid.service.core.storing;

import net.digitalid.service.core.encoding.AbstractEncodingFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.database.configuration.Database;

/**
 * This class implements the methods that all storable factories which store their data as a {@link Block block} in the {@link Database database} share.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public abstract class BlockBasedStoringFactory<O extends Storable<O, E>, E> extends AbstractStoringFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Column –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the column of this storable factory.
     */
    private static final @Nonnull Column COLUMN = Column.get("block", SQLType.BLOB);
    
    /**
     * Stores the blockable factory used to decode 
     */
    private final @Nonnull AbstractEncodingFactory<O, E> blockableFactory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new factory with the given type.
     * 
     * @param type the semantic type that corresponds to the storable class.
     */
    protected BlockBasedStoringFactory(@Nonnull AbstractEncodingFactory<O, E> blockableFactory) {
        super(COLUMN);
        this.blockableFactory = blockableFactory;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return FreezableArray.getNonNullable(blockableFactory.encodeNonNullable(object).toString());
    }
    
    @Override
    @NonCommitting
    public final void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        Store.nonNullable(blockableFactory.encodeNonNullable(object), preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public final @Nullable O restoreNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            final @Nullable Block block = Block.FACTORY.getNullable(None.OBJECT, resultSet, columnIndex);
            return block == null ? null : blockableFactory.decodeNonNullable(entity, block);
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("Could not decode a block from the database.", exception);
        }
    }
    
}
