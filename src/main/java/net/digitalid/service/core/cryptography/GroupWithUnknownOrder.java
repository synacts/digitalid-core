package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.IntegerWrapper;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.sql.XDFBasedSQLConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * Description.
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The encoding factory for this class.
     */
    @Immutable
    public static final class EncodingFactory extends AbstractNonRequestingXDFConverter<GroupWithUnknownOrder, Object> {
        
        /**
         * Creates a new encoding factory.
         */
        private EncodingFactory() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull GroupWithUnknownOrder group) {
            return IntegerWrapper.encodeNonNullable(TYPE, group.getModulus());
        }
        
        @Pure
        @Override
        public @Nonnull GroupWithUnknownOrder decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("unknown.group@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
            
            final @Nonnull @Positive BigInteger modulus = IntegerWrapper.decodeNonNullable(block);
            if (modulus.compareTo(BigInteger.ZERO) != 1) throw new InvalidEncodingException("The modulus has to be positive.");
            return new GroupWithUnknownOrder(modulus);
        }
        
    }
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory ENCODING_FACTORY = new EncodingFactory();
    
    @Pure
    @Override
    public @Nonnull EncodingFactory getEncodingFactory() {
        return ENCODING_FACTORY;
    }

    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull AbstractSQLConverter<GroupWithUnknownOrder, Object> STORING_FACTORY = XDFBasedSQLConverter.get(ENCODING_FACTORY);
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<GroupWithUnknownOrder, Object> getStoringFactory() {
        return STORING_FACTORY;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Converters –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Converters<GroupWithUnknownOrder, Object> FACTORIES = Converters.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
