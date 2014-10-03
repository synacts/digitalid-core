package ch.virtualid.packet;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class represents an audit with a time and trail.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Audit implements Immutable, Blockable {
    
    /**
     * Stores the semantic type {@code last.time.audit@virtualid.ch}.
     */
    private static final @Nonnull SemanticType LAST_TIME = SemanticType.create("last.time.audit@virtualid.ch").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code this.time.audit@virtualid.ch}.
     */
    private static final @Nonnull SemanticType THIS_TIME = SemanticType.create("this.time.audit@virtualid.ch").load(Time.TYPE);
    
    /**
     * Stores the semantic type {@code trail.audit@virtualid.ch}.
     */
    private static final @Nonnull SemanticType TRAIL = SemanticType.create("trail.audit@virtualid.ch").load(Packet.SIGNATURES);
    
    /**
     * Stores the semantic type {@code audit@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("audit@virtualid.ch").load(TupleWrapper.TYPE, LAST_TIME, THIS_TIME, TRAIL);
    
    
    /**
     * Stores the time of the last audit.
     */
    private final @Nonnull Time lastTime;
    
    /**
     * Stores the time of this audit.
     */
    private final @Nullable Time thisTime;
    
    /**
     * Stores the trail of this audit.
     * 
     * @invariant trail == null || trail.isFrozen() : "The trail is either null or frozen.";
     * @invariant trail == null || trail.doesNotContainNull() : "The trail is either null or does not contain null.";
     * @invariant trail == null || for (@Nonnull Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    private final @Nullable ReadonlyList<Block> trail;
    
    /**
     * Creates a new audit with the given last time.
     * 
     * @param lastTime the time of the last audit.
     */
    public Audit(@Nonnull Time lastTime) {
        this(lastTime, null, null);
    }
    
    /**
     * Creates a new audit with the given times and trail.
     * 
     * @param lastTime the time of the last audit.
     * @param thisTime the time of this audit.
     * @param trail the trail of this audit.
     * 
     * @require trail == null || trail.isFrozen() : "The trail is either null or frozen.";
     * @require trail == null || trail.doesNotContainNull() : "The trail is either null or does not contain null.";
     * @require trail == null || for (@Nonnull Block block : trail) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
     */
    public Audit(@Nonnull Time lastTime, @Nullable Time thisTime, @Nullable ReadonlyList<Block> trail) {
        assert trail == null || trail.isFrozen() : "The trail is either null or frozen.";
        assert trail == null || trail.doesNotContainNull() : "The trail is either null or does not contain null.";
        if (trail != null) for (@Nonnull Block block : trail) assert block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the trail is based on the packet signature type.";
        
        this.lastTime = lastTime;
        this.thisTime = thisTime;
        this.trail = trail;
    }
    
    /**
     * Creates a new audit from the given block.
     * 
     * @param block the block containing the audit.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public Audit(@Nonnull Block block) throws InvalidEncodingException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull TupleWrapper tuple = new TupleWrapper(block);
        this.lastTime = new Time(tuple.getElementNotNull(0));
        this.thisTime = tuple.isElementNull(1) ? null : new Time(tuple.getElementNotNull(1));
        this.trail = tuple.isElementNull(2) ? null : new ListWrapper(tuple.getElementNotNull(2)).getElementsNotNull();
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(3);
        elements.set(0, lastTime.toBlock().setType(LAST_TIME));
        elements.set(1, Block.toBlock(THIS_TIME, thisTime));
        elements.set(2, trail == null ? null : new ListWrapper(TRAIL, trail).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Returns the time of the last audit.
     * 
     * @return the time of the last audit.
     */
    @Pure
    public @Nonnull Time getLastTime() {
        return lastTime;
    }
    
    /**
     * Returns the time of this audit.
     * 
     * @return the time of this audit.
     */
    @Pure
    public @Nonnull Time getThisTime() throws InvalidEncodingException {
        if (thisTime == null) throw new InvalidEncodingException("The time of this audit may not be null.");
        return thisTime;
    }
    
    /**
     * Returns the trail of this audit.
     * 
     * @return the trail of this audit.
     * 
     * @ensure for (@Nonnull Block block : return) block.getType().isBasedOn(Packet.SIGNATURE) : "Each block of the returned list is based on the packet signature type.";
     */
    @Pure
    public @Nonnull ReadonlyList<Block> getTrail() throws InvalidEncodingException {
        if (trail == null) throw new InvalidEncodingException("The trail of this audit may not be null.");
        return trail;
    }
    
}
