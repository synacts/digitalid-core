package net.digitalid.core.conversion.wrappers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.Store;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.exceptions.operation.FailedValueRestoringException;
import net.digitalid.database.core.exceptions.operation.FailedValueStoringException;
import net.digitalid.database.core.exceptions.state.value.MaskingCorruptValueException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.service.core.converter.xdf.Encode;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.Loaded;

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
        public final void storeNonNullable(@Nonnull W wrapper, @NonCapturable @Nonnull ValueCollector collector) throws FailedValueStoringException {
            Store.nonNullable(Encode.nonNullable(wrapper), preparedStatement, parameterIndex);
        }
        
        @Pure
        @Override
        @NonCommitting
        public final @Nullable W restoreNullable(@Nonnull Object none, @NonCapturable @Nonnull SelectionResult result) throws FailedValueRestoringException, CorruptValueException, InternalException {
            try {
                final @Nullable Block block = Block.SQL_CONVERTER.restoreNullable(getType(), resultSet, columnIndex);
                return block == null ? null : XDFConverter.decodeNonNullable(none, block);
            } catch (@Nonnull DatabaseException | NetworkException | ExternalException | RequestException exception) {
                throw MaskingCorruptValueException.get(exception);
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
        return Encode.nonNullable((W) this).toString();
    }
    
}
