package ch.virtualid.handler;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ClientAgent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.attribute.CertifiedAttributeValue;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.contact.Authentications;
import ch.virtualid.contact.Contact;
import ch.virtualid.contact.ReadonlyAuthentications;
import ch.virtualid.credential.Credential;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.NonHostEntity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketError;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.virtualid.module.Service;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.ClientRequest;
import ch.virtualid.packet.CredentialsRequest;
import ch.virtualid.packet.HostRequest;
import ch.virtualid.packet.Request;
import ch.virtualid.packet.Response;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.ReadonlyIterator;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class implements a remote method invocation mechanism.
 * All methods have to extend this class and {@link #add(ch.virtualid.handler.Method.Factory) register} themselves as handlers.
 * 
 * @see Action
 * @see Query
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.9
 */
public abstract class Method extends Handler {
    
    /**
     * Stores the recipient of this method.
     */
    private final @Nonnull HostIdentifier recipient;
    
    /**
     * Creates a method that encodes the content of a packet for the given recipient about the given subject.
     * 
     * @param entity the entity to which this handler belongs.
     * @param subject the subject of this handler.
     * @param recipient the recipient of this method.
     * 
     * @require !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
     * @require !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
     * 
     * @ensure isNonHost() : "This method belongs to a non-host.";
     */
    protected Method(@Nullable NonHostEntity entity, @Nonnull InternalIdentifier subject, @Nonnull HostIdentifier recipient) {
        super(entity, subject);
        
        assert !(entity instanceof Account) || canBeSentByHosts() : "Methods encoded on hosts can be sent by hosts.";
        assert !(entity instanceof Role) || !canOnlyBeSentByHosts() : "Methods encoded on clients cannot only be sent by hosts.";
        
        this.recipient = recipient;
    }
    
    /**
     * Creates a method that decodes a packet with the given signature for the given entity.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure hasEntity() : "This method has an entity.";
     * @ensure hasSignature() : "This handler has a signature.";
     */
    protected Method(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient) {
        super(entity, signature);
        
        this.recipient = recipient;
    }
    
    /**
     * Returns the recipient of this method.
     * 
     * @return the recipient of this method.
     */
    @Pure
    public final @Nonnull HostIdentifier getRecipient() {
        return recipient;
    }
    
    
    /**
     * Returns whether this method can be sent by hosts.
     * 
     * @return whether this method can be sent by hosts.
     */
    @Pure
    public abstract boolean canBeSentByHosts();
    
    /**
     * Returns whether this method can only be sent by hosts.
     * 
     * @return whether this method can only be sent by hosts.
     */
    @Pure
    public abstract boolean canOnlyBeSentByHosts();
    
    /**
     * Returns the permissions required for this method.
     * 
     * @return the permissions required for this method.
     * 
     * @ensure return.isFrozen() : "The returned permissions are frozen.";
     */
    @Pure
    public abstract @Nonnull ReadonlyAgentPermissions getRequiredPermissions();
    
    
    /**
     * Returns the class that handles the reply of this method or null if the method never gives a reply.
     * 
     * @return the class that handles the reply of this method or null if the method never gives a reply.
     */
    @Pure
    public abstract @Nullable Class<? extends Reply> getReplyClass();
    
    /**
     * Executes this method on the host.
     * 
     * @return a reply for this method or null.
     * 
     * @throws PacketException if the authorization is not sufficient.
     * 
     * @require isOnHost() : "This method is called on a host.";
     * @require hasSignature() : "This handler has a signature.";
     * 
     * @ensure return == null || getReplyClass() != null && getReplyClass().isInstance(return) : "If a reply is returned, it is an instance of the indicated class.";
     */
    public abstract @Nullable Reply executeOnHost() throws PacketException, SQLException;
    
    
    /**
     * Returns whether the other method is similar to this one.
     * Handlers are similar if they<br>
     * - have the same entity, subject and recipient, and<br>
     * - belong to the same service and the same class of methods.<br>
     * (The latter is implemented by inheritance and dynamic method binding.)
     * <p>
     * You can override this method and return {@code false} if this method should be sent alone (e.g. due to an overridden {@link #send()} method).
     * 
     * @param other the other method to compare this one with.
     * 
     * @return whether the other method is similar to this one.
     */
    @Pure
    public boolean isSimilarTo(@Nonnull Method other) {
        return Objects.equals(this.getEntity(), other.getEntity())
                && this.getSubject().equals(other.getSubject())
                && this.getRecipient().equals(other.getRecipient())
                && this.getService().equals(other.getService());
    }
    
    /**
     * Sends the block encoded by this method to the stored recipient.
     * This method can be overridden to support, for example, one-time credentials.
     * You might also want to return {@code false} for {@link #isSimilarTo(ch.virtualid.handler.Method)}.
     * 
     * @return the response to the request that is encoded by this method.
     * 
     * @require isNonHost() : "This method belongs to a non-host.";
     * 
     * @ensure return.hasRequest() : "The returned response has a request.";
     */
    public @Nonnull Response send() throws SQLException, IOException, PacketException, ExternalException {
        return Method.send(new FreezableArrayList<Method>(this).freeze());
    }
    
    /**
     * Sends the block encoded by this method to the stored recipient.
     * 
     * @return the reply to this method, which may not be null.
     * 
     * @require isNonHost() : "This method belongs to a non-host.";
     * 
     * @see #getReplyClass()
     */
    @SuppressWarnings("unchecked")
    public final @Nonnull <T extends Reply> T sendNotNull() throws SQLException, IOException, PacketException, ExternalException {
        return (T) send().getReplyNotNull(0);
    }
    
    
    /**
     * Returns whether the given methods are {@link #isSimilarTo(ch.virtualid.handler.Method) similar} to each other (in both directions) and not null.
     * 
     * @param methods the methods to check for similarity and non-nullness.
     * 
     * @return whether the given methods are similar to each other and not null.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     */
    @Pure
    public static boolean areSimilar(@Nonnull ReadonlyList<? extends Method> methods) {
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert methods.isNotEmpty() : "The list of methods is not empty.";
        
        final @Nonnull ReadonlyIterator<? extends Method> iterator = methods.iterator();
        final @Nullable Method reference = iterator.next();
        if (reference == null) return false;
        while (iterator.hasNext()) {
            final @Nullable Method method = iterator.next();
            if (method == null || !method.isNonHost() || !method.isSimilarTo(reference) || !reference.isSimilarTo(method)) return false;
        }
        return true;
    }
    
    /**
     * Returns the permissions required for the given methods.
     * 
     * @param methods the methods whose permissions are to be aggregated.
     * 
     * @return the permissions required for the given methods.
     */
    @Pure
    private static ReadonlyAgentPermissions getRequiredPermissions(@Nonnull ReadonlyList<? extends Method> methods) {
        final @Nonnull AgentPermissions permissions = new AgentPermissions();
        for (@Nonnull Method method : methods) {
            permissions.putAll(method.getRequiredPermissions());
        }
        return permissions.freeze();
    }
    
    /**
     * Sends the blocks encoded by the given methods to the common recipient.
     * 
     * @param methods the methods whose blocks are to be sent.
     * 
     * @return a list of replies, which can be null, corresponding to the methods.
     * 
     * @throws FailedRequestException if the blocks of the methods could not be sent.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require areSimilar(methods) : "All methods are similar and not null.";
     * 
     * @ensure return.hasRequest() : "The returned response has a request.";
     */
    public static @Nonnull Response send(@Nonnull ReadonlyList<Method> methods) throws SQLException, IOException, PacketException, ExternalException {
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert methods.isNotEmpty() : "The list of methods is not empty.";
        assert areSimilar(methods) : "All methods are similar and not null.";
        
        final @Nonnull Method reference = methods.getNotNull(0);
        final @Nullable Entity entity = reference.getEntity();
        final @Nonnull InternalIdentifier subject = reference.getSubject();
        final @Nonnull Identity identity = subject.getIdentity();
        final @Nonnull Service service = reference.getService();
        final @Nonnull HostIdentifier recipient = reference.getRecipient();
        
        if (reference.isOnHost() && !reference.canBeSentByHosts()) throw new PacketException(PacketError.INTERNAL, "These methods cannot be sent by hosts.");
        if (reference.isOnClient() && reference.canOnlyBeSentByHosts()) throw new PacketException(PacketError.INTERNAL, "These methods cannot be sent by clients.");
        
        // TODO: Delete the following two lines and implement a real lookup!
        final @Nullable ReadonlyList<Credential> credentials = null;
        final @Nullable ReadonlyList<CertifiedAttributeValue> certificates = null;
        
        if (reference instanceof ExternalQuery) {
            final @Nonnull ReadonlyAuthentications authentications;
            if (entity != null && entity instanceof Role && identity instanceof Person) {
                authentications = Contact.get((Role) entity, (Person) identity).getAuthentications();
            } else {
                authentications = Authentications.NONE;
            }
            
            if (authentications.isEmpty()) {
                return new Request(methods, recipient, subject).send();
            } else {
                assert entity != null && entity instanceof Role;
                final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions(methods);
                // TODO: Get the credentials and certificates from the role or throw a packet exception if the permissions are not covered.
                return new CredentialsRequest(methods, recipient, subject, null, credentials, certificates, false, null).send();
            }
        } else {
            assert entity != null : "The entity can only be null in case of external queries.";
            
            if (reference instanceof ExternalAction) {
                if (entity instanceof Account) {
                    return new HostRequest(methods, recipient, subject, ((Account) entity).getIdentity().getAddress()).send();
                } else {
                    assert entity instanceof Role;
                    final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions(methods);
                    // TODO: Get the identity-based credential from the role or throw a packet exception if the permissions are not covered.
                    return new CredentialsRequest(methods, recipient, subject, null, credentials, null, true, null).send();
                }
            } else {
                assert reference instanceof InternalMethod;
                if (!(entity instanceof Role)) throw new PacketException(PacketError.INTERNAL, "The entity has to be a role in case of internal methods.");
                final @Nonnull Role role = (Role) entity;
                final @Nonnull Agent agent = role.getAgent();
                
                final @Nonnull Restrictions restrictions = agent.getRestrictions();
                for (@Nonnull Method method : methods) {
                    if (!restrictions.cover(((InternalMethod) method).getRequiredRestrictions())) throw new PacketException(PacketError.AUTHORIZATION, "The restrictions of the role do not cover the required restrictions.");
                }
                
                final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions(methods);
                final @Nonnull Audit audit = new Audit(Time.MIN); // TODO: Synchronizer.getAudit(service);
                final boolean lodged = reference instanceof InternalAction;
                
                if (service.equals(CoreService.SERVICE)) {
                    if (agent instanceof ClientAgent) {
                        if (!agent.getPermissions().cover(permissions)) throw new PacketException(PacketError.AUTHORIZATION, "The permissions of the role do not cover the required permissions.");
                        return new ClientRequest(methods, subject, audit, ((ClientAgent) agent).getCommitment().addSecret(role.getClient().getSecret())).send();
                    } else {
                        assert agent instanceof OutgoingRole;
                        // TODO: Retrieve the internal credentials from the role or throw a packet exception if the permissions are not covered.
                        return new CredentialsRequest(methods, recipient, subject, audit, credentials, null, lodged, null).send();
                    }
                } else {
                    // TODO: Retrieve the external credentials from the role or throw a packet exception if the permissions are not covered.
                    return new CredentialsRequest(methods, recipient, subject, audit, credentials, null, lodged, null).send();
                }
            }
        }
    }
    
    
    /**
     * Each method needs to {@link #add(ch.virtualid.identity.SemanticType, ch.virtualid.handler.Method.Factory) register} a factory that inherits from this class.
     */
    protected static abstract class Factory {
        
        /**
         * Creates a method that handles contents of the indicated type.
         * 
         * @param entity the entity to which the returned method belongs
         * @param signature the signature of the returned method (or a dummy that just contains a subject).
         * @param recipient the recipient of the returned method.
         * @param block the content which is to be handled.
         * 
         * @return a new method that decodes the given block.
         * 
         * @require signature.hasSubject() : "The signature has a subject.";
         * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
         * 
         * @ensure return.hasEntity() : "The returned method has an entity.";
         * @ensure return.hasSignature() : "The returned method has a signature.";
         */
        @Pure
        protected abstract @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException;
        
    }
    
    
    /**
     * Maps method types to the factory that creates handlers for that type.
     */
    private static final @Nonnull Map<SemanticType, Factory> factories = new ConcurrentHashMap<SemanticType, Factory>();
    
    /**
     * Adds the given factory that creates handlers for the given type.
     * 
     * @param type the type to handle.
     * @param factory the factory to add.
     */
    protected static void add(@Nonnull SemanticType type, @Nonnull Factory factory) {
        factories.put(type, factory);
    }
    
    /**
     * Returns a method that handles the given block.
     * 
     * @param entity the entity to which the content belongs.
     * @param signature the signature of the content.
     * @param recipient the recipient of the content.
     * @param block the content which is to be decoded.
     * 
     * @return a method that handles the given block.
     * 
     * @throws PacketException if no handler is found for the given content type.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure return.hasEntity() : "The returned method has an entity.";
     * @ensure return.hasSignature() : "The returned method has a signature.";
     */
    @Pure
    public static @Nonnull Method get(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws SQLException, IOException, PacketException, ExternalException {
        final @Nullable Method.Factory factory = factories.get(block.getType());
        if (factory == null) throw new PacketException(PacketError.METHOD, "No method could be found for the type " + block.getType().getAddress() + ".");
        else return factory.create(entity, signature, recipient, block);
    }
    
    /**
     * Returns this method as an {@link Action}.
     * 
     * @return this method as an {@link Action}.
     * 
     * @throws InvalidEncodingException if this method is not an instance of {@link Action}.
     */
    @Pure
    public final @Nonnull Action toAction() throws InvalidEncodingException {
        if (this instanceof Action) return (Action) this;
        throw new InvalidEncodingException("The method with the type " + getType().getAddress() + " is not an action.");
    }
    
    /**
     * Returns this method as a {@link Query}.
     * 
     * @return this method as a {@link Query}.
     * 
     * @throws InvalidEncodingException if this method is not an instance of {@link Query}.
     */
    @Pure
    public final @Nonnull Query toQuery() throws InvalidEncodingException {
        if (this instanceof Query) return (Query) this;
        throw new InvalidEncodingException("The method with the type " + getType().getAddress() + " is not a query.");
    }
    
}
