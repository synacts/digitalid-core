package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.contact.Context;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.handler.InternalAction;
import ch.virtualid.handler.Method;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.IdentifierClass;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identifier.NonHostIdentifier;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.identity.Successor;
import ch.virtualid.module.BothModule;
import ch.virtualid.module.CoreService;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Closes the account and sets the given successor.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class AccountClose extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code close.account@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("close.account@virtualid.ch").load(InternalNonHostIdentity.IDENTIFIER);
    
    
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
     * 
     * @require role.isNative() : "The role is native.";
     */
    public AccountClose(@Nonnull Role role, @Nullable InternalNonHostIdentifier successor) {
        super(role);
        
        assert role.isNative() : "The role is native.";
        
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
    private AccountClose(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        super(entity, signature, recipient);
        
        this.successor = IdentifierClass.create(block).toInternalNonHostIdentifier();
        this.restrictions = new Restrictions(true, true, true, Context.getRoot(entity.toNonHostEntity()));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        assert successor != null : "The successor may only be null to reverse account initialization.";
        return successor.toBlock().setType(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Closes the given account with the successor " + successor + ".";
    }
    
    
    /**
     * Stores the required restrictions.
     */
    private final @Nonnull Restrictions restrictions;
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return AgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return restrictions;
    }
    
    @Pure
    @Override
    public @Nonnull ReadonlyAgentPermissions getAuditPermissions() {
        return AgentPermissions.GENERAL_WRITE;
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return restrictions;
    }
    
    
    @Override
    protected void executeOnBoth() throws SQLException {
        CoreService.SERVICE.removeState(getNonHostEntity());
        if (successor != null) Successor.set((NonHostIdentifier) getSubject(), successor, null);
    }
    
    @Pure
    @Override
    public @Nullable InternalAction getReverse() {
        return null;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull BothModule getModule() {
        return CoreService.SERVICE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
            return new AccountClose(entity, signature, recipient, block);
        }
        
    }
    
}
