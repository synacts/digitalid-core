package net.digitalid.service.core.identity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.Blockable;
import net.digitalid.service.core.wrappers.Int8Wrapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class enumerates the various categories of digital identities.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public enum Category implements Blockable, SQLizable {
    
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
    MOBILE_PERSON(5);
    
    
    /**
     * Stores an empty list of categories that can be shared among semantic types.
     * (This declaration may not be in the semantic type class as the initialization would be too late.)
     */
    public static final @Nonnull ReadOnlyList<Category> NONE = new FreezableArrayList<Category>(0).freeze();
    
    
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
     * Returns the category encoded by the given value.
     * 
     * @param value the value encoding the category.
     * 
     * @return the category encoded by the given value.
     * 
     * @require isValid(value) : "The value is a valid category.";
     */
    @Pure
    public static @Nonnull Category get(byte value) {
        assert isValid(value) : "The value is a valid category.";
        
        for (final @Nonnull Category category : values()) {
            if (category.value == value) return category;
        }
        
        throw new ShouldNeverHappenError("The value '" + value + "' does not encode a category.");
    }
    
    
    /**
     * Stores the semantic type {@code category@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("category@core.digitalid.net").load(Int8Wrapper.TYPE);
    
    /**
     * Returns the category encoded by the given block or throws an {@link InvalidEncodingException}.
     * 
     * @param block the block encoding the category.
     * 
     * @return the category encoded by the given block.
     * 
     * @throws InvalidEncodingException if the given block does not encode a category.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the category type.";
     */
    @Pure
    public static @Nonnull Category get(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the category type.";
        
        final byte value = new Int8Wrapper(block).getValue();
        if (!isValid(value)) throw new InvalidEncodingException("The value '" + value + "' does not encode a category.");
        return get(value);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return new Int8Wrapper(TYPE, value).toBlock();
    }
    
    
    /**
     * Stores the byte representation of this category.
     * 
     * @invariant isValid(value) : "The value is a valid category.";
     */
    private final byte value;
    
    /**
     * Creates a new category with the given value.
     * 
     * @param value the value encoding the category.
     */
    private Category(int value) {
        this.value = (byte) value;
    }
    
    /**
     * Returns the byte representation of this category.
     * 
     * @return the byte representation of this category.
     * 
     * @ensure isValid(value) : "The value is a valid category.";
     */
    @Pure
    public byte getValue() {
        return value;
    }
    
    
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
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Database.getConfiguration().TINYINT();
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull Category get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        final @Nonnull byte value = resultSet.getByte(columnIndex);
        if (!isValid(value)) throw new SQLException("'" + value + "' is not a valid category.");
        return get(value);
    }
    
    @Override
    @NonCommitting
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setByte(parameterIndex, value);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
    }
    
}
