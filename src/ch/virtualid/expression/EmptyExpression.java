package ch.virtualid.expression;

import ch.virtualid.credential.Credential;
import ch.xdf.Block;
import java.sql.SQLException;

/**
 * This class models empty expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
final class EmptyExpression extends Expression {

    /**
     * Creates a new empty expression.
     */
    EmptyExpression() {
        super(null, null, 0);
    }

    /**
     * Returns whether this expression is active.
     * 
     * @return whether this expression is active.
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * Returns whether this expression matches the given block (for certification restrictions).
     * 
     * @param attribute the attribute to check.
     * @return whether this expression matches the given block.
     */
    @Override
    public boolean matches(Block attribute) {
        return true;
    }

    /**
     * Returns whether this expression matches the given credentials.
     * 
     * @param credentials the credentials to check.
     * @return whether this expression matches the given credentials.
     */
    @Override
    public boolean matches(Credential[] credentials) throws SQLException, Exception {
        return false;
    }

    /**
     * Returns this expression as a string.
     * 
     * @return this expression as a string.
     */
    @Override
    public String toString() {
        return "";
    }
}
