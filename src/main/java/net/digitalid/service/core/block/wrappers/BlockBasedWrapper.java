package net.digitalid.service.core.block.wrappers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.ConvertToXDF;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.internal.InternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.index.MutableIndex;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.ConvertToSQL;
import net.digitalid.utility.database.exceptions.DatabaseException;

/**
 * This class implements methods that all wrappers whose storable mechanisms use a {@link Block block} share.
 */
@Immutable
public abstract class BlockBasedWrapper<W extends BlockBasedWrapper<W>> extends AbstractWrapper<W> {
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * The SQL converter for block-based wrappers.
     */
    @Immutable
    public final static class SQLConverter<W extends BlockBasedWrapper<W>> extends AbstractWrapper.SQLConverter<W> {
        
        /**
         * Stores the XDF converter used to encode and decode the block.
         */
        private final @Nonnull AbstractWrapper.XDFConverter<W> XDFConverter;
        
        /**
         * Creates a new SQL converter with the given XDF converter.
         * 
         * @param XDFConverter the XDF converter used to encode and decode the block.
         */
        protected SQLConverter(@Nonnull AbstractWrapper.XDFConverter<W> XDFConverter) {
            super(Block.DECLARATION, XDFConverter.getType());
            
            this.XDFConverter = XDFConverter;
        }
        
        @Override
        @NonCommitting
        public final void storeNonNullable(@Nonnull W wrapper, @Nonnull PreparedStatement preparedStatement, @Nonnull MutableIndex parameterIndex) throws SQLException {
            ConvertToSQL.nonNullable(ConvertToXDF.nonNullable(wrapper), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public final @Nullable W restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, @Nonnull MutableIndex columnIndex) throws SQLException {
            try {
                final @Nullable Block block = Block.SQL_CONVERTER.restoreNullable(getType(), resultSet, columnIndex);
                return block == null ? null : XDFConverter.decodeNonNullable(none, block);
            } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
                throw new SQLException("Could not decode a block from the database.", exception);
            }
        }
        
    }
    
    @Pure
    @Override
    public abstract @Nonnull SQLConverter<W> getSQLConverter();
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public final @Nonnull String toString() {
        return ConvertToXDF.nonNullable((W) this).toString();
    }
    
}
