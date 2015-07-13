package net.digitalid.core.expression;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.concept.NonHostConcept;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.wrappers.Blockable;
import net.digitalid.core.database.SQLizable;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.StringWrapper;

/**
 * This class models abstract expressions.
 * 
 * @see ActiveExpression
 * @see PassiveExpression
 * @see PersonalExpression
 * @see ImpersonalExpression
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
@Immutable
abstract class AbstractExpression extends NonHostConcept implements Blockable, SQLizable {
    
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
    @NonCommitting
    AbstractExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws SQLException, IOException, PacketException, ExternalException {
        super(entity);
        
        this.expression = Expression.parse(entity, string);
        if (!isValid()) throw new InvalidEncodingException("The expression '" + string + "' is invalid as " + getClass().getSimpleName() + ".");
    }
    
    /**
     * Creates a new abstract expression from the given entity and block.
     * 
     * @param entity the entity to which this abstract expression belongs.
     * @param block the block which contains the abstract expression.
     * 
     * @require block.getType().isBasedOn(StringWrapper.TYPE) : "The block is based on the string type.";
     */
    @NonCommitting
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
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof AbstractExpression)) return false;
        final @Nonnull AbstractExpression other = (AbstractExpression) object;
        return this.expression.equals(other.expression);
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return expression.hashCode();
    }
    
    @Pure
    @Override
    public final String toString() {
        return expression.toString();
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = "TEXT NOT NULL COLLATE " + Database.getConfiguration().BINARY();
    
    @Override
    @NonCommitting
    public final void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setString(parameterIndex, toString());
    }
    
}
