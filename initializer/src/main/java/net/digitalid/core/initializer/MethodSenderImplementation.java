package net.digitalid.core.initializer;

import java.math.BigInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.functional.iterators.ReadOnlyIterator;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.NonEmpty;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.audit.RequestAudit;
import net.digitalid.core.credential.Credential;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.handler.method.InternalMethod;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.identification.identity.Identity;
import net.digitalid.core.identification.identity.Person;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.packet.Response;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.typeset.authentications.FreezableAuthentications;

/**
 * TODO.
 */
public class MethodSenderImplementation {
    
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
    public @Nonnull Response send() throws ExternalException {
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
    public final @Nonnull <T extends Reply> T sendNotNull() throws ExternalException {
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
        final @Nonnull FreezableAgentPermissions permissions = FreezableAgentPermissions.withNoPermissions();
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
    public static @Nonnull Response send(@Nonnull ReadOnlyList<Method> methods, @Nullable RequestAudit audit) throws ExternalException {
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
    
}
