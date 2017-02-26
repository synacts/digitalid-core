package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;

/**
 * The only purpose of this class is to be able to use the identifier resolution from this package.
 */
@Stateless
public abstract class PseudoIdentifierResolver extends IdentifierResolver {
    
    @Pure
    @NonCommitting
    protected static @Nonnull Identity resolveWithProvider(@Nonnull Identifier identifier) throws ExternalException {
        return IdentifierResolver.resolveWithProvider(identifier);
    }
    
}
