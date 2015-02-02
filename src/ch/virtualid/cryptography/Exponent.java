package ch.virtualid.cryptography;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.IntegerWrapper;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An exponent is a number that raises elements of an arbitrary group.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Exponent extends Number implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code exponent.group@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("exponent.group@virtualid.ch").load(IntegerWrapper.TYPE);
    
    
    /**
     * Creates a new exponent with the given value.
     * 
     * @param value the value of the new exponent.
     */
    public Exponent(@Nonnull BigInteger value) {
        super(value);
    }
    
    /**
     * Creates a new exponent from the given block.
     * 
     * @param block the block that encodes the value of the new exponent.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Exponent(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Adds the given exponent to this exponent.
     * 
     * @param exponent the exponent to be added.
     * 
     * @return the sum of this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent add(@Nonnull Exponent exponent) {
        return new Exponent(getValue().add(exponent.getValue()));
    }
    
    /**
     * Subtracts the given exponent from this exponent.
     * 
     * @param exponent the exponent to be subtracted.
     * 
     * @return the difference between this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent subtract(@Nonnull Exponent exponent) {
        return new Exponent(getValue().subtract(exponent.getValue()));
    }
    
    /**
     * Multiplies this exponent with the given exponent.
     * 
     * @param exponent the exponent to be multiplied.
     * 
     * @return the product of this and the given exponent.
     */
    @Pure
    public @Nonnull Exponent multiply(@Nonnull Exponent exponent) {
        return new Exponent(getValue().multiply(exponent.getValue()));
    }
    
    /**
     * Inverses this exponent in the given group.
     * 
     * @param group a group with known order.
     * 
     * @return the multiplicative inverse of this exponent.
     * 
     * @require group.hasOrder() : "The order of the group is known.";
     * @require group.getOrder().gcd(getValue()).compareTo(BigInteger.ONE) == 0 : "The exponent is relatively prime to the group order.";
     */
    @Pure
    public @Nonnull Exponent inverse(@Nonnull Group group) {
        assert group.hasOrder() : "The order of the group is known.";
        assert group.getOrder().gcd(getValue()).compareTo(BigInteger.ONE) == 0 : "The exponent is relatively prime to the group order.";
        
        return new Exponent(getValue().modInverse(group.getOrder()));
    }
    
    /**
     * Returns the next (or the same) relatively prime exponent.
     * 
     * @param group a group with known order.
     * 
     * @return the next (or the same) relatively prime exponent.
     * 
     * @require group.hasOrder() : "The order of the group is known.";
     */
    @Pure
    public @Nonnull Exponent getNextRelativePrime(@Nonnull Group group) {
        assert group.hasOrder() : "The order of the group is known.";
        
        @Nonnull BigInteger next = getValue();
        while (next.gcd(group.getOrder()).compareTo(BigInteger.ONE) == 1) next = next.add(BigInteger.ONE);
        return new Exponent(next);
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Block.FORMAT;
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @DoesNotCommit
    public static @Nullable Exponent get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            final @Nullable Block block = Block.get(TYPE, resultSet, columnIndex);
            if (block == null) return null;
            else return new Exponent(block);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The exponent returned by the database is invalid.", exception);
        }
    }
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @DoesNotCommit
    public static @Nonnull Exponent getNotNull(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            return new Exponent(Block.getNotNull(TYPE, resultSet, columnIndex));
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The exponent returned by the database is invalid.", exception);
        }
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to this object.
     * 
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @Override
    @DoesNotCommit
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        toBlock().set(preparedStatement, parameterIndex);
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given exponent.
     * 
     * @param exponent the exponent to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @DoesNotCommit
    public static void set(@Nullable Exponent exponent, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        Block.set(Block.toBlock(exponent), preparedStatement, parameterIndex);
    }
    
}
