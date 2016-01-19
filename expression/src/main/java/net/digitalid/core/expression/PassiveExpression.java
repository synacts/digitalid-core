package net.digitalid.core.expression;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.signature.CredentialsSignatureWrapper;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.entity.NonHostEntity;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.service.core.identity.SemanticType;

/**
 * This class models passive expressions.
 */
@Immutable
public final class PassiveExpression extends AbstractExpression {
    
    /**
     * Stores the semantic type {@code passive.expression@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("passive.expression@core.digitalid.net").load(StringWrapper.XDF_TYPE);
    
    
    /**
     * Creates a new passive expression with the given entity and string.
     * 
     * @param entity the entity to which this passive expression belongs.
     * @param string the string which is to be parsed for the expression.
     */
    @NonCommitting
    public PassiveExpression(@Nonnull NonHostEntity entity, @Nonnull String string) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, string);
    }
    
    /**
     * Creates a new passive expression from the given entity and block.
     * 
     * @param entity the entity to which this passive expression belongs.
     * @param block the block which contains the passive expression.
     * 
     * @require block.getType().isBasedOn(StringWrapper.TYPE) : "The block is based on the string type.";
     */
    @NonCommitting
    public PassiveExpression(@Nonnull NonHostEntity entity, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, block);
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    boolean isValid() {
        return true;
    }
    
    
    /**
     * Returns whether this passive expression is public.
     * 
     * @return whether this passive expression is public.
     */
    @Pure
    public boolean isPublic() {
        return getExpression().isPublic();
    }
    
    /**
     * Returns whether this passive expression matches the given signature.
     * 
     * @param signature the signature which is to be checked.
     * 
     * @return whether this passive expression matches the given signature.
     */
    @Pure
    @NonCommitting
    public boolean matches(@Nonnull CredentialsSignatureWrapper signature) throws DatabaseException {
        return getExpression().matches(signature);
    }
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param entity the entity to which the returned expression belongs.
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PassiveExpression get(@Nonnull NonHostEntity entity, @NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        try {
            return new PassiveExpression(entity, resultSet.getString(columnIndex));
        } catch (@Nonnull IOException | RequestException | ExternalException exception) {
            throw new SQLException("The expression returned by the database is invalid.", exception);
        }
    }
    
}
