package ch.virtualid.expression;

import ch.virtualid.annotations.Pure;
import ch.virtualid.credential.Credential;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.server.Host;
import ch.xdf.Block;
import java.sql.Connection;
import java.sql.SQLException;

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
     * @require left != null : "The left child is not null.";
     * @require right != null : "The right child is not null.";
     * @require operator == '+' || operator == '-' || operator == '*' : "The operator is either plus, minus or times.";
     */
    BinaryExpression(Connection connection, Host host, long vid, String left, String right, char operator) throws InvalidEncodingException, SQLException, Exception {
        super(connection, host, vid);

        assert left != null : "The left child is not null.";
        assert right != null : "The right child is not null.";
        assert operator == '+' || operator == '-' || operator == '*' : "The operator is either plus, minus or times.";

        this.left = Expression.parse(left, connection, host, vid);
        this.right = Expression.parse(right, connection, host, vid);
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
