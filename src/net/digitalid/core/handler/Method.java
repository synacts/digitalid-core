package net.digitalid.core.handler;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.AgentPermissions;
import net.digitalid.core.agent.ReadonlyAgentPermissions;
import net.digitalid.core.agent.Restrictions;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.OnlyForHosts;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.attribute.CertifiedAttributeValue;
import net.digitalid.core.auxiliary.Time;
import net.digitalid.core.collections.FreezableArrayList;
import net.digitalid.core.collections.FreezableList;
import net.digitalid.core.collections.ReadonlyIterator;
import net.digitalid.core.collections.ReadonlyList;
import net.digitalid.core.contact.Authentications;
import net.digitalid.core.contact.Contact;
import net.digitalid.core.contact.ReadonlyAuthentications;
import net.digitalid.core.credential.ClientCredential;
import net.digitalid.core.credential.Credential;
import net.digitalid.core.entity.Account;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.InternalIdentifier;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityQuery;
import net.digitalid.core.identity.Person;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.packet.ClientRequest;
import net.digitalid.core.packet.CredentialsRequest;
import net.digitalid.core.packet.HostRequest;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.Response;
import net.digitalid.core.service.CoreService;
import net.digitalid.core.synchronizer.RequestAudit;
import net.digitalid.core.wrappers.Block;
import net.digitalid.core.wrappers.SignatureWrapper;

/**
 * This class implements a remote method invocation mechanism.
 * All methods have to extend this class and {@link #add(net.digitalid.core.identity.SemanticType, net.digitalid.core.handler.Method.Factory) register} themselves as handlers.
 * 
 * @see Action
 * @see Query
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
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
    public @Nonnull ReadonlyAgentPermissions getRequiredPermissions() {
        return AgentPermissions.NONE;
    }
    
    
    /**
     * Executes this method on the host.
     * 
     * @return a reply for this method or null.
     * 
     * @throws PacketException if the authorization is not sufficient.
     * 
     * @require hasSignature() : "This handler has a signature.";
     * 
     * @ensure matches(return) : "This method matches the returned reply.";
     */
    @OnlyForHosts
    @NonCommitting
    public abstract @Nullable Reply executeOnHost() throws PacketException, SQLException;
    
    /**
     * Returns whether this method matches the given reply.
     * 
     * @return whether this method matches the given reply.
     */
    @Pure
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
     * You might also want to return {@code false} for {@link #isSimilarTo(net.digitalid.core.handler.Method)}.
     * 
     * @return the response to the request that is encoded by this method.
     * 
     * @ensure return.getSize() == 1 : "The response contains one element.";
     * @ensure return.hasRequest() : "The returned response has a request.";
     */
    @NonCommitting
    public @Nonnull Response send() throws SQLException, IOException, PacketException, ExternalException {
        final @Nullable RequestAudit requestAudit = RequestAudit.get(this);
        final @Nonnull Response response;
        try {
            response = Method.send(new FreezableArrayList<Method>(this).freeze(), requestAudit);
        } catch (@Nonnull SQLException | IOException | PacketException | ExternalException exception) {
            if (requestAudit != null) RequestAudit.release(this);
            throw exception;
        }
        if (requestAudit != null) response.getAuditNotNull().executeAsynchronously(this);
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
    public final @Nonnull <T extends Reply> T sendNotNull() throws SQLException, IOException, PacketException, ExternalException {
        assert !matches(null) : "This method does not match null.";
        
        return (T) send().getReplyNotNull(0);
    }
    
    
    /**
     * Returns whether the given methods are {@link #isSimilarTo(net.digitalid.core.handler.Method) similar} to each other (in both directions).
     * 
     * @param methods the methods to check for similarity.
     * 
     * @return whether the given methods are similar to each other.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     */
    @Pure
    public static boolean areSimilar(@Nonnull ReadonlyList<? extends Method> methods) {
        assert methods.isFrozen() : "The list of methods is frozen.";
        assert methods.isNotEmpty() : "The list of methods is not empty.";
        assert methods.doesNotContainNull() : "The list of methods does not contain null.";
        
        final @Nonnull ReadonlyIterator<? extends Method> iterator = methods.iterator();
        final @Nonnull Method reference = iterator.next();
        while (iterator.hasNext()) {
            final @Nonnull Method method = iterator.next();
            if (!method.isSimilarTo(reference) || !reference.isSimilarTo(method)) return false;
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
     * @param audit the request audit or null if no audit is requested.
     * 
     * @return a list of replies, which can be null, corresponding to the methods.
     * 
     * @throws FailedRequestException if the blocks of the methods could not be sent.
     * 
     * @require methods.isFrozen() : "The list of methods is frozen.";
     * @require methods.isNotEmpty() : "The list of methods is not empty.";
     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
     * @require areSimilar(methods) : "The methods are similar to each other.";
     * 
     * @ensure return.getSize() == methods.size() : "The returned response and the given methods have the same size.";
     * @ensure return.hasRequest() : "The returned response has a request.";
     */
    @NonCommitting
    public static @Nonnull Response send(@Nonnull ReadonlyList<Method> methods, @Nullable RequestAudit audit) throws SQLException, IOException, PacketException, ExternalException {
        assert areSimilar(methods) : "The methods are similar to each other.";
        
        final @Nonnull Method reference = methods.getNotNull(0);
        final @Nullable Entity entity = reference.getEntity();
        final @Nonnull InternalIdentifier subject = reference.getSubject();
        final @Nonnull HostIdentifier recipient = reference.getRecipient();
        final boolean lodged = reference.isLodged();
        final @Nullable BigInteger value = reference.getValue();
        
        if (reference.isOnHost() && !reference.canBeSentByHosts()) throw new PacketException(PacketError.INTERNAL, "These methods cannot be sent by hosts.");
        if (reference.isOnClient() && reference.canOnlyBeSentByHosts()) throw new PacketException(PacketError.INTERNAL, "These methods cannot be sent by clients.");
        
        if (reference instanceof ExternalQuery) {
            final @Nonnull ReadonlyAuthentications authentications;
            if (reference instanceof IdentityQuery) {
                authentications = Authentications.NONE;
            } else {
                final @Nonnull Identity identity = subject.getIdentity();
                if (entity != null && entity instanceof Role && identity instanceof Person) {
                    authentications = Contact.get((Role) entity, (Person) identity).getAuthentications();
                } else {
                    authentications = Authentications.NONE;
                }
            }
            
            if (authentications.isEmpty()) {
                return new Request(methods, recipient, subject).send();
            } else {
                assert entity != null && entity instanceof Role;
                final @Nonnull Role role = (Role) entity;
                final @Nonnull Time time = new Time();
                final @Nonnull FreezableList<Credential> credentials;
                final @Nullable FreezableList<CertifiedAttributeValue> certificates;
                final @Nonnull ReadonlyAgentPermissions permissions = getRequiredPermissions(methods);
                if (authentications.contains(Authentications.IDENTITY_BASED_TYPE)) {
                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased(role, permissions);
                    credentials = new FreezableArrayList<Credential>(credential);
                    certificates = new FreezableArrayList<CertifiedAttributeValue>(authentications.size() - 1);
                    for (final @Nonnull SemanticType type : authentications) {
                        if (!type.equals(Authentications.IDENTITY_BASED_TYPE)) {
                            final @Nullable AttributeValue attributeValue = Attribute.get(entity, type).getValue();
                            if (attributeValue != null && attributeValue.isCertified()) {
                                final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.toCertifiedAttributeValue();
                                if (certifiedAttributeValue.isValid(time)) certificates.add(certifiedAttributeValue);
                            }
                        }
                    }
                    certificates.freeze();
                } else {
                    credentials = new FreezableArrayList<Credential>(authentications.size());
                    for (final @Nonnull SemanticType type : authentications) {
                        final @Nullable AttributeValue attributeValue = Attribute.get(entity, type).getValue();
                        if (attributeValue != null && attributeValue.isCertified()) {
                            final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.toCertifiedAttributeValue();
                            if (certifiedAttributeValue.isValid(time)) credentials.add(ClientCredential.getAttributeBased(role, certifiedAttributeValue, permissions));
                        }
                    }
                    certificates = null;
                }
                return new CredentialsRequest(methods, recipient, subject, audit, credentials.freeze(), certificates, lodged, value).send();
            }
        } else {
            assert entity != null : "The entity can only be null in case of external queries.";
            
            if (reference instanceof ExternalAction) {
                if (entity instanceof Role) {
                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased((Role) entity, getRequiredPermissions(methods));
                    return new CredentialsRequest(methods, recipient, subject, audit, new FreezableArrayList<Credential>(credential).freeze(), null, lodged, value).send();
                } else {
                    return new HostRequest(methods, recipient, subject, entity.getIdentity().getAddress()).send();
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
                
                if (reference.getService().equals(CoreService.SERVICE)) {
                    if (role.isNative()) {
                        if (!agent.getPermissions().cover(getRequiredPermissions(methods))) throw new PacketException(PacketError.AUTHORIZATION, "The permissions of the client agent do not cover the required permissions.");
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
     * Each method needs to {@link #add(net.digitalid.core.identity.SemanticType, net.digitalid.core.handler.Method.Factory) register} a factory that inherits from this class.
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
        @NonCommitting
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
    @NonCommitting
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
     * Returns this method as an {@link InternalAction}.
     * 
     * @return this method as an {@link InternalAction}.
     * 
     * @throws InvalidEncodingException if this method is not an instance of {@link InternalAction}.
     */
    @Pure
    public final @Nonnull InternalAction toInternalAction() throws InvalidEncodingException {
        if (this instanceof InternalAction) return (InternalAction) this;
        throw new InvalidEncodingException("The method with the type " + getType().getAddress() + " is not an internal action.");
    }
    
    /**
     * Returns this method as an {@link ExternalAction}.
     * 
     * @return this method as an {@link ExternalAction}.
     * 
     * @throws InvalidEncodingException if this method is not an instance of {@link ExternalAction}.
     */
    @Pure
    public final @Nonnull ExternalAction toExternalAction() throws InvalidEncodingException {
        if (this instanceof ExternalAction) return (ExternalAction) this;
        throw new InvalidEncodingException("The method with the type " + getType().getAddress() + " is not an external action.");
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
    
    /**
     * Returns this method as an {@link InternalQuery}.
     * 
     * @return this method as an {@link InternalQuery}.
     * 
     * @throws InvalidEncodingException if this method is not an instance of {@link InternalQuery}.
     */
    @Pure
    public final @Nonnull InternalQuery toInternalQuery() throws InvalidEncodingException {
        if (this instanceof InternalQuery) return (InternalQuery) this;
        throw new InvalidEncodingException("The method with the type " + getType().getAddress() + " is not an internal query.");
    }
    
    /**
     * Returns this method as an {@link ExternalQuery}.
     * 
     * @return this method as an {@link ExternalQuery}.
     * 
     * @throws InvalidEncodingException if this method is not an instance of {@link ExternalQuery}.
     */
    @Pure
    public final @Nonnull ExternalQuery toExternalQuery() throws InvalidEncodingException {
        if (this instanceof ExternalQuery) return (ExternalQuery) this;
        throw new InvalidEncodingException("The method with the type " + getType().getAddress() + " is not an external query.");
    }
    
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        final @Nonnull StringBuilder string = new StringBuilder(getClass().getSimpleName());
        string.append(" by ").append(hasEntity() ? getEntityNotNull().getIdentity().getAddress() : "null");
        string.append(" for ").append(getSubject()).append(" (").append(getDescription()).append(")");
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
