package net.digitalid.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.Pure;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public class KnownGroup {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Order –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the order of this group.
     * 
     * @invariant order.compareTo(BigInteger.ZERO) == 1 && order.compareTo(getModulus()) == -1 : "The order is positive and smaller than the modulus.";
     */
    private final @Nonnull BigInteger order;
    
    /**
     * Returns the order of this group.
     * 
     * @return the order of this group.
     * 
     * @ensure order.compareTo(BigInteger.ZERO) == 1 && order.compareTo(getModulus()) == -1 : "The order is positive and smaller than the modulus.";
     */
    @Pure
    public @Nonnull BigInteger getOrder() {
        return order;
    }
    
    /**
     * Returns a new group with the same modulus but without the order.
     * 
     * @return a new group with the same modulus but without the order.
     * 
     * @require hasOrder() : "The order of this group is known.";
     * 
     * @ensure return.hasNoOrder() : "The order of the returned group is unknown.";
     */
    @Pure
    public @Nonnull Group dropOrder() {
        assert hasOrder() : "The order of this group is known.";
        
        return new Group(modulus, null);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    public KnownGroup() {
        // TODO
    }
    
}
