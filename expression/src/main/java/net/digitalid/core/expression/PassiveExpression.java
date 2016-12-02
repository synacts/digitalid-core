package net.digitalid.core.expression;


import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.signature.credentials.CredentialsSignature;

/**
 * This class models passive expressions.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class PassiveExpression extends AbstractExpression {
    
    /* -------------------------------------------------- Aggregation -------------------------------------------------- */
    
    /**
     * Returns whether this passive expression is public.
     */
    @Pure
    public boolean isPublic() {
        return getExpression().isPublic();
    }
    
    /**
     * Returns whether this passive expression matches the given signature.
     */
    @Pure
    @NonCommitting
    public boolean matches(@Nonnull CredentialsSignature<?> signature) throws DatabaseException {
        return getExpression().matches(signature);
    }
    
}
