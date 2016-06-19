package net.digitalid.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.client.Client;
import net.digitalid.core.client.annotations.Clients;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestErrorCode;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.handler.core.CoreServiceInternalAction;
import net.digitalid.core.host.annotations.Hosts;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.synchronizer.Synchronizer;

/**
 * Internal actions can only be sent by {@link Client clients} and can usually be {@link #reverseOnClient() reversed}.
 * They are always signed identity-based and an audit request or trail is appended during {@link Package packaging}.
 * <p>
 * <em>Important:</em> Do not execute internal actions directly but always pass them to the {@link Synchronizer#execute(net.digitalid.service.core.handler.InternalAction) Synchronizer}!
 * 
 * @invariant hasEntity() : "This internal action has an entity.";
 * @invariant isNonHost() : "This internal action belongs to a non-host.";
 * @invariant getEntityNotNull().getIdentity().equals(getSubject().getIdentity()) : "The identity of the entity and the subject are the same.";
 * 
 * @see CoreServiceInternalAction
 */
@Immutable
public abstract class InternalAction extends Action implements InternalMethod {
    
    /**
     * Creates an internal action that encodes the content of a packet for the given recipient.
     * 
     * @param role the role to which this handler belongs.
     * @param recipient the recipient of this method.
     */
    @Clients
    protected InternalAction(@Nonnull Role role, @Nonnull HostIdentifier recipient) {
        super(role, role.getIdentity().getAddress(), recipient);
    }
    
    /**
     * Creates an internal action that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    protected InternalAction(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws ExternalException {
        super(entity, signature, recipient);
        
        if (!isNonHost()) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "Internal actions have to belong to a non-host."); }
        if (!getEntityNotNull().getIdentity().equals(getSubject().getIdentity())) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The identity of the entity and the subject have to be the same for internal actions."); }
    }
    
    
    @Pure
    @Override
    public boolean isSimilarTo(@Nonnull Method other) {
        return super.isSimilarTo(other) && other instanceof InternalAction;
    }
    
    @Pure
    @Override
    public final boolean canBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public final boolean canOnlyBeSentByHosts() {
        return false;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return Restrictions.MIN;
    }
    
    
    /**
     * Executes this internal action on the host.
     * 
     * @throws RequestException if the authorization is not sufficient.
     * 
     * @require hasSignature() : "This handler has a signature.";
     */
    @Hosts
    @NonCommitting
    protected abstract void executeOnHostInternalAction() throws RequestException, DatabaseException;
    
    @Override
    @Hosts
    @NonCommitting
    public final @Nullable ActionReply executeOnHost() throws RequestException, DatabaseException {
        executeOnHostInternalAction();
        return null;
    }
    
    @Pure
    @Override
    public final boolean matches(@Nullable Reply reply) {
        return reply == null;
    }
    
    
    /**
     * Returns whether this internal action interferes with the given action.
     * 
     * @param action the action which is to be checked for interference.
     * 
     * @return whether this internal action interferes with the given action.
     * 
     * @require action.getRole().equals(getRole()) : "The role of the given and this action is the same.";
     * @require action.getService().equals(getService()) : "The service of the given and this action is the same.";
     */
    @Pure
    public abstract boolean interferesWith(@Nonnull Action action);
    
    /**
     * Returns the reverse of this action or null if this action cannot be reversed.
     * 
     * @return the reverse of this action or null if this action cannot be reversed.
     */
    @Pure
    @Clients
    public abstract @Nullable InternalAction getReverse() throws DatabaseException;
    
    /**
     * Reverses this internal action on the client if this action can be reversed.
     */
    @NonCommitting
    @Clients
    public final void reverseOnClient() throws DatabaseException {
        Require.that(isOnClient()).orThrow("This method is called on a client.");
        
        final @Nullable InternalAction reverse = getReverse();
        if (reverse != null) { reverse.executeOnClient(); }
    }
    
}
