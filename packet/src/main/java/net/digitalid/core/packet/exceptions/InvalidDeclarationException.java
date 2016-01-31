package net.digitalid.core.packet.exceptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.Identifier;

/**
 * This exception is thrown when an identity has an invalid declaration.
 */
@Immutable
public class InvalidDeclarationException extends ExternalException {
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Stores the identifier that has an invalid declaration.
     */
    private final @Nonnull Identifier identifier;
    
    /**
     * Returns the identifier that has an invalid declaration.
     * 
     * @return the identifier that has an invalid declaration.
     */
    @Pure
    public @Nonnull Identifier getIdentifier() {
        return identifier;
    }
    
    /* -------------------------------------------------- Reply -------------------------------------------------- */
    
    /**
     * Stores the reply that contains the invalid declaration.
     */
    private final @Nullable Reply reply;
    
    /**
     * Returns the reply that contains the invalid declaration.
     * 
     * @return the reply that contains the invalid declaration.
     */
    @Pure
    public @Nullable Reply getReply() {
        return reply;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new invalid declaration exception with the given message, identifier and reply.
     * 
     * @param message a string explaining the problem of the invalid declaration.
     * @param identifier the identifier that has an invalid declaration.
     * @param reply the reply that contains the invalid declaration.
     */
    protected InvalidDeclarationException(@Nonnull String message, @Nonnull Identifier identifier, @Nullable Reply reply) {
        super(identifier.toString() + " has an invalid declaration: " + message);
        
        this.identifier = identifier;
        this.reply = reply;
    }
    
    /**
     * Returns a new invalid declaration exception with the given message, identifier and reply.
     * 
     * @param message a string explaining the problem of the invalid declaration.
     * @param identifier the identifier that has an invalid declaration.
     * @param reply the reply that contains the invalid declaration.
     * 
     * @return a new invalid declaration exception with the given message, identifier and reply.
     */
    @Pure
    public static @Nonnull InvalidDeclarationException get(@Nonnull String message, @Nonnull Identifier identifier, @Nullable Reply reply) {
        return new InvalidDeclarationException(message, identifier, reply);
    }
    
    /**
     * Returns a new invalid declaration exception with the given message and identifier.
     * 
     * @param message a string explaining the problem of the invalid declaration.
     * @param identifier the identifier that has an invalid declaration.
     * 
     * @return a new invalid declaration exception with the given message and identifier.
     */
    @Pure
    public static @Nonnull InvalidDeclarationException get(@Nonnull String message, @Nonnull Identifier identifier) {
        return new InvalidDeclarationException(message, identifier, null);
    }
    
}
