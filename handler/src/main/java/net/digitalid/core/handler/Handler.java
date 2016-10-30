package net.digitalid.core.handler;

import java.util.Objects;

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
    @Deprecated // TODO: Are such shortcuts desirable?
    public boolean isOnHost() {
        final @Nullable E entity = getEntity();
        return entity != null && entity.isOnHost();
    }
    
    /**
     * Returns whether this entity is on a client.
     */
    @Pure
    @Deprecated // TODO: Are such shortcuts desirable?
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
    public abstract @Nullable Signature<?> getSignature(); // TODO: Provide the correct type argument if possible.
    
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
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    // TODO: The following methods are probably best generated.
    
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
        if (object == this) { return true; }
        if (object == null || !(object instanceof Handler)) { return false; }
        @SuppressWarnings("rawtypes")
        final @Nonnull Handler other = (Handler) object;
        return Objects.equals(this.getEntity(), other.getEntity()) && Objects.equals(this.getSubject(), other.getSubject());
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
        hash = 89 * hash + Objects.hashCode(this.getEntity());
        hash = 89 * hash + Objects.hashCode(this.getSubject());
        return hash;
    }
    
    @Pure
    @Override
    public abstract boolean equals(@Nullable Object object);
    
    @Pure
    @Override
    public abstract int hashCode();
    
}
