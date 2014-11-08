package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.module.Service;
import ch.xdf.SignatureWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides the features that all handlers share.
 * 
 * @see Method
 * @see Reply
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public abstract class Handler implements Immutable, Blockable {
    
    /**
     * Stores the entity to which this handler belongs or null if it is impersonal.
     */
    private final @Nullable Entity entity;
    
    /**
     * Stores the subject of this handler.
     * 
     * The subject is stored as an identifier because for certain handlers
     * the corresponding identity is not known to (or does not yet) exist.
     */
    private final @Nonnull InternalIdentifier subject;
    
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
    protected Handler(@Nullable Entity entity, @Nonnull InternalIdentifier subject) {
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
    protected Handler(@Nullable Entity entity, @Nonnull SignatureWrapper signature) {
        this.entity = entity;
        this.signature = signature;
        this.subject = signature.getSubjectNotNull();
    }
    
    /**
     * Returns the entity to which this handler belongs or null if it is impersonal.
     * 
     * @return the entity to which this handler belongs or null if it is impersonal.
     */
    @Pure
    public final @Nullable Entity getEntity() {
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
    public final @Nonnull Entity getEntityNotNull() {
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
     * Returns the subject of this handler.
     * 
     * @return the subject of this handler.
     */
    @Pure
    public final @Nonnull InternalIdentifier getSubject() {
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
    @Override
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
    @Override
    public abstract @Nonnull String toString();
    
}
