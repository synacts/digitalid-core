package net.digitalid.core.certificate;

import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.concept.CoreConcept;
import net.digitalid.core.entity.NonHostEntity;

/**
 * Description.
 */
@Immutable
public abstract class Certificate extends CoreConcept<NonHostEntity<?>, Long> {
    
    // TODO:
    
//    /**
//     * Stores the semantic type {@code delegation@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType DELEGATION = SemanticType.map("delegation@core.digitalid.net").load(TupleWrapper.XDF_TYPE, NonHostIdentity.IDENTIFIER, PassiveExpression.TYPE);
//    
//    /**
//     * Stores the semantic type {@code list.delegation@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType DELEGATIONS = SemanticType.map("list.delegation@core.digitalid.net").load(ListWrapper.XDF_TYPE, DELEGATION);
//    
//    /**
//     * Stores the semantic type {@code outgoing.list.delegation@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType OUTGOING_DELEGATIONS = SemanticType.map("outgoing.list.delegation@core.digitalid.net").load(new Category[] {Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
//    
//    /**
//     * Stores the semantic type {@code incoming.list.delegation@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType INCOMING_DELEGATIONS = SemanticType.map("incoming.list.delegation@core.digitalid.net").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
//    
//    /**
//     * Returns whether the given issuer is authorized to certify the given content.
//     * 
//     * @param issuer the issuer of interest.
//     * @param content the content of interest.
//     * 
//     * @return whether the given issuer is authorized to certify the given content.
//     */
//    @Pure
//    @NonCommitting
//    public static boolean isAuthorized(@Nonnull InternalNonHostIdentity issuer, @Nonnull Block content) throws ExternalException {
////        long vid = Mapper.getVid(identifier);
////        long type = Mapper.getVid(new SelfcontainedWrapper(value).getIdentifier());
////        
////        if (vid == type) { return true; }
////        
////        // Load the certification delegations of the VID and recurse for each delegation that matches the type and the value.
////        long time = System.currentTimeMillis() + getCachingPeriod(Vid.INCOMING_DELEGATIONS) - getCachingPeriod(type);
////        Block attribute = getAttribute(vid, Vid.INCOMING_DELEGATIONS, time);
////        if (attribute == null) { return false; }
////        
////        List<Block> incoming_delegations = ListWrapper.decodeNullableElements(SelfcontainedWrapper.decodeNonNullable(new SignatureWrapper(attribute, false).getElement()));
////        for (final @Nonnull Block incoming_delegation : incoming_delegations) {
////            Block[] elements = TupleWrapper.decode(incoming_delegation).getElementsNotNull(3);
////            if (Mapper.getVid(StringWrapper.decodeNonNullable(elements[0])) == type) {
////                String restriction = StringWrapper.decodeNonNullable(elements[2]);
////                Expression expression = Expression.parse(restriction);
////                if (expression.matches(value)) {
////                    // Check that the delegating VID references the current VID with the same type and expression.
////                    identifier = StringWrapper.decodeNonNullable(elements[1]);
////                    attribute = getAttribute(Mapper.getVid(identifier), Vid.OUTGOING_DELEGATIONS, time);
////                    if (attribute == null) { continue; }
////                    List<Block> outgoing_delegations = ListWrapper.decodeNullableElements(SelfcontainedWrapper.decodeNonNullable(new SignatureWrapper(attribute, false).getElement()));
////                    for (final @Nonnull Block outgoing_delegation : outgoing_delegations) {
////                        elements = TupleWrapper.decode(outgoing_delegation).getElementsNotNull(3);
////                        if (Mapper.getVid(StringWrapper.decodeNonNullable(elements[0])) == type && Mapper.getVid(StringWrapper.decodeNonNullable(elements[1])) == vid && StringWrapper.decodeNonNullable(elements[2]).equalsIgnoreCase(restriction)) {
////                            if (isAuthorized(identifier, value)) { return true; }
////                        }
////                    }
////                }
////            }
////        }
//        
//        return false;
//    }
//    
//    public Certificate(@Nonnull NonHostEntity<?> entity) {
//        super(entity);
//    }
    
}
