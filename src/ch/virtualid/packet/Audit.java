package ch.virtualid.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.BlockableObject;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableLinkedList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.Int64Wrapper;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import javax.annotation.Nonnull;

/**
 * This class represents an audit with a time and trail.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class Audit extends BlockableObject implements Immutable {
    
    /**
     * Stores the semantic type {@code audit@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("audit@virtualid.ch").load(TupleWrapper.TYPE, todo);
    
    
    /**
     * Stores the time of the last audit.
     */
    private final long lastTime;
    
    /**
     * Stores the time of the current audit.
     */
    private final long thisTime;
    
    /**
     * Stores the trail of this audit.
     * 
     * @invariant trail.isFrozen() : "The trail is frozen.";
     */
    private final @Nonnull ReadonlyList<Block> trail;
    
    /**
     * Creates a new audit with the given last time.
     * 
     * @param lastTime the time of the last audit.
     */
    public Audit(long lastTime) {
        this(lastTime, 0, new FreezableLinkedList<Block>().freeze());
    }
    
    /**
     * Creates a new audit with the given times and trail.
     * 
     * @param lastTime the time of the last audit.
     * @param thisTime the time of the current audit.
     * @param trail the trail of this audit.
     * 
     * @require trail.isFrozen() : "The trail is frozen.";
     */
    public Audit(long lastTime, long thisTime, @Nonnull ReadonlyList<Block> trail) {
        assert trail.isFrozen() : "The trail is frozen.";
        
        this.lastTime = lastTime;
        this.thisTime = thisTime;
        this.trail = trail;
    }
    
    /**
     * Creates a new audit from the given block.
     * 
     * @param block the block containing the audit.
     */
    public Audit(@Nonnull Block block) throws InvalidEncodingException {
        super(block);
        
        @Nonnull Block[] elements = new TupleWrapper(block).getElementsNotNull(3);
        this.lastTime = new Int64Wrapper(elements[0]).getValue();
        this.thisTime = new Int64Wrapper(elements[1]).getValue();
        this.trail = new ListWrapper(elements[2]).getElements();
    }
    
    @Pure
    @Override
    public @Nonnull Block encode() {
        @Nonnull Block[] elements = new Block[3];
        elements[0] = new Int64Wrapper(lastTime).toBlock();
        elements[1] = new Int64Wrapper(thisTime).toBlock();
        elements[2] = new ListWrapper(trail).toBlock();
        return new TupleWrapper(elements).toBlock();
    }
    
    
    /**
     * Returns the time of the last audit.
     * 
     * @return the time of the last audit.
     */
    @Pure
    public long getLastTime() {
        return lastTime;
    }
    
    /**
     * Returns the time of the current audit.
     * 
     * @return the time of the current audit.
     */
    @Pure
    public long getThisTime() {
        return thisTime;
    }
    
    /**
     * Returns the trail of this audit.
     * 
     * @return the trail of this audit.
     */
    @Pure
    public @Nonnull ReadonlyList<Block> getTrail() {
        return trail;
    }
    
}
