package net.digitalid.service.core.identity.resolution;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.Int8Wrapper;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.reference.Capturable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.elements.NonNullableElements;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.annotations.freezable.NonFrozen;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.column.Column;
import net.digitalid.utility.database.column.SQLType;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.converter.SQL;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class enumerates the various categories of digital identities.
 */
@Immutable
public enum Category implements XDF<Category, Object>, SQL<Category, Object> {
    
    /* -------------------------------------------------- Categories -------------------------------------------------- */
    
    /**
     * The category for a host.
     */
    HOST(0),
    
    /**
     * The category for a syntactic type.
     */
    SYNTACTIC_TYPE(1),
    
    /**
     * The category for a semantic type.
     */
    SEMANTIC_TYPE(2),
    
    /**
     * The category for a natural person.
     */
    NATURAL_PERSON(3),
    
    /**
     * The category for an artificial person.
     */
    ARTIFICIAL_PERSON(4),
    
    /**
     * The category for an email person.
     */
    EMAIL_PERSON(5),
    
    /**
     * The category for a mobile person.
     */
    MOBILE_PERSON(6);
    
    /* -------------------------------------------------- Empty List -------------------------------------------------- */
    
    /**
     * Stores an empty list of categories that can be shared among semantic types.
     * (This declaration may not be in the semantic type class as the initialization would be too late.)
     */
    public static final @Nonnull @Frozen ReadOnlyList<Category> NONE = FreezableArrayList.<Category>getWithCapacity(0).freeze();
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given value is a valid category.
     *
     * @param value the value to check.
     * 
     * @return whether the given value is a valid category.
     */
    @Pure
    public static boolean isValid(byte value) {
        return value >= 0 && value <= 6;
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the byte representation of this category.
     */
    private final @Validated byte value;
    
    /**
     * Returns the byte representation of this category.
     * 
     * @return the byte representation of this category.
     */
    @Pure
    public @Validated byte getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new category with the given value.
     * 
     * @param value the value encoding the category.
     */
    private Category(@Validated int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the category encoded by the given value.
     * 
     * @param value the value encoding the category.
     * 
     * @return the category encoded by the given value.
     */
    @Pure
    public static @Nonnull Category get(@Validated byte value) {
        assert isValid(value) : "The value is a valid category.";
        
        for (final @Nonnull Category category : values()) {
            if (category.value == value) return category;
        }
        
        throw new ShouldNeverHappenError("The value '" + value + "' does not encode a category.");
    }
    
    /* -------------------------------------------------- Queries -------------------------------------------------- */
    
    /**
     * Returns whether this category denotes a type.
     * 
     * @return whether this category denotes a type.
     */
    @Pure
    public boolean isType() {
        return this == SYNTACTIC_TYPE || this == SEMANTIC_TYPE;
    }
    
    /**
     * Returns whether this category denotes an internal person.
     * 
     * @return whether this category denotes an internal person.
     */
    @Pure
    public boolean isInternalPerson() {
        return this == NATURAL_PERSON || this == ARTIFICIAL_PERSON;
    }
    
    /**
     * Returns whether this category denotes an external person.
     * 
     * @return whether this category denotes an external person.
     */
    @Pure
    public boolean isExternalPerson() {
        return this == EMAIL_PERSON || this == MOBILE_PERSON;
    }
    
    /**
     * Returns whether this category denotes a person.
     * 
     * @return whether this category denotes a person.
     */
    @Pure
    public boolean isPerson() {
        return isInternalPerson() || isExternalPerson();
    }
    
    /**
     * Returns whether this category denotes an internal non-host identity.
     * 
     * @return whether this category denotes an internal non-host identity.
     */
    @Pure
    public boolean isInternalNonHostIdentity() {
        return isType()|| isInternalPerson();
    }
    
    /**
     * Returns whether this category denotes an internal identity.
     * 
     * @return whether this category denotes an internal identity.
     */
    @Pure
    public boolean isInternalIdentity() {
        return this == HOST || isInternalNonHostIdentity();
    }
    
    /* -------------------------------------------------- XDF -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code category@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("category@core.digitalid.net").load(Int8Wrapper.TYPE);
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractNonRequestingXDFConverter<Category, Object> {
        
        /**
         * Creates a new XDF converter.
         */
        private XDFConverter() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull Category category) {
            return Int8Wrapper.encode(TYPE, category.value);
        }
        
        @Pure
        @Override
        public @Nonnull Category decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("category@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(getType()) : "The block is based on the type of this converter.";
            
            final byte value = Int8Wrapper.decode(block);
            if (!isValid(value)) throw new InvalidEncodingException("The value '" + value + "' does not encode a category.");
            return get(value);
        }
        
    }
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter();
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL -------------------------------------------------- */
    
    /**
     * The SQL converter for this class.
     */
    @Immutable
    public static class SQLConverter extends AbstractSQLConverter<Category, Object> {
        
        /**
         * Creates a new SQL converter.
         */
        private SQLConverter() {
            super(Column.get("category", SQLType.TINYINT));
        }
        
        @Pure
        @Override
        public @Capturable @Nonnull @NonNullableElements @NonFrozen FreezableArray<String> getValues(@Nonnull Category category) {
            return FreezableArray.getNonNullable(category.toString());
        }
        
        @Override
        @NonCommitting
        public void storeNonNullable(@Nonnull Category category, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
            preparedStatement.setByte(parameterIndex, category.value);
        }
        
        @Pure
        @Override
        @NonCommitting
        public @Nullable Category restoreNullable(@Nonnull Object none, @Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
            final byte value = resultSet.getByte(columnIndex);
            if (resultSet.wasNull()) { return null; }
            if (!isValid(value)) { throw new SQLException("'" + value + "' is not a valid category."); }
            return get(value);
        }
        
    }
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter SQL_CONVERTER = new SQLConverter();
    
    @Pure
    @Override
    public @Nonnull SQLConverter getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Category, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
