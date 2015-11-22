package net.digitalid.service.core.exceptions.external.encoding;

import javax.annotation.Nonnull;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This exception is thrown when a value decoded from a block is invalid.
 */
@Immutable
public class InvalidParameterValueException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Parameter -------------------------------------------------- */
    
    /**
     * Stores the parameter name whose value is invalid.
     */
    private final @Nonnull String parameter;
    
    /**
     * Returns the parameter name whose value is invalid.
     * 
     * @return the parameter name whose value is invalid.
     */
    @Pure
    public final @Nonnull String getParameter() {
        return parameter;
    }
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Stores the value which is invalid.
     */
    private final @Nonnull Object value;
    
    /**
     * Returns the value which is invalid.
     * 
     * @return the value which is invalid.
     */
    @Pure
    public final @Nonnull Object getValue() {
        return value;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid parameter value exception with the given parameter and value.
     * 
     * @param parameter the parameter name whose value is invalid.
     * @param value the value decoded from a block which is invalid.
     */
    protected InvalidParameterValueException(@Nonnull String parameter, @Nonnull Object value) {
        super("The " + parameter + " '" + value + "' " + (parameter.endsWith("s") ? "are" : "is") + " invalid.");
        
        this.parameter = parameter;
        this.value = value;
    }
    
    /**
     * Returns a new invalid parameter value exception with the given parameter and value.
     * 
     * @param parameter the parameter name whose value is invalid.
     * @param value the value decoded from a block which is invalid.
     * 
     * @return a new invalid parameter value exception with the given parameter and value.
     */
    @Pure
    public static @Nonnull InvalidParameterValueException get(@Nonnull String parameter, @Nonnull Object value) {
        return new InvalidParameterValueException(parameter, value);
    }
    
}
