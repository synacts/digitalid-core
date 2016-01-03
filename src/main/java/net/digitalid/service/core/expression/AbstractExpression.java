package net.digitalid.service.core.expression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.Blockable;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.concept.NonHostConcept;
import net.digitalid.service.core.database.SQLizable;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.exceptions.external.ExternalException;

/**
 * This class models abstract expressions.
 * 
 * @see ActiveExpression
 * @see PassiveExpression
 * @see PersonalExpression
 * @see ImpersonalExpression
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
    AbstractExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity);
        
        this.expression = Expression.parse(entity, string);
        if (!isValid()) { throw InvalidParameterValueException.get("expression", string); }
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
    AbstractExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        this(entity, StringWrapper.decodeNonNullable(block));
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return StringWrapper.encodeNonNullable(getType(), expression.toString());
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
        if (object == this) { return true; }
        if (object == null || !(object instanceof AbstractExpression)) { return false; }
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
    public final void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        preparedStatement.setString(parameterIndex, toString());
    }
    
}
