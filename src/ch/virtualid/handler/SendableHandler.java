package ch.virtualid.handler;

import ch.virtualid.contact.Authentications;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.ClientEntity;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Account;
import ch.virtualid.exceptions.ShouldNeverHappenError;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.packet.FailedRequestException;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.packet.Request;
import ch.xdf.Block;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * All handlers have to extend this class, declare a public static field with the name {@code TYPE} which
 * states the semantic type of the packets that are handled and provide a constructor with the signature
 * ({@link Entity}, {@link Entity}, {@link SignatureWrapper}, {@link Block}, {@link HostIdentifier})
 * that only throws {@link InvalidEncodingException} and {@link FailedIdentityException}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.4
 */
public abstract class SendableHandler extends Handler {
    
    /**
     * Stores the recipient of this sendable handler.
     */
    private final @Nonnull HostIdentifier recipient;
    
    /**
     * Creates a sendable handler that decodes the given signature and block for the given entity.
     * 
     * @param block the element of the content.
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this handler.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    protected SendableHandler(@Nonnull Block block, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) throws InvalidEncodingException {
        super(block, entity, signature);
        
        this.recipient = recipient;
    }
    
    /**
     * Creates a sendable handler that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this handler.
     * 
     * @require !(entity instanceof HostEntity)|| canBeSentByHost() : "Handlers encoded on hosts have to be sendable by hosts.";
     * @require !(entity instanceof ClientEntity) || !canOnlyBeSentByHost() : "Handlers only sendable by hosts may not occur on clients.";
     */
    protected SendableHandler(@Nullable Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject);
        
        assert !(entity instanceof Account)|| canBeSentByHost() : "Handlers encoded on hosts have to be sendable by hosts.";
        assert !(entity instanceof ClientEntity) || !canOnlyBeSentByHost() : "Handlers only sendable by hosts may not occur on clients.";
        
        this.recipient = recipient;
    }
    
    /**
     * Returns the recipient of this sendable handler.
     * 
     * @return the recipient of this sendable handler.
     */
    @Pure
    public final @Nonnull HostIdentifier getRecipient() {
        return recipient;
    }
    
    
    /**
     * Returns whether the other handler is similar to this one.
     * Handlers are similar if they<br>
     * - have the same entity, subject and recipient,<br>
     * - belong to the same service and the same class of handlers<br>
     * - and can equally be sent by hosts.
     * <p>
     * You can override this method and return {@code false} if this handler should be sent alone (e.g. due to an overridden {@link #send()} method).
     * 
     * @param other the other handler to compare this one with.
     * 
     * @return whether the other handler is similar to this one.
     */
    @Pure
    public boolean isSimilarTo(@Nonnull SendableHandler other) {
        return Objects.equals(this.getEntity(), other.getEntity())
                && this.getSubject().equals(other.getSubject())
                && this.getRecipient().equals(other.getRecipient())
                && this.getService().equals(other.getService())
                && this.canBeSentByHost() == other.canBeSentByHost()
                && this.canOnlyBeSentByHost() == other.canOnlyBeSentByHost();
    }
    
    /**
     * Returns whether this handler can be sent by a host.
     * 
     * @return whether this handler can be sent by a host.
     */
    @Pure
    public abstract boolean canBeSentByHost();
    
    /**
     * Returns whether this handler can only be sent by a host.
     * 
     * @return whether this handler can only be sent by a host.
     */
    @Pure
    public abstract boolean canOnlyBeSentByHost();
    
    
    /**
     * Returns the authentications desired by this handler.
     * 
     * @return the authentications desired by this handler.
     */
    @Pure
    public abstract @Nonnull Authentications getDesiredAuthentications(); // TODO: capturable?
    
    /**
     * Returns the permissions required for this handler.
     * 
     * @return the permissions required for this handler.
     */
    @Pure
    public abstract @Nonnull ReadonlyAgentPermissions getRequiredPermissions(); // TODO: or capturable?
    
    
    /**
     * Sends the block encoded by this handler to the stored recipient.
     * This method can be overridden to support, for example, one-time credentials.
     * You might also want to return {@code false} for {@link #isSimilarTo(ch.virtualid.handler.SendableHandler)}.
     * 
     * @return the reply to this handler in case of queries or, potentially, external actions.
     */
    public @Nullable Reply send() throws FailedRequestException {
        return SendableHandler.send(Arrays.asList(this)).get(0);
    }
    
    
    /**
     * Returns whether the given handlers are not null and {@link #isSimilarTo(ch.virtualid.handler.SendableHandler) similar} to each other.
     * 
     * @param handlers the handlers to check for non-nullness and similarity.
     * 
     * @return whether the given handlers are not null and similar to each other.
     * 
     * @require !handlers.isEmpty() : "The list of handlers is not empty.";
     */
    public static boolean areSimilar(@Nonnull List<? extends SendableHandler> handlers) {
        assert !handlers.isEmpty() : "The list of handlers is not empty.";
        
        @Nonnull Iterator<? extends SendableHandler> iterator = handlers.iterator();
        @Nullable SendableHandler reference = iterator.next();
        if (reference == null) return false;
        while (iterator.hasNext()) {
            @Nullable SendableHandler handler = iterator.next();
            if (handler == null || !handler.isSimilarTo(reference)) return false;
        }
        return true;
    }
    
    /**
     * Sends the blocks encoded by the given handlers to the common recipient.
     * 
     * @param handlers the handlers whose blocks are to be sent.
     * 
     * @return a list of replies, which can be null, corresponding to the handlers.
     * 
     * @throws FailedRequestException if the blocks of the handlers could not be sent.
     * 
     * @require !handlers.isEmpty() : "The list of handlers is not empty.";
     * @require areSimilar(handlers) : "All handlers have to be similar (and not null).";
     */
    public static @Nonnull List<Reply> send(@Nonnull List<? extends SendableHandler> handlers) throws FailedRequestException {
        assert !handlers.isEmpty() : "The list of handlers is not empty.";
        assert areSimilar(handlers) : "All handlers have to be similar (and not null).";
        
        @Nonnull Authentications authentications = new Authentications();
        @Nonnull List<SelfcontainedWrapper> contents = new ArrayList<SelfcontainedWrapper>(handlers.size());
        for (@Nonnull SendableHandler handler : handlers) {
            authentications.addAll(handler.getDesiredAuthentications());
            contents.add(new SelfcontainedWrapper(handler.getType().getNonHostAddress(), handler.toBlock()));
        }
        
        @Nonnull SendableHandler reference = handlers.get(0);
        if (isOnBoth() || authentications.isEmpty()) {
            assert this instanceof ExternalQuery;
            
            // Do not sign (i.e. also ignore the permissions).
        } else if (isOnHost()) {
            assert !isInternal(); // canBeSentByHost(); -> exception instead of assertion?
            
            // Sign as host (as the given entity) (and ignore the permissions).
        } else if (isOnClient()) {
            // !canOnlyBeSentByHost();
            
            @Nonnull AgentPermissions permissions = new AgentPermissions();
            for (@Nonnull SendableHandler handler : handlers) {
                permissions.putAll(handler.getRequiredPermissions());
            }
            
            @Nonnull Role role = (Role) getEntity();
            boolean action = this instanceof Action;
            
            // TODO: Check that the permissions of the role cover the required permissions and that its restrictions cover the required restrictions.
            
            if (isInternal()) {
                // Include auditing.
                Synchronizer.getAudit(getService());
                if (getService().equals(SemanticType.CORE_SERVICE)) {
                    // TODO: If core internal action, check that the authorization of the role covers the required authorization.
                    
                    // Sign as a client or role (and ignore the permissions). -> lodged if action
                } else {
                    // Sign with credentials according to the permissions. -> lodged if action
                }
            } else {
                // Sign with credentials according to the permissions. -> lodged if action
            }
        } else {
            // Should never happen!
        }
        
        response = new Request(content, nonHostIdentifier).send();
        
        int size = getSize();
        numbers = new ArrayList<Long>(size);
        
        try (@Nonnull java.sql.Connection connection = Database.getConnection()) {
            for (int i = 0; i < size; i++) {
                @Nonnull SignatureWrapper signature = getSignature(i);
                numbers.add(i, signature.isSigned() ? addSignature(connection, (HostSignatureWrapper) signature, getContent(i)) : null);
            }
            connection.commit();
        } catch (@Nonnull SQLException exception) {
            throw new FailedRequestException("Could not store the response in the database.", exception);
        } catch (@Nonnull FailedIdentityException exception) {
            throw new FailedRequestException("Could not find the identity " + exception.getIdentifier() + ".", exception);
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new FailedRequestException("Could not decode the response from the host " + getEncryption().getRecipient() + ".", exception);
        }
        
    }
    
    
    /**
     * 
     */
    protected static abstract class Factory {
        
        protected abstract SendableHandler create();
        
    }
    
    
    /**
     * Maps request and response types to their corresponding handler.
     */
    private static final @Nonnull Map<SemanticType, Class<? extends SendableHandler>> handlers = new HashMap<SemanticType, Class<? extends SendableHandler>>();
    
    /**
     * Adds the given sendable handler.
     * 
     * @param handler the handler to add.
     */
    public static void add(@Nonnull Class<? extends SendableHandler> handler) throws ServiceException {
        try {
            handlers.put((SemanticType) handler.getField("TYPE").get(null), handler);
        } catch (@Nonnull NoSuchFieldException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' does not declare a field with the name 'TYPE'.", exception);
        } catch (@Nonnull SecurityException | IllegalAccessException exception) {
            throw new ServiceException("The field with the name 'TYPE' in the handler '" + handler.getName() + "' cannot be accessed.", exception);
        } catch (@Nonnull NullPointerException | IllegalArgumentException exception) {
            throw new ServiceException("The field with the name 'TYPE' in the handler '" + handler.getName() + "' is not static.", exception);
        }
    }
    
    /**
     * Returns the handler for the given packet type.
     * 
     * @param type the type of the packet to be handled.
     * @param connection an open connection to the database.
     * @param entity the entity to which the handler belongs.
     * @param signature the signature of the packet.
     * @param block the element of the content.
     * @param recipient the recipient of the handler.
     * 
     * @return the handler for the given packet type.
     * 
     * @throws PacketException if no handler is found for the given packet type.
     * 
     * @require !connection.isOnBoth() : "The decoding of sendable handlers is site-specific.";
     * @require !connection.isOnClient() || entity instanceof Role : "On the client-side, the entity is a role.";
     * @require !connection.isOnHost() || entity instanceof Identity : "On the host-side, the entity is an identity.";
     * @require !connection.isOnClient() || ((Role) entity).isOnSame(connection) : "On the client-side, the role is on the same site.";
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     */
    public static @Nonnull SendableHandler get(@Nonnull SemanticType type, @Nonnull Entity connection, @Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull Block block, @Nonnull HostIdentifier recipient) throws PacketException, ServiceException, InvalidEncodingException, FailedIdentityException {
        @Nullable Class<? extends SendableHandler> handler = handlers.get(type);
        if (handler == null) throw new PacketException(PacketError.REQUEST);
        try {
            @Nonnull Constructor<? extends SendableHandler> constructor = handler.getConstructor(Entity.class, Entity.class, SignatureWrapper.class, Block.class, HostIdentifier.class);
            return constructor.newInstance(connection, entity, signature, block, recipient);
        } catch (@Nonnull NoSuchMethodException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' does not declare a constructor with the required signature.", exception);
        } catch (@Nonnull SecurityException | IllegalAccessException exception) {
            throw new ServiceException("The required constructor in the handler '" + handler.getName() + "' cannot be accessed.", exception);
        } catch (@Nonnull InstantiationException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' is abstract and cannot be instantiated.", exception);
        } catch (@Nonnull IllegalArgumentException exception) {
            throw new ServiceException("The handler '" + handler.getName() + "' does not accept the given parameters.", exception);
        } catch (@Nonnull InvocationTargetException exception) {
            @Nullable Throwable cause = exception.getCause();
            if (cause == null) throw new ShouldNeverHappenError("The cause of the targe exception should never be null.");
            if (cause instanceof InvalidEncodingException) throw (InvalidEncodingException) cause;
            if (cause instanceof FailedIdentityException) throw (FailedIdentityException) cause;
            // TODO: Probably extend with other exceptions.
            throw new ServiceException("The constructor of the handler '" + handler.getName() + "' throws an unspecified exception.", exception);
        }
    }
    
}
