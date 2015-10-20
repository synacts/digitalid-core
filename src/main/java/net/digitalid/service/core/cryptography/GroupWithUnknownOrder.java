package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import net.digitalid.service.core.annotations.BasedOn;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.service.core.wrappers.IntegerWrapper;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
@Immutable
public final class GroupWithUnknownOrder extends Group<GroupWithUnknownOrder> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code unknown.group@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("unknown.group@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new multiplicative group with the given modulus.
     * 
     * @param modulus the modulus of the new group.
     */
    private GroupWithUnknownOrder(@Nonnull @Positive BigInteger modulus) {
        super(modulus);
    }
    
    /**
     * Creates a new multiplicative group with the given modulus.
     * 
     * @param modulus the modulus of the new group.
     * 
     * @return a new multiplicative group with the given modulus.
     */
    @Pure
    public static @Nonnull GroupWithUnknownOrder get(@Nonnull @Positive BigInteger modulus) {
        return new GroupWithUnknownOrder(modulus);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends BlockBasedSimpleNonConceptFactory<GroupWithUnknownOrder> {
        
        /**
         * Creates a new factory.
         */
        private Factory() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull GroupWithUnknownOrder group) {
            return IntegerWrapper.encodeNonNullable(TYPE, group.getModulus());
        }
        
        @Pure
        @Override
        public @Nonnull GroupWithUnknownOrder decodeNonNullable(@Nonnull @BasedOn("unknown.group@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
            
            final @Nonnull @Positive BigInteger modulus = IntegerWrapper.decodeNonNullable(block);
            if (modulus.compareTo(BigInteger.ZERO) != 1) throw new InvalidEncodingException("The modulus has to be positive.");
            return new GroupWithUnknownOrder(modulus);
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final @Nonnull Factory FACTORY = new Factory();
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return FACTORY;
    }
    
}
