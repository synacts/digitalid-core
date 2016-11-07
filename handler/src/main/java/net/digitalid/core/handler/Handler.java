package net.digitalid.core.handler;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.service.Service;
import net.digitalid.core.signature.Signature;

/**
 * This class provides the features that all handlers share.
 * 
 * @see Method
 * @see Reply
 */
@Immutable
public abstract class Handler<E extends Entity> extends RootClass {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity to which this handler belongs or null if it is impersonal.
     */
    @Pure
    @Provided
    public abstract @Nullable E getEntity();
    
    /**
     * Returns whether this handler is on a host.
     */
    @Pure
    public boolean isOnHost() {
        final @Nullable E entity = getEntity();
        return entity != null && entity.isOnHost();
    }
    
    /**
     * Returns whether this handler is on a client.
     */
    @Pure
    public boolean isOnClient() {
        final @Nullable E entity = getEntity();
        return entity != null && entity.isOnClient();
    }
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    /**
     * Returns the subject of this handler.
     * 
     * The subject is stored as an identifier because for certain handlers
     * the corresponding identity is not known to (or does not yet) exist.
     */
    @Pure
    @Default("signature == null ? null : signature.getSubject()") // TODO: This probably does not work like this.
    public abstract @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Returns the signature of this handler.
     */
    @Pure
    @Provided
    public abstract @Nullable Signature<?> getSignature();
    
    /**
     * Returns whether this handler will be sent.
     */
    @Pure
    public boolean willBeSent() {
        return getSignature() == null;
    }
    
    /**
     * Returns whether this handler has been received.
     */
    @Pure
    public boolean hasBeenReceived() {
        return getSignature() != null;
    }
    
    /* -------------------------------------------------- Other -------------------------------------------------- */
    
    /**
     * Returns the type of packets that this handler handles.
     */
    @Pure
    public abstract @Nonnull SemanticType getType();
    
    /**
     * Returns the service that this handler implements.
     */
    @Pure
    public abstract @Nonnull Service getService();
    
    /**
     * Returns a description of this handler.
     */
    @Pure
    public abstract @Nonnull String getDescription();
    
}
