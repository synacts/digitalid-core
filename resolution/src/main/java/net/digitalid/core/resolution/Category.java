package net.digitalid.core.resolution;

import javax.annotation.Nonnull;

import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.exceptions.UnexpectedValueException;
import net.digitalid.utility.freezable.Frozen;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQL;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;

import net.digitalid.core.conversion.NonRequestingConverters;
import net.digitalid.core.conversion.key.NonRequestingKeyConverter;
import net.digitalid.core.conversion.wrappers.value.integer.Integer08Wrapper;
import net.digitalid.core.conversion.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.NonRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.identity.SemanticType;

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
        Require.that(isValid(value)).orThrow("The value is a valid category.");
        
        for (final @Nonnull Category category : values()) {
            if (category.value == value) { return category; }
        }
        
        throw UnexpectedValueException.with("value", value);
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
    private static final @Nonnull NonRequestingKeyConverter<Category, Object, Byte, Object> KEY_CONVERTER = new NonRequestingKeyConverter<Category, Object, Byte, Object>() {
        
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
        public @Nonnull Category recover(@Nonnull Object none, @Nonnull @Validated Byte value) {
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
    public static final @Nonnull NonRequestingXDFConverter<Category, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, Integer08Wrapper.getValueXDFConverter(TYPE));
    
    @Pure
    @Override
    public @Nonnull NonRequestingXDFConverter<Category, Object> getXDFConverter() {
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
    public static final @Nonnull SQLConverter<Category, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, Integer08Wrapper.getValueSQLConverter(DECLARATION));
    
    @Pure
    @Override
    public @Nonnull SQLConverter<Category, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<Category, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
