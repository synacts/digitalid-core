package net.digitalid.core.handler;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.collections.freezable.FreezableArrayList;
import net.digitalid.utility.collections.freezable.FreezableList;
import net.digitalid.utility.collections.readonly.ReadOnlyIterator;
import net.digitalid.utility.collections.readonly.ReadOnlyList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.freezable.Frozen;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.service.CoreService;

import net.digitalid.core.synchronizer.RequestAudit;

import net.digitalid.service.core.auxiliary.Time;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.FreezableAgentPermissions;
import net.digitalid.core.agent.ReadOnlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;

import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.attribute.CertifiedAttributeValue;

import net.digitalid.core.contact.Contact;
import net.digitalid.core.contact.FreezableAuthentications;
import net.digitalid.core.contact.ReadOnlyAuthentications;

import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.credential.Credential;

import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestErrorCode;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;

import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.Person;
import net.digitalid.core.identity.SemanticType;

import net.digitalid.core.resolution.IdentityQuery;

import net.digitalid.core.packet.ClientRequest;
import net.digitalid.core.packet.CredentialsRequest;
import net.digitalid.core.packet.HostRequest;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.Response;

import net.digitalid.core.host.annotations.Hosts;

/**
 * This class implements a remote method invocation mechanism.
 * All methods have to extend this class and {@link #add(net.digitalid.service.core.identity.SemanticType, net.digitalid.service.core.handler.Method.Factory) register} themselves as handlers.
 * 
 * @see Action
 * @see Query
 */
@Immutable
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
        
        Require.that(!(entity instanceof Account) || canBeSentByHosts()).orThrow("Methods encoded on hosts can be sent by hosts.");
        Require.that(!(entity instanceof Role) || !canOnlyBeSentByHosts()).orThrow("Methods encoded on clients cannot only be sent by hosts.");
        
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
     * Returns whether this method needs to be lodged.
     * 
     * @return whether this method needs to be lodged.
     */
    @Pure
    public abstract boolean isLodged();
    
    /**
     * Returns either the value b' for clients or the value f' for hosts or null if no credential is shortened.
     * 
     * @return either the value b' for clients or the value f' for hosts or null if no credential is shortened.
     */
    @Pure
    public @Nullable BigInteger getValue() {
        return null;
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
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return FreezableAgentPermissions.NONE;
    }
    
    
    /**
     * Executes this method on the host.
     * 
     * @return a reply for this method or null.
     * 
     * @throws RequestException if the authorization is not sufficient.
     * 
     * @require hasSignature() : "This handler has a signature.";
     * 
     * @ensure matches(return) : "This method matches the returned reply.";
     */
    @Hosts
    @NonCommitting
    public abstract @Nullable Reply executeOnHost() throws RequestException, DatabaseException;
    
    /**
     * Returns whether this method matches the given reply.
     * 
     * @return whether this method matches the given reply.
     */
    @Pure
    // TODO: Make the return type void and throw a InvalidReplyParameterValueException instead?
    public abstract boolean matches(@Nullable Reply reply);
    
    
    /**
     * Returns whether the other method is similar to this one.
     * Handlers are similar if they<br>
     * - have the same entity, subject and recipient, and<br>
     * - belong to the same service and the same class of methods.<br>
     * (The latter is implemented by inheritance and dynamic method binding.)
     * <p>
     * You can override this method and return {@code false} if this method
     * should be sent alone (e.g. due to an overridden {@link #send()} method).
     * The implementation has to be transitive but must not be reflexive.
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
     * You might also want to return {@code false} for {@link #isSimilarTo(net.digitalid.service.core.handler.Method)}.
     * 
     * @return the response to the request that is encoded by this method.
     * 
     * @ensure return.getSize() == 1 : "The response contains one element.";
     * @ensure return.hasRequest() : "The returned response has a request.";
     */
    @NonCommitting
    public @Nonnull Response send() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nullable RequestAudit requestAudit = RequestAudit.get(this);
        final @Nonnull Response response;
        try {
            response = Method.send(FreezableArrayList.get(this).freeze(), requestAudit);
        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
            if (requestAudit != null) { RequestAudit.release(this); }
            throw exception;
        }
        if (requestAudit != null) { response.getAuditNotNull().executeAsynchronously(this); }
        response.checkReply(0);
        return response;
    }
    
    /**
     * Sends the block encoded by this method to the stored recipient.
     * 
     * @return the reply to this method, which may not be null.
     * 
     * @require !matches(null) : "This method does not match null.";
     * 
     * @ensure matches(return) : "This method matches the returned reply.";
     */
    @NonCommitting
    @SuppressWarnings("unchecked")
    public final @Nonnull <T extends Reply> T sendNotNull() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(!matches(null)).orThrow("This method does not match null.");
        
        return (T) send().getReplyNotNull(0);
    }
    
    
    /**
     * Returns whether the given methods are {@link #isSimilarTo(net.digitalid.service.core.handler.Method) similar} to each other (in both directions).
     * 
     * @param methods the methods to check for similarity.
     * 
     * @return whether the given methods are similar to each other.
     */
    @Pure
    public static boolean areSimilar(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<? extends Method> methods) {
        Require.that(methods.isFrozen()).orThrow("The list of methods is frozen.");
        Require.that(!methods.isEmpty()).orThrow("The list of methods is not empty.");
        Require.that(!methods.containsNull()).orThrow("The list of methods does not contain null.");
        
        final @Nonnull ReadOnlyIterator<? extends Method> iterator = methods.iterator();
        final @Nonnull Method reference = iterator.next();
        while (iterator.hasNext()) {
            final @Nonnull Method method = iterator.next();
            if (!method.isSimilarTo(reference) || !reference.isSimilarTo(method)) { return false; }
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
    private static ReadOnlyAgentPermissions getRequiredPermissions(@Nonnull ReadOnlyList<? extends Method> methods) {
        final @Nonnull FreezableAgentPermissions permissions = new FreezableAgentPermissions();
        for (final @Nonnull Method method : methods) {
            permissions.putAll(method.getRequiredPermissionsToExecuteMethod());
        }
        return permissions.freeze();
    }
    
    /**
     * Sends the blocks encoded by the given methods to the common recipient.
     * 
     * @param methods the methods whose blocks are to be sent.
     * @param audit the request audit or null if no audit is requested.
     * 
     * @return a list of replies, which can be null, corresponding to the methods.
     * 
     * @throws FailedRequestException if the blocks of the methods could not be sent.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require !methods.isEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require areSimilar(methods) : "The methods are similar to each other.";
     * 
     * @ensure return.getSize() == methods.size() : "The returned response and the given methods have the same size.";
     * @ensure return.hasRequest() : "The returned response has a request.";
     */
    @NonCommitting
    public static @Nonnull Response send(@Nonnull ReadOnlyList<Method> methods, @Nullable RequestAudit audit) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        Require.that(areSimilar(methods)).orThrow("The methods are similar to each other.");
        
        final @Nonnull Method reference = methods.getNonNullable(0);
        final @Nullable Entity entity = reference.getEntity();
        final @Nonnull InternalIdentifier subject = reference.getSubject();
        final @Nonnull HostIdentifier recipient = reference.getRecipient();
        final boolean lodged = reference.isLodged();
        final @Nullable BigInteger value = reference.getValue();
        
        if (reference.isOnHost() && !reference.canBeSentByHosts()) { throw InternalException.get("These methods cannot be sent by hosts."); }
        if (reference.isOnClient() && reference.canOnlyBeSentByHosts()) { throw InternalException.get("These methods cannot be sent by clients."); }
        
        if (reference instanceof ExternalQuery) {
            final @Nonnull ReadOnlyAuthentications authentications;
            if (reference instanceof IdentityQuery) {
                authentications = FreezableAuthentications.NONE;
            } else {
                final @Nonnull Identity identity = subject.getIdentity();
                if (entity != null && entity instanceof Role && identity instanceof Person) {
                    authentications = Contact.get((Role) entity, (Person) identity).getAuthentications();
                } else {
                    authentications = FreezableAuthentications.NONE;
                }
            }
            
            if (authentications.isEmpty()) {
                return new Request(methods, recipient, subject).send();
            } else {
                assert entity != null && entity instanceof Role;
                final @Nonnull Role role = (Role) entity;
                final @Nonnull Time time = Time.getCurrent();
                final @Nonnull FreezableList<Credential> credentials;
                final @Nullable FreezableList<CertifiedAttributeValue> certificates;
                final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissions(methods);
                if (authentications.contains(FreezableAuthentications.IDENTITY_BASED_TYPE)) {
                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased(role, permissions);
                    credentials = new FreezableArrayList<Credential>(credential);
                    certificates = FreezableArrayList.getWithCapacity(authentications.size() - 1);
                    for (final @Nonnull SemanticType type : authentications) {
                        if (!type.equals(FreezableAuthentications.IDENTITY_BASED_TYPE)) {
                            final @Nullable AttributeValue attributeValue = Attribute.get(entity, type).getValue();
                            if (attributeValue != null && attributeValue.isCertified()) {
                                final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.castTo(CertifiedAttributeValue.class);
                                if (certifiedAttributeValue.isValid(time)) { certificates.add(certifiedAttributeValue); }
                            }
                        }
                    }
                    certificates.freeze();
                } else {
                    credentials = FreezableArrayList.getWithCapacity(authentications.size());
                    for (final @Nonnull SemanticType type : authentications) {
                        final @Nullable AttributeValue attributeValue = Attribute.get(entity, type).getValue();
                        if (attributeValue != null && attributeValue.isCertified()) {
                            final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.castTo(CertifiedAttributeValue.class);
                            if (certifiedAttributeValue.isValid(time)) { credentials.add(ClientCredential.getAttributeBased(role, certifiedAttributeValue, permissions)); }
                        }
                    }
                    certificates = null;
                }
                return new CredentialsRequest(methods, recipient, subject, audit, credentials.freeze(), certificates, lodged, value).send();
            }
        } else {
            Require.that(entity != null).orThrow("The entity can only be null in case of external queries.");
            
            if (reference instanceof ExternalAction) {
                if (entity instanceof Role) {
                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased((Role) entity, getRequiredPermissions(methods));
                    return new CredentialsRequest(methods, recipient, subject, audit, new FreezableArrayList<Credential>(credential).freeze(), null, lodged, value).send();
                } else {
                    return new HostRequest(methods, recipient, subject, entity.getIdentity().getAddress()).send();
                }
            } else {
                assert reference instanceof InternalMethod;
                if (!(entity instanceof Role)) { throw InternalException.get("The entity has to be a role in case of internal methods."); }
                final @Nonnull Role role = (Role) entity;
                final @Nonnull Agent agent = role.getAgent();
                
                final @Nonnull Restrictions restrictions = agent.getRestrictions();
                for (final @Nonnull Method method : methods) {
                    if (!restrictions.cover(((InternalMethod) method).getRequiredRestrictionsToExecuteMethod())) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The restrictions of the role do not cover the required restrictions."); }
                }
                
                if (reference.getService().equals(CoreService.SERVICE)) {
                    if (role.isNative()) {
                        if (!agent.getPermissions().cover(getRequiredPermissions(methods))) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The permissions of the client agent do not cover the required permissions."); }
                        return new ClientRequest(methods, subject, audit, role.toNativeRole().getAgent().getCommitment().addSecret(role.getClient().getSecret())).send();
                    } else {
                        final @Nonnull ClientCredential credential = ClientCredential.getRoleBased(role.toNonNativeRole(), getRequiredPermissions(methods));
                        return new CredentialsRequest(methods, recipient, subject, audit, new FreezableArrayList<Credential>(credential).freeze(), null, lodged, value).send();
                    }
                } else {
                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased(role, getRequiredPermissions(methods));
                    return new CredentialsRequest(methods, recipient, subject, audit, new FreezableArrayList<Credential>(credential).freeze(), null, lodged, value).send();
                }
            }
        }
    }
    
    
    /**
     * Each method needs to {@link #add(net.digitalid.service.core.identity.SemanticType, net.digitalid.service.core.handler.Method.Factory) register} a factory that inherits from this class.
     */
    protected static abstract class Factory<E extends Entity> {
        
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
        @NonCommitting
        protected abstract @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
        
    }
    
    
    /**
     * Maps method types to the factory that creates handlers for that type.
     */
    private static final @Nonnull Map<SemanticType, Factory> converters = new ConcurrentHashMap<>();
    
    /**
     * Adds the given factory that creates handlers for the given type.
     * 
     * @param type the type to handle.
     * @param factory the factory to add.
     */
    protected static void add(@Nonnull SemanticType type, @Nonnull Factory factory) {
        converters.put(type, factory);
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
     * @throws RequestException if no handler is found for the given content type.
     * 
     * @require signature.hasSubject() : "The signature has a subject.";
     * 
     * @ensure return.hasEntity() : "The returned method has an entity.";
     * @ensure return.hasSignature() : "The returned method has a signature.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull Method get(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nullable Method.Factory factory = converters.get(block.getType());
        if (factory == null) { throw RequestException.get(RequestErrorCode.METHOD, "No method could be found for the type " + block.getType().getAddress() + "."); }
        else { return factory.create(entity, signature, recipient, block); }
    }
    
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder("\"").append(getClass().getSimpleName()).append(" to ").append(getSubject());
        if (hasEntity()) { string.append(" by ").append(getEntityNotNull().getIdentity().getAddress()); }
        string.append(": ").append(getDescription()).append("\"");
        return string.toString();
    }
    
    @Pure
    @Override
    protected final boolean protectedEquals(@Nullable Object object) {
        return super.protectedEquals(object) && object instanceof Method && this.recipient.equals(((Method) object).recipient);
    }
    
    @Pure
    @Override
    protected final int protectedHashCode() {
        return 89 * super.protectedHashCode() + recipient.hashCode();
    }
    
}
