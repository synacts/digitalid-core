package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import net.digitalid.service.core.identifier.NonHostIdentifier;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This interface models an external identity.
 * 
 * @see ExternalPerson
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public interface ExternalIdentity extends Identity {
    
    /**
     * Stores the semantic type {@code external@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("external@core.digitalid.net").load(Identity.IDENTIFIER);
    
    
    @Pure
    @Override
    public @Nonnull NonHostIdentifier getAddress();
    
}
