package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.service.core.concept.property.ConceptPropertyInternalAction;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;

/**
 * This exception is thrown when the action to change a property is invalid.
 * This is typically the case when the action would not affect the property.
 */
@Immutable
public class InvalidConceptPropertyActionException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    /**
     * Stores the action to change a property which is invalid.
     */
    private final @Nonnull ConceptPropertyInternalAction<?, ?, ?> action;
    
    /**
     * Returns the action to change a property which is invalid.
     * 
     * @return the action to change a property which is invalid.
     */
    @Pure
    public final @Nonnull ConceptPropertyInternalAction<?, ?, ?> getAction() {
        return action;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid property action exception with the given action.
     * 
     * @param action the action to change a property which is invalid.
     */
    protected InvalidConceptPropertyActionException(@Nonnull ConceptPropertyInternalAction<?, ?, ?> action) {
        super("The action to change the property '" + action.getProperty().getConceptPropertySetup().getPropertyName() + "' of the concept '" + action.getProperty().getConceptPropertySetup().getConceptSetup().getConceptName() + "' of the user " + action.getEntityNotNull().getIdentity().getAddress() + " is invalid.");
        
        this.action = action;
    }
    
    /**
     * Returns a new invalid property action exception with the given action.
     * 
     * @param action the action to change a property which is invalid.
     * 
     * @return a new invalid property action exception with the given action.
     */
    @Pure
    public static @Nonnull InvalidConceptPropertyActionException get(@Nonnull ConceptPropertyInternalAction<?, ?, ?> action) {
        return new InvalidConceptPropertyActionException(action);
    }
    
}
