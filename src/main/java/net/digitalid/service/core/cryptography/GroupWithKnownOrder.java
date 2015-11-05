package net.digitalid.service.core.cryptography;

import java.math.BigInteger;
import javax.annotation.Nonnull;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.IntegerWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.sql.XDFBasedSQLConverter;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.utility.annotations.math.Positive;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.database.converter.AbstractSQLConverter;

/**
 * This class models a multiplicative group with known order.
 */
@Immutable
public final class GroupWithKnownOrder extends Group<GroupWithKnownOrder> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Types –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the semantic type {@code modulus.group@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType MODULUS = SemanticType.map("modulus.group@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code order.group@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType ORDER = SemanticType.map("order.group@core.digitalid.net").load(IntegerWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code known.group@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("known.group@core.digitalid.net").load(TupleWrapper.TYPE, MODULUS, ORDER);
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Order –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the order of this group.
     * 
     * @invariant order.compareTo(getModulus()) == -1 : "The order is smaller than the modulus.";
     */
    private final @Nonnull @Positive BigInteger order;
    
    /**
     * Returns the order of this group.
     * 
     * @return the order of this group.
     * 
     * @ensure order.compareTo(getModulus()) == -1 : "The order is smaller than the modulus.";
     */
    @Pure
    public @Nonnull @Positive BigInteger getOrder() {
        return order;
    }
    
    /**
     * Returns a new group with the same modulus but without the order.
     * 
     * @return a new group with the same modulus but without the order.
     */
    @Pure
    public @Nonnull GroupWithUnknownOrder dropOrder() {
        return GroupWithUnknownOrder.get(getModulus());
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new multiplicative group with the given modulus and order.
     * 
     * @param modulus the modulus of the new group.
     * @param order the order of the new group.
     * 
     * @require order.compareTo(modulus) == -1 : "The order is smaller than the modulus.";
     */
    private GroupWithKnownOrder(@Nonnull @Positive BigInteger modulus, @Nonnull @Positive BigInteger order) {
        super(modulus);
        
        assert order.compareTo(BigInteger.ZERO) == 1 && order.compareTo(modulus) == -1 : "The order is positive and smaller than the modulus.";
        
        this.order = order;
    }
    
    /**
     * Creates a new multiplicative group with the given modulus and order.
     * 
     * @param modulus the modulus of the new group.
     * @param order the order of the new group.
     * 
     * @return a new multiplicative group with the given modulus and order.
     * 
     * @require order.compareTo(modulus) == -1 : "The order is smaller than the modulus.";
     */
    @Pure
    public static @Nonnull GroupWithKnownOrder get(@Nonnull @Positive BigInteger modulus, @Nonnull @Positive BigInteger order) {
        return new GroupWithKnownOrder(modulus, order);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Encodable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The XDF converter for this class.
     */
    @Immutable
    public static final class XDFConverter extends AbstractNonRequestingXDFConverter<GroupWithKnownOrder, Object> {
        
        /**
         * Creates a new XDF converter.
         */
        private XDFConverter() {
            super(TYPE);
        }
        
        @Pure
        @Override
        public @Nonnull Block encodeNonNullable(@Nonnull GroupWithKnownOrder group) {
            final @Nonnull FreezableArray<Block> elements =  FreezableArray.get(2);
            elements.set(0, IntegerWrapper.encodeNonNullable(MODULUS, group.getModulus()));
            elements.set(1, IntegerWrapper.encodeNonNullable(ORDER, group.getOrder()));
            return TupleWrapper.encode(TYPE, elements.freeze());
        }
        
        @Pure
        @Override
        public @Nonnull GroupWithKnownOrder decodeNonNullable(@Nonnull Object none, @Nonnull @BasedOn("known.group@core.digitalid.net") Block block) throws InvalidEncodingException {
            assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
            
            final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
            
            final @Nonnull @Positive BigInteger modulus = IntegerWrapper.decodeNonNullable(tuple.getNonNullableElement(0));
            if (modulus.compareTo(BigInteger.ZERO) != 1) throw new InvalidEncodingException("The modulus has to be positive.");
            
            final @Nonnull @Positive BigInteger order = IntegerWrapper.decodeNonNullable(tuple.getNonNullableElement(1));
            if (order.compareTo(BigInteger.ZERO) != 1 || order.compareTo(modulus) != -1) throw new InvalidEncodingException("The order has to be positive and smaller than the modulus.");
            
            return new GroupWithKnownOrder(modulus, order);
        }
        
    }
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull XDFConverter XDF_CONVERTER = new XDFConverter();
    
    @Pure
    @Override
    public @Nonnull XDFConverter getXDFConverter() {
        return XDF_CONVERTER;
    }

    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<GroupWithKnownOrder, Object> SQL_CONVERTER = XDFBasedSQLConverter.get(XDF_CONVERTER);
    
    @Pure
    @Override
    public @Nonnull AbstractSQLConverter<GroupWithKnownOrder, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Converters –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<GroupWithKnownOrder, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
