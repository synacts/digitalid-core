package net.digitalid.service.core.handler;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.block.wrappers.SignatureWrapper;
import net.digitalid.service.core.storage.Service;
import net.digitalid.service.core.entity.Account;
import net.digitalid.service.core.entity.Entity;
import net.digitalid.service.core.entity.NonHostAccount;
import net.digitalid.service.core.entity.NonHostEntity;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.factory.encoding.Encodable;
import net.digitalid.service.core.identifier.InternalIdentifier;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;

/**
 * This class provides the features that all handlers share.
 * 
 * @see Method
 * @see Reply
 */
@Immutable
public abstract class Handler<O, E> implements Encodable<O, E> {
    
    /**
     * Stores the entity to which this handler belongs or null if it is impersonal.
     */
    private final @Nullable Entity<?> entity;
    
    /**
     * Stores the subject of this handler.
     * 
     * The subject is stored as an identifier because for certain handlers
     * the corresponding identity is not known to (or does not yet) exist.
     */
    private final @Nonnull InternalIdentifier<?> subject;
    
    /**
     * Stores the signature of this handler.
     */
    private final @Nullable SignatureWrapper signature;
    
    /**
     * Creates a handler that encodes the content of a packet about the given subject.
     * 
     * @param entity the entity to which this handler belongs or null if it is impersonal.
     * @param subject the subject of this handler.
     */
    protected Handler(@Nullable Entity<?> entity, @Nonnull InternalIdentifier<?> subject) {
        this.entity = entity;
        this.signature = null;
        this.subject = subject;
    }
    
    /**
     * Creates a handler that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs or null if it is impersonal.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected Handler(@Nullable Entity<?> entity, @Nonnull SignatureWrapper signature) {
        this.entity = entity;
        this.signature = signature;
        this.subject = signature.getNonNullableSubject();
    }
    
    /**
     * Returns the entity to which this handler belongs or null if it is impersonal.
     * 
     * @return the entity to which this handler belongs or null if it is impersonal.
     */
    @Pure
    public final @Nullable Entity<?> getEntity() {
        return entity;
    }
    
    /**
     * Returns whether this handler has an entity.
     * 
     * @return whether this handler has an entity.
     */
    @Pure
    public final boolean hasEntity() {
        return entity != null;
    }
    
    /**
     * Returns the entity to which this handler belongs.
     * 
     * @return the entity to which this handler belongs.
     * 
     * @require hasEntity() : "This handler has an entity.";
     */
    @Pure
    public final @Nonnull Entity<?> getEntityNotNull() {
        assert entity != null : "This handler has an entity.";
        
        return entity;
    }
    
    /**
     * Returns whether this handler is on a host.
     * 
     * @return whether this handler is on a host.
     */
    @Pure
    public final boolean isOnHost() {
        return entity instanceof Account;
    }
    
    /**
     * Returns whether this handler is on a client.
     * 
     * @return whether this handler is on a client.
     */
    @Pure
    public final boolean isOnClient() {
        return entity instanceof Role;
    }
    
    /**
     * Returns the account to which this handler belongs.
     * 
     * @return the account to which this handler belongs.
     * 
     * @require isOnHost() : "This handler is on a host.";
     */
    @Pure
    public final @Nonnull Account getAccount() {
        assert isOnHost() : "This handler is on a host.";
        
        assert entity != null;
        return (Account) entity;
    }
    
    /**
     * Returns whether this handler belongs to a non-host.
     * 
     * @return whether this handler belongs to a non-host.
     */
    @Pure
    public final boolean isNonHost() {
        return entity instanceof NonHostEntity;
    }
    
    /**
     * Returns the non-host entity to which this handler belongs.
     * 
     * @return the non-host entity to which this handler belongs.
     * 
     * @require isNonHost() : "This handler belongs to a non-host.";
     */
    @Pure
    public final @Nonnull NonHostEntity getNonHostEntity() {
        assert isNonHost() : "This handler belongs to a non-host.";
        
        assert entity != null;
        return (NonHostEntity) entity;
    }
    
    /**
     * Returns the non-host account to which this handler belongs.
     * 
     * @return the non-host account to which this handler belongs.
     * 
     * @require isOnHost() : "This handler is on a host.";
     * @require isNonHost() : "This handler belongs to a non-host.";
     */
    @Pure
    public final @Nonnull NonHostAccount getNonHostAccount() {
        assert isOnHost() : "This handler is on a host.";
        assert isNonHost() : "This handler belongs to a non-host.";
        
        assert entity != null;
        return (NonHostAccount) entity;
    }
    
    /**
     * Returns the role to which this handler belongs.
     * 
     * @return the role to which this handler belongs.
     * 
     * @require isOnClient() : "This handler is on a client.";
     */
    @Pure
    public final @Nonnull Role getRole() {
        assert isOnClient() : "This handler is on a client.";
        
        assert entity != null;
        return (Role) entity;
    }
    
    /**
     * Returns the subject of this handler.
     * 
     * @return the subject of this handler.
     */
    @Pure
    public final @Nonnull InternalIdentifier<?> getSubject() {
        return subject;
    }
    
    /**
     * Returns the signature of this handler.
     * 
     * @return the signature of this handler.
     */
    @Pure
    public final @Nullable SignatureWrapper getSignature() {
        return signature;
    }
    
    /**
     * Returns whether this handler has a signature.
     * 
     * @return whether this handler has a signature.
     */
    @Pure
    public final boolean hasSignature() {
        return signature != null;
    }
    
    /**
     * Returns the signature of this handler.
     * 
     * @return the signature of this handler.
     * 
     * @require hasSignature() : "This handler has a signature.";
     */
    @Pure
    public final @Nonnull SignatureWrapper getSignatureNotNull() {
        assert signature != null : "This handler has a signature.";
        
        return signature;
    }
    
    
    /**
     * Returns the type of packets that this handler handles.
     * 
     * @return the type of packets that this handler handles.
     */
    @Pure
    public abstract @Nonnull SemanticType getType();
    
    /**
     * Returns the service that this handler implements.
     * 
     * @return the service that this handler implements.
     */
    @Pure
    public abstract @Nonnull Service getService();
    
    /**
     * Returns a description of this handler.
     * 
     * @return a description of this handler.
     */
    @Pure
    public abstract @Nonnull String getDescription();
    
    
    /**
     * Returns whether the given object is equal to this handler.
     * This method does not override {@link Object#equals(java.lang.Object)}
     * in order to enforce an equals implementation with an abstract method.
     * 
     * @param object the object to be checked for equality.
     * 
     * @return whether the given object is equal to this handler.
     */
    @Pure
    protected boolean protectedEquals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof Handler)) return false;
        @SuppressWarnings("rawtypes")
        final @Nonnull Handler other = (Handler) object;
        return Objects.equals(this.entity, other.entity) && Objects.equals(this.subject, other.subject);
    }
    
    /**
     * Returns the hash code of this method.
     * This method does not override {@link Object#hashCode()} in order
     * to enforce a hash code implementation with an abstract method.
     * 
     * @return the hash code of this method.
     */
    @Pure
    protected int protectedHashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.entity);
        hash = 89 * hash + Objects.hashCode(this.subject);
        return hash;
    }
    
    @Pure
    @Override
    public abstract boolean equals(@Nullable Object object);
    
    @Pure
    @Override
    public abstract int hashCode();
    
}
