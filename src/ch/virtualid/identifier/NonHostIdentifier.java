package ch.virtualid.identifier;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.interfaces.Immutable;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This interface models non-host identifiers.
 * 
 * @see InternalNonHostIdentifier
 * @see ExternalIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface NonHostIdentifier extends Identifier, Immutable {
    
    @Pure
    @Override
    @DoesNotCommit
    public @Nonnull NonHostIdentity getMappedIdentity() throws SQLException;
    
    @Pure
    @Override
    @DoesNotCommit
    public @Nonnull NonHostIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException;
    
}
