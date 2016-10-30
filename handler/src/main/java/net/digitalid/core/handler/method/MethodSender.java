package net.digitalid.core.handler.method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.reply.Reply;

/**
 * The method sender sends a method.
 */
@Stateless
public interface MethodSender {
    
    /* -------------------------------------------------- Interface -------------------------------------------------- */
    
    /**
     * Sends the given method and returns its reply (if there is one).
     */
    @Impure
    @NonCommitting
    public <E extends Entity, R extends Reply<E>> @Nullable R getReply(@Nonnull Method<E> method) throws ExternalException;
    
    /* -------------------------------------------------- Configuration -------------------------------------------------- */
    
    /**
     * Stores the method sender, which has to be provided by the initializer package.
     */
    public static final @Nonnull Configuration<MethodSender> configuration = Configuration.withUnknownProvider();
    
    /* -------------------------------------------------- Static Access -------------------------------------------------- */
    
    /**
     * Retrieves the public key of the given internal identity at the given time.
     */
    @Impure
    @NonCommitting
    public static <E extends Entity, R extends Reply<E>> @Nullable R send(@Nonnull Method<E> method) throws ExternalException {
        return configuration.get().getReply(method);
    }
    
}
