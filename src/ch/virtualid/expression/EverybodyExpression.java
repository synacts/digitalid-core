package ch.virtualid.expression;

import ch.virtualid.credential.Credential;
import ch.virtualid.entity.NonHostEntity;
import ch.xdf.Block;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models everybody expressions.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.8
 */
final class EverybodyExpression extends Expression {
    
    /**
     * Creates a new empty expression.
     */
    EverybodyExpression(@Nonnull NonHostEntity entity) {
        super(entity);
    }
    
    /**
     * Returns whether this expression is active.
     * 
     * @return whether this expression is active.
     */
    @Override
    public boolean isActive() {
        return false;
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
        return true;
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
