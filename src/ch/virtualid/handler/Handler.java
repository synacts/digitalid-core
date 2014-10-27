package ch.virtualid.handler;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Concept;
import ch.virtualid.entity.Entity;
import ch.virtualid.identifier.Identifier;
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
public abstract class Handler extends Concept implements Immutable, Blockable {
    
    /**
     * Stores the subject of this handler.
     * 
     * The subject is stored as an identifier because for certain handlers
     * the corresponding identity is not known to (or does not yet) exist.
     */
    private final @Nonnull Identifier subject;
    
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
    protected Handler(@Nullable Entity entity, @Nonnull Identifier subject) {
        super(entity);
        
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
        super(entity);
        
        this.signature = signature;
        this.subject = signature.getSubjectNotNull();
    }
    
    /**
     * Returns a description of this handler.
     * 
     * @return a description of this handler.
     */
    @Pure
    @Override
    public abstract @Nonnull String toString();
    
    
    /**
     * Returns the subject of this handler.
     * 
     * @return the subject of this handler.
     */
    @Pure
    public final @Nonnull Identifier getSubject() {
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
     * A factory creates handlers that can handle contents of the indicated type.
     */
    protected static abstract class Factory {
        
        /**
         * Returns the type of contents that handlers created by this factory handle.
         * 
         * @return the type of contents that handlers created by this factory handle.
         */
        @Pure
        public abstract @Nonnull SemanticType getType();
        
    }
    
}
