package net.digitalid.core.synchronizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.exceptions.external.ExternalException;
import net.digitalid.utility.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.validation.state.Immutable;
import net.digitalid.utility.validation.state.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.annotations.HasSubject;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.service.core.concepts.agent.FreezableAgentPermissions;
import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.Role;

import net.digitalid.core.exceptions.RequestException;

import net.digitalid.service.core.handler.InternalQuery;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.Reply;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.IdentityImplementation;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.identity.annotations.BasedOn;
import net.digitalid.service.core.site.annotations.Hosts;
import net.digitalid.service.core.storage.Service;

/**
 * Queries the audit of the given service for the given role.
 * 
 * @see AuditReply
 */
@Immutable
final class AuditQuery extends InternalQuery {
    
    /**
     * Stores the semantic type {@code query.audit@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("query.audit@core.digitalid.net").load(SemanticType.IDENTIFIER);
    
    
    /**
     * Stores the service whose audit is queried.
     */
    private final @Nonnull Service service;
    
    /**
     * Stores the permissions of the querying agent or none.
     */
    private final @Nonnull ReadOnlyAgentPermissions permissions;
    
    /**
     * Creates an internal query for the audit of the given service.
     * 
     * @param role the role to which this handler belongs.
     * @param service the service whose audit is queried.
     */
    @NonCommitting
    AuditQuery(@Nonnull Role role, @Nonnull Service service) throws DatabaseException, RequestException, InvalidEncodingException {
        super(role, service.getRecipient(role));
        
        this.service = service;
        this.permissions = role.getAgent().getPermissions();
    }
    
    /**
     * Creates an internal query that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler.
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     */
    @Hosts
    @NonCommitting
    private AuditQuery(@Nonnull Entity entity, @Nonnull @HasSubject SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull @BasedOn("query.audit@core.digitalid.net") Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity.castTo(NonHostEntity.class), signature, recipient);
        
        this.service = Service.getService(IdentityImplementation.create(block).castTo(SemanticType.class));
        this.permissions = FreezableAgentPermissions.NONE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return service.getType().toBlock(TYPE);
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Queries the audit of the " + service.getName() + ".";
    }
    
    
    @Pure
    @Override
    public @Nonnull Service getService() {
        return service;
    }
    
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return permissions;
    }
    
    
    @Override
    public @Nonnull AuditReply executeOnHost() {
        return new AuditReply(getNonHostAccount(), service);
    }
    
    @Pure
    @Override
    public boolean matches(@Nullable Reply reply) {
        return reply instanceof AuditReply && ((AuditReply) reply).getService().equals(service);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        return protectedEquals(object) && object instanceof AuditQuery && this.service.equals(((AuditQuery) object).service);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return 89 * protectedHashCode() + service.hashCode();
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
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
            return new AuditQuery(entity, signature, recipient, block);
        }
        
    }
    
}
