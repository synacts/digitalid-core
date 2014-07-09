package ch.virtualid.handler;

import ch.virtualid.annotation.Pure;
import ch.virtualid.database.Entity;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.BlockableObject;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class provides the features that all handlers share.
 * 
 * @see Reply
 * @see SendableHandler
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public abstract class Handler extends /* Concept */ BlockableObject implements Immutable {
    
    /**
     * Stores the semantic type {@code @virtualid.ch}.
     */
    public static final @Nonnull SemanticType CORE_SERVICE = mapSemanticType(NonHostIdentifier.CORE_SERVICE);
    
    
    /**
     * Stores the signature of this handler.
     */
    private final @Nullable SignatureWrapper signature;
    
    /**
     * Stores the subject of this handler.
     * It is stored as an identifier because for certain handlers
     * the corresponding identity is not known to (or does not yet) exist.
     */
    private final @Nonnull Identifier subject;
    
    /**
     * Creates a handler that decodes the given signature and block for the given entity.
     * 
     * @param block the element of the content.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected Handler(@Nonnull Block block, @Nullable Entity entity, @Nonnull SignatureWrapper signature) {
        super(block, entity);
        
        this.signature = signature;
        this.subject = signature.getSubjectNotNull();
    }
    
    /**
     * Creates a handler that encodes the content of a packet about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     */
    protected Handler(@Nullable Entity entity, @Nonnull Identifier subject) {
        super(entity);
        
        this.signature = null;
        this.subject = subject;
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
     * Returns the subject of this handler.
     * 
     * @return the subject of this handler.
     */
    @Pure
    public final @Nonnull Identifier getSubject() {
        return subject;
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
    public abstract @Nonnull SemanticType getService();
    
}
