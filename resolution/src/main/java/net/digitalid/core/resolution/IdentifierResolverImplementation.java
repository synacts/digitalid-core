package net.digitalid.core.resolution;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.IdentifierResolver;
import net.digitalid.core.identification.identity.Identity;

/**
 * This class implements the {@link IdentifierResolver}.
 */
@Stateless
public class IdentifierResolverImplementation extends IdentifierResolver {
    
    /* -------------------------------------------------- Implementation -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Identity getIdentity(@Nonnull Identifier identifier) throws ExternalException {
        if (identifier instanceof HostIdentifier) {
            return createHostIdentity(1L, (HostIdentifier) identifier);
        } else if (identifier instanceof InternalNonHostIdentifier) {
            return createSemanticType(2L, (InternalNonHostIdentifier) identifier);
        } else {
            throw new UnsupportedOperationException("The identifier resolver does not support '" + identifier.getClass() + "' yet.");
        }
    }
    
    /* -------------------------------------------------- Injection -------------------------------------------------- */
    
    /**
     * Initializes the the public key retriever.
     */
    @PureWithSideEffects
    @Initialize(target = IdentifierResolver.class)
    public static void initializeIdentifierResolver() {
        IdentifierResolver.configuration.set(new IdentifierResolverImplementation());
    }
    
}
