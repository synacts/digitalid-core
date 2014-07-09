package ch.virtualid.handler.action.internal;

import ch.virtualid.handler.InternalAction;
import ch.virtualid.concept.Entity;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.sql.Connection;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class PasswordSet extends InternalAction {
    
    private final @Nonnull String oldPassword;
    
    private final @Nonnull String newPassword;
    
    /**
     * Creates a new action with the given connection, entity, signature and block.
     * 
     * @param connection an open connection to the database.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     */
    protected PasswordSet(@Nonnull Connection connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block) {
        super(connection, entity, signature, block);
    }
    
}
