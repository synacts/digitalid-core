package net.digitalid.core.handler.method;

import java.math.BigInteger;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.annotations.OnHostRecipient;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.Handler;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;

/**
 * This class implements a remote method invocation mechanism.
 * All methods have to extend this class and {@link #add(net.digitalid.service.core.identity.SemanticType, net.digitalid.service.core.handler.Method.Factory) register} themselves as handlers.
 * 
 * @see Action
 * @see Query
 */
@Immutable
public abstract class Method<E extends Entity> extends RootClass implements Handler<E> {
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient of this method.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getRecipient();
    
    /* -------------------------------------------------- Lodged -------------------------------------------------- */
    
    /**
     * Returns whether this method needs to be lodged.
     */
    @Pure
    public abstract boolean isLodged();
    
    /* -------------------------------------------------- Value -------------------------------------------------- */
    
    /**
     * Returns either the value b' for clients or the value f' for hosts or null if no credential is shortened.
     */
    @Pure
    @Default("null")
    public abstract @Nullable BigInteger getValue();
    
    /* -------------------------------------------------- Requirements -------------------------------------------------- */
    
    /**
     * Returns whether this method can be sent by hosts.
     */
    @Pure
    public abstract boolean canBeSentByHosts();
    
    /**
     * Returns whether this method can be sent by clients.
     */
    @Pure
    public abstract boolean canBeSentByClients();
    
    /**
     * Returns the permissions required for this method.
     */
    @Pure
    public @Nonnull @Frozen ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return FreezableAgentPermissions.NONE;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * Returns whether this method matches the given reply.
     */
    @Pure
    // TODO: Rather move this method to the reply class (and match methods that generate replies)?
    // TODO: Make the return type void and throw a InvalidReplyParameterValueException instead?
    public abstract boolean matches(@Nullable Reply<E> reply);
    
    /**
     * Executes this method on the host.
     * 
     * @return a reply for this method or null.
     * 
     * @throws RequestException if the authorization is not sufficient.
     * 
     * @require hasBeenReceived() : "This method has been received.";
     * 
     * @ensure matches(return) : "This method matches the returned reply.";
     */
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    public abstract @Nullable Reply<E> executeOnHost() throws RequestException, DatabaseException;
    
    /* -------------------------------------------------- Send -------------------------------------------------- */
    
    @NonCommitting
    @PureWithSideEffects
    public <R extends Reply<E>> @Nullable R send() throws ExternalException {
        // TODO (see net.digitalid.core.initializer.MethodSenderImplementation)
        return null;
    }
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
    /**
     * Returns whether the other method is similar to this one.
     * Handlers are similar if they<br>
     * - have the same entity, subject and recipient, and<br>
     * - belong to the same service and the same class of methods.<br>
     * (The latter is implemented by inheritance and dynamic method binding.)
     * <p>
     * You can override this method and return {@code false} if this method
     * should be sent alone (e.g. due to an overridden {@link #send()} method).
     * The implementation has to be transitive but must not be reflexive.
     * 
     * @param other the other method to compare this one with.
     * 
     * @return whether the other method is similar to this one.
     */
    @Pure
    public boolean isSimilarTo(@Nonnull Method<?> other) {
        return Objects.equals(this.getEntity(), other.getEntity())
                && this.getSubject().equals(other.getSubject())
                && this.getRecipient().equals(other.getRecipient())
                && this.getService().equals(other.getService());
    }
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Validate.that(!willBeSent() || !isOnHost() || canBeSentByHosts()).orThrow("Methods to be sent on hosts have to be sendable by hosts.");
        Validate.that(!willBeSent() || !isOnClient() || canBeSentByClients()).orThrow("Methods to be sent on clients have to be sendable by clients.");
        Validate.that(!hasBeenReceived() || isOnHost()).orThrow("Methods can only be received on hosts and the entity may not be null then.");
    }
    
}
