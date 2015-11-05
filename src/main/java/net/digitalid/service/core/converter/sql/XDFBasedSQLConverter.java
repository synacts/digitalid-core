package net.digitalid.service.core.converter.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
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
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.ConvertToSQL;

/**
 * This class implements the methods that all SQL converters which store their data as a {@link Block block} in the {@link Database database} share.
 * 
 * @param <O> the type of the objects that this converter can store and restore, which is typically the surrounding class.
 * @param <E> the type of the external object that is needed to restore an object, which is quite often an {@link Entity}.
 *            In case no external information is needed for the restoration of an object, declare it as an {@link Object}.
 */
@Immutable
public final class XDFBasedSQLConverter<O, E> extends AbstractSQLConverter<O, E> {
    
    /* -------------------------------------------------- Column -------------------------------------------------- */
    
    /**
     * Stores the column of this SQL converter.
     */
    private static final @Nonnull Column COLUMN = Column.get("block", SQLType.BLOB);
    
    /* -------------------------------------------------- Field -------------------------------------------------- */
    
    /**
     * Stores the XDF converter used to encode and decode the block.
     */
    private final @Nonnull AbstractXDFConverter<O, E> XDFConverter;
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new XDF-based SQL converter with the given XDF converter.
     * 
     * @param XDFConverter the XDF converter used to encode and decode the block.
     */
    private XDFBasedSQLConverter(@Nonnull AbstractXDFConverter<O, E> XDFConverter) {
        super(COLUMN);
        
        this.XDFConverter = XDFConverter;
    }
    
    /**
     * Returns a new XDF-based SQL converter with the given XDF converter.
     * 
     * @param XDFConverter the XDF converter used to encode and decode the block.
     * 
     * @return a new XDF-based SQL converter with the given XDF converter.
     */
    @Pure
    public static @Nonnull <O extends XDF<O, E>, E> XDFBasedSQLConverter<O, E> get(@Nonnull AbstractXDFConverter<O, E> XDFConverter) {
        return new XDFBasedSQLConverter<>(XDFConverter);
    }
    
    /* -------------------------------------------------- Storing -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull O object) {
        return FreezableArray.getNonNullable(XDFConverter.encodeNonNullable(object).toString());
    }
    
    @Override
    @NonCommitting
    public void storeNonNullable(@Nonnull O object, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        ConvertToSQL.nonNullable(XDFConverter.encodeNonNullable(object), preparedStatement, parameterIndex);
    }
    
    /* -------------------------------------------------- Retrieving -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nullable O restoreNullable(@Nonnull E external, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            final @Nullable Block block = Block.SQL_CONVERTER.restoreNullable(XDFConverter.getType(), resultSet, columnIndex);
            return block == null ? null : XDFConverter.decodeNonNullable(external, block);
        } catch (@Nonnull AbortException | PacketException | ExternalException | NetworkException exception) {
            throw new SQLException("Could not decode a block from the database.", exception);
        }
    }
    
}
