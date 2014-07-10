package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.exception.ShouldNeverHappenError;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.Int8Wrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class enumerates the various categories of virtual identities.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public enum Category implements Blockable, Immutable, SQLizable {
    HOST(0),
    SYNTACTIC_TYPE(1),
    SEMANTIC_TYPE(2),
    NATURAL_PERSON(3),
    ARTIFICIAL_PERSON(4),
    EMAIL_PERSON(5);
    
    
    /**
     * Stores the semantic type {@code category@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("category@virtualid.ch").load(Int8Wrapper.TYPE);
    
    
    /**
     * Stores the byte representation of the category.
     */
    private final byte value;
    
    /**
     * Creates a new category with the given value.
     * 
     * @param value the value encoding the category.
     */
    Category(int value) {
        this.value = (byte) value;
    }
    
    
    /**
     * Returns the byte representation of this category.
     * 
     * @return the byte representation of this category.
     */
    public byte getValue() {
        return value;
    }
    
    /**
     * Returns whether this category denotes a type.
     * 
     * @return whether this category denotes a type.
     */
    public boolean isType() {
        return this == SYNTACTIC_TYPE || this == SEMANTIC_TYPE;
    }
    
    /**
     * Returns whether this category denotes a person.
     * 
     * @return whether this category denotes a person.
     */
    public boolean isPerson() {
        return this == NATURAL_PERSON || this == ARTIFICIAL_PERSON || this == EMAIL_PERSON;
    }
    
    @Override
    public @Nonnull String toString() {
        return String.valueOf(value);
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
     * Returns the category encoded by the given value.
     * 
     * @param value the value encoding the category.
     * 
     * @return the category encoded by the given value.
     * 
     * @require value >= 0 && value <= 5 : "The value is between 0 and 5 (both inclusive).";
     */
    public static @Nonnull Category get(byte value) {
        assert value >= 0 && value <= 5 : "The value is between 0 and 5 (both inclusive).";
        
        for (@Nonnull Category category : values()) {
            if (category.value == value) return category;
        }
        
        throw new ShouldNeverHappenError("The value '" + value + "' does not encode a category.");
    }
    
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
    public static @Nonnull Category get(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the category type.";
        
        final byte value = new Int8Wrapper(block).getValue();
        if (value < 0 || value > 5) throw new InvalidEncodingException("The value '" + value + "' does not encode a category.");
        return get(value);
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
    public static @Nonnull Category get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return get(resultSet.getByte(columnIndex));
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setByte(parameterIndex, value);
    }
    
}
