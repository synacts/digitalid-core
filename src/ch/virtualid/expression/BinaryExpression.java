package ch.virtualid.expression;

import ch.virtualid.annotations.Pure;
import ch.virtualid.credential.Credential;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.xdf.Block;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models binary expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
final class BinaryExpression extends Expression {
    
    /**
     * Stores the left child of this binary expression.
     */
    private final Expression left;
    
    /**
     * Stores the right child of this binary expression.
     */
    private final Expression right;
    
    /**
     * Stores the operator this binary expression.
     */
    private final char operator;
    
    /**
     * Creates a new binary expression with the given left and right children.
     * 
     * @param connection a connection to the database.
     * @param host the host of the VID.
     * @param vid the VID of the contexts.
     * @param left the left child to parse.
     * @param right the right child to parse.
     * @param operator the operator to use.
     * 
     * @require operator == '+' || operator == '-' || operator == '*' : "The operator is either plus, minus or times.";
     */
    BinaryExpression(@Nonnull NonHostEntity entity, @Nonnull String left, @Nonnull String right, @Nonnull char operator) throws SQLException, IOException, PacketException, ExternalException {
        super(entity);
        
        assert left != null : "The left child is not null.";
        assert right != null : "The right child is not null.";
        assert operator == '+' || operator == '-' || operator == '*' : "The operator is either plus, minus or times.";
        
        this.left = Expression.parse(entity, left);
        this.right = Expression.parse(entity, right);
        this.operator = operator;
    }
    
    /**
     * Returns whether this expression is active.
     * 
     * @return whether this expression is active.
     */
    @Override
    public boolean isActive() {
        return left.isActive() && right.isActive();
    }
    
    /**
     * Returns whether this expression matches the given block (for certification restrictions).
     * 
     * @param attribute the attribute to check.
     * @return whether this expression matches the given block.
     * @require attribute != null : "The attribute is not null.";
     */
    @Override
    public boolean matches(Block attribute) throws InvalidEncodingException, SQLException, Exception {
        assert attribute != null : "The attribute is not null.";
        
        switch (operator) {
            case '+': return left.matches(attribute) || right.matches(attribute);
            case '-': return left.matches(attribute) && !right.matches(attribute);
            case '*': return left.matches(attribute) && right.matches(attribute);
            default: return false;
        }
    }
    
    /**
     * Returns whether this expression matches the given credentials.
     * 
     * @param credentials the credentials to check.
     * @return whether this expression matches the given credentials.
     */
    @Override
    public boolean matches(Credential[] credentials) throws SQLException, Exception {
        switch (operator) {
            case '+': return left.matches(credentials) || right.matches(credentials);
            case '-': return left.matches(credentials) && !right.matches(credentials);
            case '*': return left.matches(credentials) && right.matches(credentials);
            default: return false;
        }
    }
    
    /**
     * Returns this expression as a string.
     * 
     * @return this expression as a string.
     */
    @Pure
    @Override
    public String toString() {
        return "(" + left + operator + right + ")";
    }
    
}
