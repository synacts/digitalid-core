package net.digitalid.core.client;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.context.Context;
import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NativeRole;
import net.digitalid.core.exceptions.RequestException;
import net.digitalid.core.handler.Action;
import net.digitalid.core.handler.InternalAction;
import net.digitalid.core.handler.Method;
import net.digitalid.core.handler.core.CoreServiceInternalAction;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierImplementation;
import net.digitalid.core.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identifier.NonHostIdentifier;
import net.digitalid.core.identity.InternalNonHostIdentity;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.resolution.Successor;
import net.digitalid.core.service.CoreService;

import net.digitalid.service.core.dataservice.StateModule;

/**
 * Closes the account and sets the given successor.
 */
@Immutable
public final class AccountClose extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code close.account@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("close.account@core.digitalid.net").load(InternalNonHostIdentity.IDENTIFIER);
    
    
    /**
     * Stores the successor of the given account.
     */
    private final @Nullable InternalNonHostIdentifier successor;
    
    /**
     * Creates an action to close a given account.
     * <p>
     * <em>Important:</em> The successor may only be null
     * in case {@link AccountInitialize} is to be reversed.
     * 
     * @param role the role to which this handler belongs.
     * @param successor the successor of the given account.
     */
    AccountClose(@Nonnull NativeRole role, @Nullable InternalNonHostIdentifier successor) {
        super(role);
        
        this.successor = successor;
        this.restrictions = new Restrictions(true, true, true, Context.getRoot(role));
    }
    
    /**
     * Creates an action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    private AccountClose(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        this.successor = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, block).castTo(InternalNonHostIdentifier.class);
        this.restrictions = new Restrictions(true, true, true, Context.getRoot(entity.castTo(NonHostEntity.class)));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        Require.that(successor != null).orThrow("The successor may only be null to reverse account initialization.");
        return successor.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Closes the given account with the successor " + successor + ".";
    }
    
    
    /**
     * Stores the required restrictions.
     */
    private final @Nonnull Restrictions restrictions;
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return FreezableAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return restrictions;
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
        return FreezableAgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return restrictions;
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws DatabaseException {
        CoreService.SERVICE.removeState(getNonHostEntity());
        if (successor != null) { Successor.set((NonHostIdentifier) getSubject(), successor, null); }
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return false;
    }
    
    @Pure
    @Override
    public @Nullable InternalAction getReverse() {
        return null;
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AccountClose && Objects.equals(successor, ((AccountClose) object).successor);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + Objects.hashCode(successor);
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return CoreService.SERVICE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new AccountClose(entity, signature, recipient, block);
        }
        
    }
    
}
