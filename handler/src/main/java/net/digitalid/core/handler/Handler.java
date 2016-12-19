package net.digitalid.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.annotations.SiteDependency;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.selfcontained.Selfcontained;
import net.digitalid.core.service.Service;
import net.digitalid.core.signature.Signature;

/**
 * This type provides the features that all handlers share.
 * 
 * @see Method
 * @see Reply
 */
@Immutable
public interface Handler<ENTITY extends Entity<?>> extends RootInterface, SiteDependency {
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Returns the signature of this handler.
     */
    @Pure
    @Provided
    @Default("null")
    public @Nullable Signature<?> getSignature();
    
    /**
     * Returns whether this handler will be sent.
     */
    @Pure
    public default boolean willBeSent() {
        return getSignature() == null;
    }
    
    /**
     * Returns whether this handler has been received.
     */
    @Pure
    public default boolean hasBeenReceived() {
        return getSignature() != null;
    }
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity that was provided with the builder.
     */
    @Pure
    @Default("null")
    @NonRepresentative
    public @Nullable ENTITY getProvidedEntity();
    
    /**
     * Returns the entity to which this handler belongs or null if it is impersonal.
     */
    @Pure
    @Derive("signature != null ? null /* Find a way to derive it from signature.getSubject(), probably make it injectable. */ : providedEntity")
    public @Nullable ENTITY getEntity();
    
    @Pure
    @Override
    public default boolean isOnHost() {
        final @Nullable ENTITY entity = getEntity();
        return entity != null && entity.isOnHost();
    }
    
    @Pure
    @Override
    public default boolean isOnClient() {
        final @Nullable ENTITY entity = getEntity();
        return entity == null || entity.isOnClient();
    }
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    /**
     * Returns the entity that was provided with the builder.
     */
    @Pure
    @Default("null")
    @NonRepresentative
    public @Nullable InternalIdentifier getProvidedSubject();
    
    /**
     * Returns the subject of this handler.
     * 
     * The subject is stored as an identifier because for certain handlers
     * the corresponding identity is not known to (or does not yet) exist.
     */
    @Pure
    @Derive("signature != null ? signature.getSubject() : providedSubject")
    public @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Other -------------------------------------------------- */
    
    /**
     * Returns the type of packets that this handler handles.
     */
    @Pure
    public @Nonnull SemanticType getType();
    
    /**
     * Returns the service that this handler implements.
     */
    @Pure
    public @Nonnull Service getService();
    
    /**
     * Returns a description of this handler.
     */
    @Pure
    public @Nonnull String getDescription();
    
    /* -------------------------------------------------- Conversion -------------------------------------------------- */
    
    @Pure
    @TODO(task = "Is this the right approach? If yes, we should have this generated automatically.", date = "2016-12-19", author = Author.KASPAR_ETTER)
    public default @Nonnull Selfcontained convert() throws ExternalException {
        return null;
    }
    
}
