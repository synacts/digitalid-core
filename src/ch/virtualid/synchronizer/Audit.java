package ch.virtualid.synchronizer;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.packet.Packet;
import ch.virtualid.collections.FreezableArray;
import ch.virtualid.collections.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import javax.annotation.Nonnull;

/**
 * This class models an audit with a time and trail.
 * 
 * @see RequestAudit
 * @see ResponseAudit
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Audit implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code last.time.audit@virtualid.ch}.
     */
    public static final @Nonnull SemanticType LAST_TIME = SemanticType.create("last.time.audit@virtualid.ch").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code this.time.audit@virtualid.ch}.
     */
    public static final @Nonnull SemanticType THIS_TIME = SemanticType.create("this.time.audit@virtualid.ch").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code trail.audit@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TRAIL = SemanticType.create("trail.audit@virtualid.ch").load(Packet.SIGNATURES);
    
    /**
     * Stores the semantic type {@code audit@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("audit@virtualid.ch").load(TupleWrapper.TYPE, LAST_TIME, THIS_TIME, TRAIL);
    
    
    /**
     * Stores the time of the last audit.
     */
    private final @Nonnull Time lastTime;
    
    /**
     * Creates a new audit with the given last time.
     * 
     * @param lastTime the time of the last audit.
     */
    Audit(@Nonnull Time lastTime) {
        this.lastTime = lastTime;
    }
    
    /**
     * Creates a new audit from the given block.
     * 
     * @param block the block containing the audit.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public static @Nonnull Audit get(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        final @Nonnull Time lastTime = new Time(tuple.getElementNotNull(0));
        if (tuple.isElementNull(1)) {
            return new RequestAudit(lastTime);
        } else {
            final @Nonnull Time thisTime = new Time(tuple.getElementNotNull(1));
            final @Nonnull ReadonlyList<Block> trail = new ListWrapper(tuple.getElementNotNull(2)).getElementsNotNull();
            return new ResponseAudit(lastTime, thisTime, trail);
        }
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
        elements.set(0, lastTime.toBlock().setType(LAST_TIME));
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the time of the last audit.
     * 
     * @return the time of the last audit.
     */
    @Pure
    public final @Nonnull Time getLastTime() {
        return lastTime;
    }
    
    
    /**
     * Returns this audit as a {@link RequestAudit}.
     * 
     * @return this audit as a {@link RequestAudit}.
     * 
     * @throws InvalidEncodingException if this audit is not an instance of {@link RequestAudit}.
     */
    @Pure
    public final @Nonnull RequestAudit toRequestAudit() throws InvalidEncodingException {
        if (this instanceof RequestAudit) return (RequestAudit) this;
        throw new InvalidEncodingException("This audit is not a request audit.");
    }
    
    /**
     * Returns this audit as a {@link ResponseAudit}.
     * 
     * @return this audit as a {@link ResponseAudit}.
     * 
     * @throws InvalidEncodingException if this audit is not an instance of {@link ResponseAudit}.
     */
    @Pure
    public final @Nonnull ResponseAudit toResponseAudit() throws InvalidEncodingException {
        if (this instanceof ResponseAudit) return (ResponseAudit) this;
        throw new InvalidEncodingException("This audit is not a response audit.");
    }
    
}
