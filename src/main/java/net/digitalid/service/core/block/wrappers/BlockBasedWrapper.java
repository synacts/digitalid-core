package net.digitalid.service.core.block.wrappers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.encoding.Encode;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.database.storing.Store;

/**
 * This class implements methods that all wrappers whose storable mechanisms use a {@link Block block} share.
 */
@Immutable
public abstract class BlockBasedWrapper<W extends BlockBasedWrapper<W>> extends Wrapper<W> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new block-based wrapper with the given semantic type.
     * 
     * @param semanticType the semantic type of the new wrapper.
     * 
     * @require semanticType.isBasedOn(getSyntacticType()) : "The given semantic type is based on the indicated syntactic type.";
     */
    protected BlockBasedWrapper(@Nonnull @Loaded SemanticType semanticType) {
        super(semanticType);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The storing factory for block-based wrappers.
     */
    @Immutable
    public final static class StoringFactory<W extends BlockBasedWrapper<W>> extends Wrapper.StoringFactory<W> {
        
        /**
         * Stores the column for the block-based wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("block", SQLType.BLOB);
        
        /**
         * Stores the encoding factory used to encode and decode the block.
         */
        private final @Nonnull Wrapper.EncodingFactory<W> encodingFactory;
        
        /**
         * Creates a new storing factory with the given encoding factory.
         * 
         * @param encodingFactory the encoding factory used to encode and decode the block.
         */
        protected StoringFactory(@Nonnull Wrapper.EncodingFactory<W> encodingFactory) {
            super(COLUMN, encodingFactory.getType());
            
            this.encodingFactory = encodingFactory;
        }
        
        @Override
        @NonCommitting
        public final void storeNonNullable(@Nonnull W wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            Store.nonNullable(Encode.nonNullable(wrapper), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public final @Nullable W restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            try {
                final @Nullable Block block = Block.STORING_FACTORY.restoreNullable(getType(), resultSet, columnIndex);
                return block == null ? null : encodingFactory.decodeNonNullable(none, block);
            } catch (@Nonnull AbortException | PacketException | ExternalException | NetworkException exception) {
                throw new SQLException("Could not decode a block from the database.", exception);
            }
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull StoringFactory<W> getStoringFactory();
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public final @Nonnull String toString() {
        return Encode.nonNullable((W) this).toString();
    }
    
}
