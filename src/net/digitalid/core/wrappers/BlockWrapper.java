package net.digitalid.core.wrappers;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Column;
import net.digitalid.core.database.Database;
import net.digitalid.core.database.SQLType;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.SemanticType;

/**
 * This class implements methods that all wrappers whose storable mechanisms use a {@link Block block} share.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
public abstract class BlockWrapper<W extends BlockWrapper<W>> extends Wrapper<W> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new block wrapper with the given semantic type.
     * 
     * @param semanticType the semantic type of the new wrapper.
     * 
     * @require semanticType.isBasedOn(getSyntacticType()) : "The given semantic type is based on the indicated syntactic type.";
     */
    protected BlockWrapper(@Nonnull @Loaded SemanticType semanticType) {
        super(semanticType);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for block wrappers.
     */
    @Immutable
    public abstract static class Factory<W extends Wrapper<W>> extends Wrapper.Factory<W> {
        
        /**
         * Stores the column for the wrapper.
         */
        private static final @Nonnull Column COLUMN = Column.get("block", SQLType.BLOB);
        
        /**
         * Creates a new factory with the given type.
         * 
         * @param type the semantic type of the wrapper.
         */
        protected Factory(@Nonnull @Loaded SemanticType type) {
            super(type, COLUMN);
        }
        
        @Override
        @NonCommitting
        public final void setNonNullable(@Nonnull W wrapper, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            Database.setNonNullable(Block.fromNonNullable(wrapper), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public final @Nullable W getNullable(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            try {
                final @Nullable Block block = Block.FACTORY.getNullable(resultSet, columnIndex);
                return block == null ? null : decodeNonNullable(block.setType(getType()));
            } catch (@Nonnull IOException | PacketException | ExternalException exception) {
                throw new SQLException("Could not decode a block from the database.", exception);
            }
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull Factory<W> getFactory();
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public final @Nonnull String toString() {
        return Block.fromNonNullable((W) this).toString();
    }
    
}
