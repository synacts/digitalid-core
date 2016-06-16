package net.digitalid.core.conversion.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.service.core.handler.Reply;

/**
 * This exception is thrown when a reply parameter does not match the corresponding query.
 */
@Immutable
public class InvalidReplyParameterValueException extends InvalidEncodingException {
    
    /* -------------------------------------------------- Reply -------------------------------------------------- */
    
    /**
     * Stores the reply whose parameter does not match the corresponding query.
     */
    private final @Nullable Reply reply;
    
    /**
     * Returns the reply whose parameter does not match the corresponding query.
     * 
     * @return the reply whose parameter does not match the corresponding query.
     */
    @Pure
    public final @Nullable Reply getReply() {
        return reply;
    }
    
    /* -------------------------------------------------- Parameter -------------------------------------------------- */
    
    /**
     * Stores the parameter whose value is different than expected.
     */
    private final @Nonnull String parameter;
    
    /**
     * Returns the parameter whose value is different than expected.
     * 
     * @return the parameter whose value is different than expected.
     */
    @Pure
    public final @Nonnull String getParameter() {
        return parameter;
    }
    
    /* -------------------------------------------------- Expected Value -------------------------------------------------- */
    
    /**
     * Stores the expected value in the reply.
     */
    private final @Nonnull Object expectedValue;
    
    /**
     * Returns the expected value in the reply.
     * 
     * @return the expected value in the reply.
     */
    @Pure
    public final @Nonnull Object getExpectedValue() {
        return expectedValue;
    }
    
    /* -------------------------------------------------- Encountered Value -------------------------------------------------- */
    
    /**
     * Stores the encountered value in the reply.
     */
    private final @Nonnull Object encounteredValue;
    
    /**
     * Returns the encountered value in the reply.
     * 
     * @return the encountered value in the reply.
     */
    @Pure
    public final @Nonnull Object getEncounteredValue() {
        return encounteredValue;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid reply parameter exception with the given parameters.
     * 
     * @param reply the reply whose parameter does not match the corresponding query.
     * @param parameter the parameter whose value is different than expected.
     * @param expectedValue the expected value in the reply.
     * @param encounteredValue the encountered value in the reply.
     */
    protected InvalidReplyParameterValueException(@Nullable Reply reply, @Nonnull String parameter, @Nonnull Object expectedValue, @Nonnull Object encounteredValue) {
        super("A reply" + (reply == null ? "" : " of the type '" + reply.getClass().getSimpleName() + "'" + (reply.hasEntity() ? " of the user " + reply.getEntityNotNull().getIdentity().getAddress() : "")) + " does not match the corresponding query. (The expected " + parameter + " was " + expectedValue + " but the encountered " + parameter + " was " + encounteredValue + ".");
        
        this.reply = reply;
        this.parameter = parameter;
        this.expectedValue = expectedValue;
        this.encounteredValue = encounteredValue;
    }
    
    /**
     * Returns a new invalid reply parameter exception with the given parameters.
     * 
     * @param reply the reply whose parameter does not match the corresponding query.
     * @param parameter the parameter whose value is different than expected.
     * @param expectedValue the expected value in the reply.
     * @param encounteredValue the encountered value in the reply.
     * 
     * @return a new invalid reply parameter exception with the given parameters.
     */
    @Pure
    public static @Nonnull InvalidReplyParameterValueException get(@Nullable Reply reply, @Nonnull String parameter, @Nonnull Object expectedValue, @Nonnull Object encounteredValue) {
        return new InvalidReplyParameterValueException(reply, parameter, expectedValue, encounteredValue);
    }
    
}
