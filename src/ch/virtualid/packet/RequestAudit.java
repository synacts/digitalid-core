package ch.virtualid.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import javax.annotation.Nonnull;

/**
 * This class models a request audit with the time of the last audit.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class RequestAudit extends Audit implements Immutable, Blockable {
    
    /**
     * Creates a new audit with the given last time.
     * 
     * @param lastTime the time of the last audit.
     */
    public RequestAudit(@Nonnull Time lastTime) {
        super(lastTime);
    }
    
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Request audit from " + getLastTime().asDate();
    }
    
}
