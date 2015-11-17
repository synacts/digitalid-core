package net.digitalid.service.core.concepts.certificate;

import javax.annotation.Nonnull;
import net.digitalid.service.core.auxiliary.Time;
import net.digitalid.service.core.block.Block;
import net.digitalid.service.core.block.wrappers.ListWrapper;
import net.digitalid.service.core.block.wrappers.TupleWrapper;
import net.digitalid.service.core.concept.NonHostConcept;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.expression.PassiveExpression;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.NonHostIdentity;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * Description.
 */
public final class Certificate extends NonHostConcept {
    
    /**
     * Stores the semantic type {@code delegation@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType DELEGATION = SemanticType.map("delegation@core.digitalid.net").load(TupleWrapper.XDF_TYPE, NonHostIdentity.IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code list.delegation@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType DELEGATIONS = SemanticType.map("list.delegation@core.digitalid.net").load(ListWrapper.XDF_TYPE, DELEGATION);
    
    /**
     * Stores the semantic type {@code outgoing.list.delegation@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType OUTGOING_DELEGATIONS = SemanticType.map("outgoing.list.delegation@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Stores the semantic type {@code incoming.list.delegation@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType INCOMING_DELEGATIONS = SemanticType.map("incoming.list.delegation@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Returns whether the given issuer is authorized to certify the given content.
     * 
     * @param issuer the issuer of interest.
     * @param content the content of interest.
     * 
     * @return whether the given issuer is authorized to certify the given content.
     */
    @Pure
    @Locked
    @NonCommitting
    public static boolean isAuthorized(@Nonnull InternalNonHostIdentity issuer, @Nonnull Block content) throws AbortException, PacketException, ExternalException, NetworkException {
//        long vid = Mapper.getVid(identifier);
//        long type = Mapper.getVid(new SelfcontainedWrapper(value).getIdentifier());
//        
//        if (vid == type) { return true; }
//        
//        // Load the certification delegations of the VID and recurse for each delegation that matches the type and the value.
//        long time = System.currentTimeMillis() + getCachingPeriod(Vid.INCOMING_DELEGATIONS) - getCachingPeriod(type);
//        Block attribute = getAttribute(vid, Vid.INCOMING_DELEGATIONS, time);
//        if (attribute == null) { return false; }
//        
//        List<Block> incoming_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//        for (final @Nonnull Block incoming_delegation : incoming_delegations) {
//            Block[] elements = new TupleWrapper(incoming_delegation).getElementsNotNull(3);
//            if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type) {
//                String restriction = new StringWrapper(elements[2]).getString();
//                Expression expression = Expression.parse(restriction);
//                if (expression.matches(value)) {
//                    // Check that the delegating VID references the current VID with the same type and expression.
//                    identifier = new StringWrapper(elements[1]).getString();
//                    attribute = getAttribute(Mapper.getVid(identifier), Vid.OUTGOING_DELEGATIONS, time);
//                    if (attribute == null) { continue; }
//                    List<Block> outgoing_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//                    for (final @Nonnull Block outgoing_delegation : outgoing_delegations) {
//                        elements = new TupleWrapper(outgoing_delegation).getElementsNotNull(3);
//                        if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type && Mapper.getVid(new StringWrapper(elements[1]).getString()) == vid && new StringWrapper(elements[2]).getString().equalsIgnoreCase(restriction)) {
//                            if (isAuthorized(identifier, value)) { return true; }
//                        }
//                    }
//                }
//            }
//        }
        
        return false;
    }
    
    
    public Certificate(@Nonnull NonHostEntity entity) {
        super(entity);
    }
    
}
