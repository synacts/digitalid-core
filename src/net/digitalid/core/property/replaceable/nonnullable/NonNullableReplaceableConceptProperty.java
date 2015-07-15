package net.digitalid.core.property.replaceable.nonnullable;

import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.property.ConceptProperty;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 0.0
 */
public class NonNullableReplaceableConceptProperty<V> extends NonNullableReplaceableProperty<V> implements ConceptProperty {
    
    // TODO: Is this class needed after all? Probably for Password, Context.name, Agent.removed, ClientAgent.commitment, ClientAgent.name, ClientAgent.icon, OutgoingRole.relation, OutgoingRole.context
    
    @Override
    public Concept getConcept() {
        throw new UnsupportedOperationException("getConcept in NullableReplaceableConceptProperty is not supported yet.");
    }
    
    @Override
    public Time getTime() {
        throw new UnsupportedOperationException("getTimeOfLastModification in NullableReplaceableConceptProperty is not supported yet.");
    }
    
}
