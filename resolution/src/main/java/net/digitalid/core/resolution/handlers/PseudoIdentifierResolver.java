package net.digitalid.core.resolution.handlers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;

/**
 * The only purpose of this class is to be able to use the identifier loading from this package.
 */
@Stateless
public abstract class PseudoIdentifierResolver extends IdentifierResolver {
    
    @Pure
    @NonCommitting
    protected static @Nullable Identity loadWithProvider(@Nonnull Identifier identifier) throws DatabaseException, RecoveryException {
        return IdentifierResolver.loadWithProvider(identifier);
    }
    
}
