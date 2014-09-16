package ch.virtualid.handler;

import ch.virtualid.agent.Agent;
import ch.virtualid.agent.AgentPermissions;
import ch.virtualid.agent.ClientAgent;
import ch.virtualid.agent.OutgoingRole;
import ch.virtualid.agent.ReadonlyAgentPermissions;
import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.client.Synchronizer;
import ch.virtualid.contact.Authentications;
import ch.virtualid.contact.Contact;
import ch.virtualid.contact.ReadonlyAuthentications;
import ch.virtualid.entity.Account;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.Identifier;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Person;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.module.CoreService;
import ch.virtualid.packet.Audit;
import ch.virtualid.packet.ClientRequest;
import ch.virtualid.packet.CredentialsRequest;
import ch.virtualid.packet.FailedRequestException;
import ch.virtualid.packet.HostRequest;
import ch.virtualid.packet.Packet;
import ch.virtualid.packet.PacketError;
import ch.virtualid.packet.PacketException;
import ch.virtualid.packet.Request;
import ch.virtualid.packet.Response;
import ch.virtualid.util.FreezableArrayList;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyIterator;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.Block;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.exceptions.FailedEncodingException;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
     */
    protected Method(@Nullable Entity entity, @Nonnull Identifier subject, @Nonnull HostIdentifier recipient) {
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
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure getEntity() != null : "The entity of this handler is not null.";
     * @ensure getSignature() != null : "The signature of this handler is not null.";
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
     */
    @Pure
    public abstract @Nonnull ReadonlyAgentPermissions getRequiredPermissions();
    
    
    /**
     * Executes this method on the host.
     * 
     * @return a reply for this method or null.
     * 
     * @throws PacketException if the authorization is not sufficient.
     * 
     * @require isOnHost() : "This method is called on a host.";
     * @require getSignature != null : "The signature of this handler is not null.";
     */
    public abstract @Nullable Reply excecute() throws PacketException, SQLException;
    
    
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
     * @return the reply to this method in case of queries or, potentially, external actions.
     */
    public @Nullable Reply send() throws FailedRequestException {
        return Method.send(new FreezableArrayList<Method>(this).freeze()).get(0);
    }
    
    
    /**
     * Returns whether the given methods are {@link #isSimilarTo(ch.virtualid.handler.Method) similar} to each other (in both directions) and not null.
     * 
     * @param methods the methods to check for similarity and non-nullness.
     * 
     * @return whether the given methods are similar to each other and not null.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require !methods.isEmpty() : "The list of methods is not empty.";
     */
    @Pure
    public static boolean areSimilar(@Nonnull ReadonlyList<? extends Method> methods) {
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert !methods.isEmpty() : "The list of methods is not empty.";
        
        final @Nonnull ReadonlyIterator<? extends Method> iterator = methods.iterator();
        final @Nullable Method reference = iterator.next();
        if (reference == null) return false;
        while (iterator.hasNext()) {
            final @Nullable Method method = iterator.next();
            if (method == null || !method.isSimilarTo(reference) || !reference.isSimilarTo(method)) return false;
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
     * @require !methods.isEmpty() : "The list of methods is not empty.";
     * @require areSimilar(methods) : "All methods are similar and not null.";
     */
    public static @Nonnull Response send(@Nonnull ReadonlyList<? extends Method> methods) throws FailedRequestException, SQLException, FailedIdentityException, InvalidDeclarationException, FailedEncodingException {
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert !methods.isEmpty() : "The list of handlers is not empty.";
        assert areSimilar(methods) : "All methods are similar and not null.";
        
        final @Nonnull Method reference = methods.getNotNull(0);
        final @Nullable Entity entity = reference.getEntity();
        final @Nonnull Identifier subject = reference.getSubject();
        final @Nonnull Identity identity = subject.getIdentity();
        final @Nonnull SemanticType service = reference.getService();
        final @Nonnull HostIdentifier recipient = reference.getRecipient();
        
        if (entity instanceof Account && !reference.canBeSentByHosts()) throw new FailedRequestException("These methods cannot be sent by hosts.");
        if (entity instanceof Role && reference.canOnlyBeSentByHosts()) throw new FailedRequestException("These methods cannot be sent by clients.");
        
        final @Nonnull FreezableList<SelfcontainedWrapper> contents = new FreezableArrayList<SelfcontainedWrapper>(methods.size());
        for (final @Nonnull Method method : methods) {
            contents.add(new SelfcontainedWrapper(Packet.CONTENT, method.toBlock()));
        }
        contents.freeze();
        
        if (reference instanceof ExternalQuery) {
            final @Nonnull ReadonlyAuthentications authentications;
            if (entity != null && entity instanceof Role && identity instanceof Person) {
                authentications = Contact.get(entity, (Person) identity).getAuthentications();
            } else {
                authentications = Authentications.NONE;
            }
            
            if (authentications.isEmpty()) {
                return new Request(contents, recipient, subject).send();
            } else {
                assert entity != null && entity instanceof Role;
                final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions(methods);
                // TODO: Get the credentials and certificates from the role or throw a failed request exception if the permissions are not covered.
                return new CredentialsRequest(contents, recipient, subject, null, credentials, certificates, false, null).send();
            }
        } else {
            if (entity == null) throw new FailedRequestException("The entity may only be null in case of external queries.");
            
            if (reference instanceof ExternalAction) {
                if (entity instanceof Account) {
                    return new HostRequest(contents, recipient, subject, ((Account) entity).getIdentity().getAddress()).send();
                } else {
                    assert entity instanceof Role;
                    final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions(methods);
                    // TODO: Get the identity-based credential from the role or throw a failed request exception if the permissions are not covered.
                    return new CredentialsRequest(contents, recipient, subject, null, credentials, null, true, null).send();
                }
            } else {
                assert reference instanceof InternalMethod;
                if (!(entity instanceof Role)) throw new FailedRequestException("The entity has to be a role in case of internal methods.");
                final @Nonnull Role role = (Role) entity;
                final @Nonnull Agent agent = role.getAgent();
                
                final @Nonnull Restrictions restrictions = agent.getRestrictions();
                for (@Nonnull Method method : methods) {
                    if (!restrictions.cover(((InternalMethod) method).getRequiredRestrictions())) throw new FailedRequestException("The restrictions of the role do not cover the required restrictions.");
                }
                
                final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions(methods);
                final @Nonnull Audit audit = Synchronizer.getAudit(service);
                final boolean lodged = reference instanceof InternalAction;
                
                if (service.equals(CoreService.TYPE)) {
                    if (agent instanceof ClientAgent) {
                        if (!agent.getPermissions().cover(permissions)) throw new FailedRequestException("The permissions of the role do not cover the required permissions.");
                        return new ClientRequest(contents, subject, audit, ((ClientAgent) agent).getCommitment()).send();
                    } else {
                        assert agent instanceof OutgoingRole;
                        // TODO: Retrieve the internal credentials from the role or throw a failed request exception if the permissions are not covered.
                        return new CredentialsRequest(contents, recipient, subject, audit, credentials, null, lodged, null).send();
                    }
                } else {
                    // TODO: Retrieve the external credentials from the role or throw a failed request exception if the permissions are not covered.
                    return new CredentialsRequest(contents, recipient, subject, audit, credentials, null, lodged, null).send();
                }
            }
        }
    }
    
    
    /**
     * Each method needs to {@link #add(ch.virtualid.handler.Method.Factory) register} a factory that inherits from this class.
     */
    protected static abstract class Factory extends Handler.Factory {
        
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
         * @require signature.getSubject() != null : "The subject of the signature is not null.";
         * @require block.getType().isBasedOn(getType()) : "The block is based on the indicated type.";
         * 
         * @ensure return.getEntity() != null : "The entity of the returned method is not null.";
         * @ensure return.getSignature() != null : "The signature of the returned method is not null.";
         */
        protected abstract @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException;
        
    }
    
    
    /**
     * Maps method types to the factory that creates handlers for that type.
     */
    private static final @Nonnull Map<SemanticType, Method.Factory> factories = Collections.synchronizedMap(new HashMap<SemanticType, Method.Factory>());
    
    /**
     * Adds the given method factory.
     * 
     * @param factory the factory to add.
     */
    protected static void add(@Nonnull Method.Factory factory) {
        factories.put(factory.getType(), factory);
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
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * 
     * @ensure return.getEntity() != null : "The entity of the returned method is not null.";
     * @ensure return.getSignature() != null : "The signature of the returned method is not null.";
     */
    @Pure
    public static @Nonnull Method get(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws PacketException, InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
        final @Nullable Method.Factory factory = factories.get(block.getType());
        if (factory == null) throw new PacketException(PacketError.REQUEST);
        else return factory.create(entity, signature, recipient, block);
    }
    
}
