package ch.virtualid.concepts;

import ch.virtualid.auxiliary.Time;
import ch.virtualid.concept.NonHostConcept;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Certificate extends NonHostConcept {
    
    /**
     * Stores the semantic type {@code delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType DELEGATION = SemanticType.create("delegation@virtualid.ch").load(TupleWrapper.TYPE, NonHostIdentity.IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType DELEGATIONS = SemanticType.create("list.delegation@virtualid.ch").load(ListWrapper.TYPE, DELEGATION);
    
    /**
     * Stores the semantic type {@code outgoing.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType OUTGOING_DELEGATIONS = SemanticType.create("outgoing.list.delegation@virtualid.ch").load(new Category[] {Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Stores the semantic type {@code incoming.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType INCOMING_DELEGATIONS = SemanticType.create("incoming.list.delegation@virtualid.ch").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Returns whether the given issuer is authorized to certify the given content.
     * 
     * @param issuer the issuer of interest.
     * @param content the content of interest.
     * 
     * @return whether the given issuer is authorized to certify the given content.
     */
    public static boolean isAuthorized(@Nonnull InternalNonHostIdentity issuer, @Nonnull Block content) throws SQLException, IOException, PacketException, ExternalException {
//        long vid = Mapper.getVid(identifier);
//        long type = Mapper.getVid(new SelfcontainedWrapper(value).getIdentifier());
//        
//        if (vid == type) return true;
//        
//        // Load the certification delegations of the VID and recurse for each delegation that matches the type and the value.
//        long time = System.currentTimeMillis() + getCachingPeriod(Vid.INCOMING_DELEGATIONS) - getCachingPeriod(type);
//        Block attribute = getAttribute(vid, Vid.INCOMING_DELEGATIONS, time);
//        if (attribute == null) return false;
//        
//        List<Block> incoming_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//        for (Block incoming_delegation : incoming_delegations) {
//            Block[] elements = new TupleWrapper(incoming_delegation).getElementsNotNull(3);
//            if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type) {
//                String restriction = new StringWrapper(elements[2]).getString();
//                Expression expression = Expression.parse(restriction);
//                if (expression.matches(value)) {
//                    // Check that the delegating VID references the current VID with the same type and expression.
//                    identifier = new StringWrapper(elements[1]).getString();
//                    attribute = getAttribute(Mapper.getVid(identifier), Vid.OUTGOING_DELEGATIONS, time);
//                    if (attribute == null) continue;
//                    List<Block> outgoing_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//                    for (Block outgoing_delegation : outgoing_delegations) {
//                        elements = new TupleWrapper(outgoing_delegation).getElementsNotNull(3);
//                        if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type && Mapper.getVid(new StringWrapper(elements[1]).getString()) == vid && new StringWrapper(elements[2]).getString().equalsIgnoreCase(restriction)) {
//                            if (isAuthorized(identifier, value)) return true;
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
