package ch.virtualid.entity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Concept;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.handler.Handler;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.InternalIdentity;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import javax.annotation.Nonnull;

/**
 * An entity captures the {@link Site site} and the {@link Identity identity} of a {@link Concept concept} or {@link Handler handler}.
 * 
 * @see EntityClass
 * @see HostEntity
 * @see NonHostEntity
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface Entity extends Immutable, SQLizable {
    
    /**
     * Stores the aspect of the observed entity being created.
     */
    public static final @Nonnull Aspect CREATED = new Aspect(EntityClass.class, "created");
    
    /**
     * Stores the aspect of the observed entity being deleted.
     */
    public static final @Nonnull Aspect DELETED = new Aspect(EntityClass.class, "deleted");
    
    
    /**
     * Returns the site of this entity.
     * 
     * @return the site of this entity.
     */
    @Pure
    public abstract @Nonnull Site getSite();
    
    /**
     * Returns the identity of this entity.
     * 
     * @return the identity of this entity.
     */
    @Pure
    public abstract @Nonnull InternalIdentity getIdentity();
    
    /**
     * Returns the number that references this entity in the database.
     * 
     * @return the number that references this entity in the database.
     */
    @Pure
    public abstract long getNumber();
    
    
    /**
     * Returns this entity as a {@link HostEntity}.
     * 
     * @return this entity as a {@link HostEntity}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link HostEntity}.
     */
    @Pure
    public @Nonnull HostEntity toHostEntity() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link NonHostEntity}.
     * 
     * @return this entity as a {@link NonHostEntity}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link NonHostEntity}.
     */
    @Pure
    public @Nonnull NonHostEntity toNonHostEntity() throws InvalidEncodingException;
    
    /**
     * Returns this entity as an {@link Account}.
     * 
     * @return this entity as an {@link Account}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link Account}.
     */
    @Pure
    public @Nonnull Account toAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link HostAccount}.
     * 
     * @return this entity as a {@link HostAccount}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link HostAccount}.
     */
    @Pure
    public @Nonnull HostAccount toHostAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link NonHostAccount}.
     * 
     * @return this entity as a {@link NonHostAccount}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link NonHostAccount}.
     */
    @Pure
    public @Nonnull NonHostAccount toNonHostAccount() throws InvalidEncodingException;
    
    /**
     * Returns this entity as a {@link Role}.
     * 
     * @return this entity as a {@link Role}.
     * 
     * @throws InvalidEncodingException if this entity is not an instance of {@link Role}.
     */
    @Pure
    public @Nonnull Role toRole() throws InvalidEncodingException;
    
}
