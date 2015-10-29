package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.NonHostIdentity;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This interface models non-host identifiers.
 * 
 * @see InternalNonHostIdentifier
 * @see ExternalIdentifier
 */
@Immutable
public interface NonHostIdentifier extends Identifier {
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity getMappedIdentity() throws AbortException;
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull NonHostIdentity getIdentity() throws AbortException, PacketException, ExternalException, NetworkException;
    
}
