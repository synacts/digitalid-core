package ch.virtualid.handler.action.external;

import ch.virtualid.concept.Entity;
import ch.virtualid.handler.ExternalAction;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.sql.Connection;
import javax.annotation.Nonnull;

/**
 * In case of AccessRequest, getRequiredPermissions() returns the attributes for which access is requested. getDesiredAuthentications() indicates an identity-based request.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class AccessRequest extends ExternalAction {
    
    /**
     * Creates a new action with the given connection, entity, signature and block.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     */
    protected AccessRequest(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block) {
        super(connection, entity, signature, block);
    }
    
}
