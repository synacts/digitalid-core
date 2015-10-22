package net.digitalid.service.core.storing;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.encoding.AbstractEncodingFactory;
import net.digitalid.service.core.encoding.Encodable;
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
 * This class implements the methods that all storing factories which store their data as a {@link Block block} in the {@link Database database} share.
 * 
 * @param <O> the type of the objects that this factory can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public final class BlockBasedStoringFactory<O, E> extends AbstractStoringFactory<O, E> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Column –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the column of this storing factory.
     */
    private static final @Nonnull Column COLUMN = Column.get("block", SQLType.BLOB);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Field –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the encoding factory used to encode and decode the block.
     */
    private final @Nonnull AbstractEncodingFactory<O, E> encodingFactory;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new block-based storing factory with the given encoding factory.
     * 
     * @param encodingFactory the encoding factory used to encode and decode the block.
     */
    private BlockBasedStoringFactory(@Nonnull AbstractEncodingFactory<O, E> encodingFactory) {
        super(COLUMN);
        
        this.encodingFactory = encodingFactory;
    }
    
    /**
     * Returns a new block-based storing factory with the given encoding factory.
     * 
     * @param encodingFactory the encoding factory used to encode and decode the block.
     * 
     * @return a new block-based storing factory with the given encoding factory.
     */
    @Pure
    public static @Nonnull <O extends Encodable<O, E>, E> BlockBasedStoringFactory<O, E> get(@Nonnull AbstractEncodingFactory<O, E> encodingFactory) {
        return new BlockBasedStoringFactory<>(encodingFactory);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storing –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return FreezableArray.getNonNullable(encodingFactory.encodeNonNullable(object).toString());
    }
    
    @Override
    @NonCommitting
    public void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        Store.nonNullable(encodingFactory.encodeNonNullable(object), preparedStatement, parameterIndex);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Retrieving –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public @Nullable O restoreNullable(@Nonnull E entity, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            final @Nullable Block block = Block.FACTORY.getNullable(None.OBJECT, resultSet, columnIndex);
            return block == null ? null : encodingFactory.decodeNonNullable(entity, block);
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("Could not decode a block from the database.", exception);
        }
    }
    
}
