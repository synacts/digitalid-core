package ch.virtualid.expression;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.NonHostConcept;
import ch.virtualid.database.Database;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models abstract expressions.
 * 
 * @see ActiveExpression
 * @see PassiveExpression
 * @see PersonalExpression
 * @see ImpersonalExpression
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
abstract class AbstractExpression extends NonHostConcept implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the expression of this abstract expression.
     */
    private final @Nonnull Expression expression;
    
    /**
     * Creates a new abstract expression with the given entity and string.
     * 
     * @param entity the entity to which this abstract expression belongs.
     * @param string the string which is to be parsed for the expression.
     */
    AbstractExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws SQLException, IOException, PacketException, ExternalException {
        super(entity);
        
        this.expression = Expression.parse(entity, string);
        if (isValid()) throw new InvalidEncodingException("The expression '" + string + "' is invalid as " + getClass().getSimpleName() + ".");
    }
    
    /**
     * Creates a new abstract expression from the given entity and block.
     * 
     * @param entity the entity to which this abstract expression belongs.
     * @param block the block which contains the abstract expression.
     * 
     * @require block.getType().isBasedOn(StringWrapper.TYPE) : "The block is based on the string type.";
     */
    AbstractExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        this(entity, new StringWrapper(block).getString());
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return new StringWrapper(getType(), expression.toString()).toBlock();
    }
    
    
    /**
     * Returns the expression of this abstract expression.
     * 
     * @return the expression of this abstract expression.
     */
    @Pure
    final @Nonnull Expression getExpression() {
        return expression;
    }
    
    /**
     * Returns whether the expression is valid.
     * 
     * @return whether the expression is valid.
     */
    @Pure
    abstract boolean isValid();
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "TEXT NOT NULL COLLATE " + Database.getConfiguration().BINARY();
    
    @Pure
    @Override
    public String toString() {
        return expression.toString();
    }
    
    @Override
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setString(parameterIndex, toString());
    }
    
}
