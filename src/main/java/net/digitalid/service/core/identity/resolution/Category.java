package net.digitalid.service.core.identity.resolution;

import javax.annotation.Nonnull;
import net.digitalid.database.core.converter.AbstractSQLConverter;
import net.digitalid.database.core.converter.SQL;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.service.core.block.wrappers.value.integer.Integer08Wrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.AbstractNonRequestingKeyConverter;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.annotations.freezable.Frozen;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;
import net.digitalid.utility.system.exceptions.InternalException;

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
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
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
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
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
            if (category.value == value) { return category; }
        }
        
        throw ShouldNeverHappenError.get("The value '" + value + "' does not encode a category.");
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
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * Stores the key converter of this class.
     */
    private static final @Nonnull AbstractNonRequestingKeyConverter<Category, Object, Byte, Object> KEY_CONVERTER = new AbstractNonRequestingKeyConverter<Category, Object, Byte, Object>() {
        
        @Pure
        @Override
        public boolean isValid(@Nonnull Byte value) {
            return Category.isValid(value);
        }
        
        @Pure
        @Override
        public @Nonnull @Validated Byte convert(@Nonnull Category category) {
            return category.value;
        }
        
        @Pure
        @Override
        public @Nonnull Category recover(@Nonnull Object none, @Nonnull @Validated Byte value) throws InvalidEncodingException, InternalException {
            return Category.get(value);
        }
        
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code category@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("category@core.digitalid.net").load(Integer08Wrapper.XDF_TYPE);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<Category, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, Integer08Wrapper.getValueXDFConverter(TYPE));
    
    @Pure
    @Override
    public @Nonnull AbstractNonRequestingXDFConverter<Category, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = ColumnDeclaration.get("category", Integer08Wrapper.SQL_TYPE);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<Category, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, Integer08Wrapper.getValueSQLConverter(DECLARATION));
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<Category, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<Category, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
