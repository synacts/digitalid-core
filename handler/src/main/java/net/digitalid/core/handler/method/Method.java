package net.digitalid.core.handler.method;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.annotations.OnHost;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.Handler;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.SemanticType;
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
public abstract class Method<E extends Entity> extends Handler<E> {
    
    /**
     * Creates a method that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     * 
     * @ensure isNonHost() : "This method belongs to a non-host.";
     */
    protected Method(@Nullable NonHostEntity entity, @Nonnull InternalIdentifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject);
        
        Require.that(!(entity instanceof Account) || canBeSentByHosts()).orThrow("Methods encoded on hosts can be sent by hosts.");
        Require.that(!(entity instanceof Role) || !canOnlyBeSentByHosts()).orThrow("Methods encoded on clients cannot only be sent by hosts.");
        
        this.recipient = recipient;
    }
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient of this method.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getRecipient();
    
    
    /**
     * Returns whether this method needs to be lodged.
     */
    @Pure
    public abstract boolean isLodged();
    
    /**
     * Returns either the value b' for clients or the value f' for hosts or null if no credential is shortened.
     */
    @Pure
    public @Nullable BigInteger getValue() {
        return null;
    }
    
    /**
     * Returns whether this method can be sent by hosts.
     */
    @Pure
    public abstract boolean canBeSentByHosts();
    
    /**
     * Returns whether this method can only be sent by hosts.
     */
    @Pure
    public abstract boolean canOnlyBeSentByHosts();
    
    /**
     * Returns the permissions required for this method.
     */
    @Pure
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return FreezableAgentPermissions.NONE;
    }
    
    
    /**
     * Executes this method on the host.
     * 
     * @return a reply for this method or null.
     * 
     * @throws RequestException if the authorization is not sufficient.
     * 
     * @require hasSignature() : "This handler has a signature.";
     * 
     * @ensure matches(return) : "This method matches the returned reply.";
     */
    @OnHost
    @NonCommitting
    public abstract @Nullable Reply<E> executeOnHost() throws RequestException, DatabaseException;
    
    /**
     * Returns whether this method matches the given reply.
     * 
     * @return whether this method matches the given reply.
     */
    @Pure
    // TODO: Make the return type void and throw a InvalidReplyParameterValueException instead?
    public abstract boolean matches(@Nullable Reply reply);
    
    /* -------------------------------------------------- Send -------------------------------------------------- */
    
    @Impure
    @NonCommitting
    public <R extends Reply<E>> @Nullable R send() throws ExternalException {
        return MethodSender.send(this);
    }
    
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
    
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("\"").append(getClass().getSimpleName()).append(" to ").append(getSubject());
        if (hasEntity()) { string.append(" by ").append(getEntityNotNull().getIdentity().getAddress()); }
        string.append(": ").append(getDescription()).append("\"");
        return string.toString();
    }
    
    @Pure
    @Override
    protected final boolean protectedEquals(@Nullable Object object) {
        return super.protectedEquals(object) && object instanceof Method && this.recipient.equals(((Method) object).recipient);
    }
    
    @Pure
    @Override
    protected final int protectedHashCode() {
        return 89 * super.protectedHashCode() + recipient.hashCode();
    }
    
    /* -------------------------------------------------- Registry -------------------------------------------------- */
    
    /**
     * Each method needs to {@link #add(net.digitalid.service.core.identity.SemanticType, net.digitalid.service.core.handler.Method.Factory) register} a factory that inherits from this class.
     */
    protected static abstract class Factory<E extends Entity> {
        
        /**
         * Creates a method that handles contents of the indicated type.
         * 
         * @param entity the entity to which the returned method belongs
         * @param signature the signature of the returned method (or a dummy that just contains a subject).
         * @param recipient the recipient of the returned method.
         * @param block the content which is to be handled.
         * 
         * @return a new method that decodes the given block.
         * 
         * @require signature.hasSubject() : "The signature has a subject.";
         * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
         * 
         * @ensure return.hasEntity() : "The returned method has an entity.";
         * @ensure return.hasSignature() : "The returned method has a signature.";
         */
        @Pure
        @NonCommitting
        protected abstract @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException;
        
    }
    
    
    /**
     * Maps method types to the factory that creates handlers for that type.
     */
    private static final @Nonnull Map<SemanticType, Factory> converters = new ConcurrentHashMap<>();
    
    /**
     * Adds the given factory that creates handlers for the given type.
     * 
     * @param type the type to handle.
     * @param factory the factory to add.
     */
    protected static void add(@Nonnull SemanticType type, @Nonnull Factory factory) {
        converters.put(type, factory);
    }
    
    /**
     * Returns a method that handles the given block.
     * 
     * @param entity the entity to which the content belongs.
     * @param signature the signature of the content.
     * @param recipient the recipient of the content.
     * @param block the content which is to be decoded.
     * 
     * @return a method that handles the given block.
     * 
     * @throws RequestException if no handler is found for the given content type.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure return.hasEntity() : "The returned method has an entity.";
     * @ensure return.hasSignature() : "The returned method has a signature.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Method get(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
        final @Nullable Method.Factory factory = converters.get(block.getType());
        if (factory == null) { throw RequestException.get(RequestErrorCode.METHOD, "No method could be found for the type " + block.getType().getAddress() + "."); }
        else { return factory.create(entity, signature, recipient, block); }
    }
    
}
