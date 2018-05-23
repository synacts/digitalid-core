/*
 * Copyright (C) 2017 Synacts GmbH, Switzerland (info@synacts.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.digitalid.core.handler.method;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.logging.Log;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.compression.CompressionBuilder;
import net.digitalid.core.compression.CompressionConverterBuilder;
import net.digitalid.core.encryption.Encryption;
import net.digitalid.core.encryption.RequestEncryptionBuilder;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.exceptions.response.DeclarationExceptionBuilder;
import net.digitalid.core.handler.Handler;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.handler.method.query.Query;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.handler.reply.instances.RequestExceptionReply;
import net.digitalid.core.handler.reply.instances.RequestExceptionReplyConverter;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.PackConverter;
import net.digitalid.core.packet.Request;
import net.digitalid.core.packet.RequestBuilder;
import net.digitalid.core.packet.Response;
import net.digitalid.core.permissions.FreezableAgentPermissions;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.SignatureBuilder;
import net.digitalid.core.signature.host.HostSignature;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * This type implements a remote method invocation mechanism.
 * All methods have to extend this interface and {@link MethodIndex#add(net.digitalid.utility.conversion.interfaces.Converter) register} themselves as handlers.
 * 
 * @see Action
 * @see Query
 */
@Immutable
public interface Method<@Unspecifiable ENTITY extends Entity> extends Handler<ENTITY> {
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient of this method.
     */
    @Pure
    @OrderOfAssignment(2)
    public @Nonnull HostIdentifier getRecipient();
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity that was provided with the builder.
     */
    @Pure
    @Provided
    @Default("null")
    public @Nullable Entity getProvidedEntity();
    
    /**
     * Casts the given entity to the generic type of this handler.
     */
    @Pure
    @SuppressWarnings("unchecked")
    public default @Nullable ENTITY castEntity(@Nullable Entity entity) {
        return (ENTITY) entity;
    }
    
    @Pure
    @Override
    @Derive("castEntity(providedEntity)")
    public @Nullable ENTITY getEntity();
    
    /* -------------------------------------------------- Lodged -------------------------------------------------- */
    
    /**
     * Returns whether this method needs to be lodged.
     */
    @Pure
    public boolean isLodged();
    
   
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    /**
     * Returns whether this method can be sent by hosts.
     */
    @Pure
    public boolean canBeSentByHosts();
    
    /**
     * Returns whether this method can be sent by clients.
     */
    @Pure
    public boolean canBeSentByClients();
    
    /**
     * Returns the permissions required for this method.
     */
    @Pure
    public default @Nonnull @Frozen ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return FreezableAgentPermissions.NONE;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * Executes this method on the host.
     * 
     * @return a reply for this method or null.
     * 
     * @throws RequestException if the authorization is not sufficient.
     */
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public @Nullable @Matching Reply<ENTITY> executeOnHost() throws RequestException, DatabaseException, RecoveryException;
    
    /* -------------------------------------------------- Send -------------------------------------------------- */
    
    /**
     * Signs the compressed content. The signature depends on the type of method.
     */
    @Pure
    public default @Nonnull Signature<Compression<Pack>> getSignature(@Nonnull Compression<Pack> compression) throws ExternalException {
        return SignatureBuilder.withObjectConverter(CompressionConverterBuilder.withObjectConverter(PackConverter.INSTANCE).build()).withObject(compression).withSubject(getSubject()).build();
    }
    
    /**
     * Encrypts the compressed content. The encryption has to be deactivated for public key chain queries.
     */
    @Pure
    public default @Nonnull Encryption<Signature<Compression<Pack>>> getEncryption(@Nonnull Compression<Pack> compression) throws ExternalException {
        return RequestEncryptionBuilder.withObject(getSignature(compression)).withRecipient(getRecipient()).build();
    }
    
    /**
     * Sends this method and returns the response.
     */
    @NonCommitting
    @PureWithSideEffects
    @TODO(task = "Verify the signature of the response (and add a flag to disable this for public key retrieval)!", date = "2017-10-06", author = Author.KASPAR_ETTER, priority = Priority.HIGH)
    public default @Nonnull Response send() throws ExternalException {
        Log.debugging("Sending $ to $.", getClass().getSimpleName(), getSubject());
        
        final @Nonnull Compression<Pack> compression = CompressionBuilder.withObject(pack()).build();
        
        final @Nonnull Encryption<Signature<Compression<Pack>>> encryption = getEncryption(compression);
        final @Nonnull Request request = RequestBuilder.withEncryption(encryption).build();
        final @Nonnull Response response = request.send();
        
        // TODO: All checks still have to be performed somewhere!
        
        final @Nonnull Pack pack = response.getEncryption().getObject().getObject().getObject();
        if (pack.getType().equals(RequestExceptionReply.TYPE)) {
            final @Nonnull Pair<@Nullable Entity, @Nonnull HostSignature<Compression<Pack>>> provided = Pair.of(getEntity(), (HostSignature<Compression<Pack>>) response.getEncryption().getObject());
            final @Nonnull RequestExceptionReply requestExceptionReply = pack.unpack(RequestExceptionReplyConverter.INSTANCE, provided);
            throw requestExceptionReply.getRequestException();
        }
        
        return response;
    }
    
    /**
     * Sends this method and returns the reply that is recovered with the given converter.
     */
    @NonCommitting
    @PureWithSideEffects
    public default <@Unspecifiable REPLY extends Reply<ENTITY>> @Nonnull REPLY send(@Nonnull Converter<REPLY, @Nonnull Pair<@Nullable ENTITY, @Nonnull HostSignature<Compression<Pack>>>> converter) throws ExternalException {
        final @Nonnull Response response = send();
        final @Nonnull Pack pack = response.getEncryption().getObject().getObject().getObject(); // TODO: Maybe we could/should check the type of the pack and throw an exception if it does not match the converter.
        final @Nonnull Pair<@Nullable ENTITY, @Nonnull HostSignature<Compression<Pack>>> provided = Pair.of(getEntity(), (HostSignature<Compression<Pack>>) response.getEncryption().getObject()); // TODO: Make sure that a response is always signed by a host by changing the generic type and converter?
        final @Nonnull REPLY reply = pack.unpack(converter, provided);
        if (!reply.matches(this)) { throw DeclarationExceptionBuilder.withMessage("The received reply does not match the sent method.").withIdentity(getSubject().resolve()).build(); }
        return reply;
    }
    
//    /**
//     * Sends the block encoded by this method to the stored recipient.
//     * This method can be overridden to support, for example, one-time credentials.
//     * You might also want to return {@code false} for {@link #isSimilarTo(net.digitalid.service.core.handler.Method)}.
//     * 
//     * @return the response to the request that is encoded by this method.
//     * 
//     * @ensure return.getSize() == 1 : "The response contains one element.";
//     * @ensure return.hasRequest() : "The returned response has a request.";
//     */
//    @NonCommitting
//    public @Nonnull Response send() throws ExternalException {
//        final @Nullable RequestAudit requestAudit = RequestAudit.get(this);
//        final @Nonnull Response response;
//        try {
//            response = Method.send(FreezableArrayList.get(this).freeze(), requestAudit);
//        } catch (@Nonnull DatabaseException | NetworkException | InternalException | ExternalException | RequestException exception) {
//            if (requestAudit != null) { RequestAudit.release(this); }
//            throw exception;
//        }
//        if (requestAudit != null) { response.getAuditNotNull().executeAsynchronously(this); }
//        response.checkReply(0);
//        return response;
//    }
//    
//    /**
//     * Sends the block encoded by this method to the stored recipient.
//     * 
//     * @return the reply to this method, which may not be null.
//     * 
//     * @require !matches(null) : "This method does not match null.";
//     * 
//     * @ensure matches(return) : "This method matches the returned reply.";
//     */
//    @NonCommitting
//    @SuppressWarnings("unchecked")
//    public final @Nonnull <T extends Reply> T sendNotNull() throws ExternalException {
//        Require.that(!matches(null)).orThrow("This method does not match null.");
//        
//        return (T) send().getReplyNotNull(0);
//    }
//    
//    /**
//     * Returns whether the given methods are {@link #isSimilarTo(net.digitalid.service.core.handler.Method) similar} to each other (in both directions).
//     * 
//     * @param methods the methods to check for similarity.
//     * 
//     * @return whether the given methods are similar to each other.
//     */
//    @Pure
//    public static boolean areSimilar(@Nonnull @Frozen @NonEmpty @NonNullableElements ReadOnlyList<? extends Method> methods) {
//        Require.that(methods.isFrozen()).orThrow("The list of methods is frozen.");
//        Require.that(!methods.isEmpty()).orThrow("The list of methods is not empty.");
//        Require.that(!methods.containsNull()).orThrow("The list of methods does not contain null.");
//        
//        final @Nonnull ReadOnlyIterator<? extends Method> iterator = methods.iterator();
//        final @Nonnull Method reference = iterator.next();
//        while (iterator.hasNext()) {
//            final @Nonnull Method method = iterator.next();
//            if (!method.isSimilarTo(reference) || !reference.isSimilarTo(method)) { return false; }
//        }
//        return true;
//    }
//    
//    /**
//     * Returns the permissions required for the given methods.
//     * 
//     * @param methods the methods whose permissions are to be aggregated.
//     * 
//     * @return the permissions required for the given methods.
//     */
//    @Pure
//    private static ReadOnlyAgentPermissions getRequiredPermissions(@Nonnull ReadOnlyList<? extends Method> methods) {
//        final @Nonnull FreezableAgentPermissions permissions = FreezableAgentPermissions.withNoPermissions();
//        for (final @Nonnull Method method : methods) {
//            permissions.putAll(method.getRequiredPermissionsToExecuteMethod());
//        }
//        return permissions.freeze();
//    }
//    
//    /**
//     * Sends the blocks encoded by the given methods to the common recipient.
//     * 
//     * @param methods the methods whose blocks are to be sent.
//     * @param audit the request audit or null if no audit is requested.
//     * 
//     * @return a list of replies, which can be null, corresponding to the methods.
//     * 
//     * @throws FailedRequestException if the blocks of the methods could not be sent.
//     * 
//     * @require methods.isFrozen() : "The list of methods is frozen.";
//     * @require !methods.isEmpty() : "The list of methods is not empty.";
//     * @require methods.doesNotContainNull() : "The list of methods does not contain null.";
//     * @require areSimilar(methods) : "The methods are similar to each other.";
//     * 
//     * @ensure return.getSize() == methods.size() : "The returned response and the given methods have the same size.";
//     * @ensure return.hasRequest() : "The returned response has a request.";
//     */
//    @NonCommitting
//    public static @Nonnull Response send(@Nonnull ReadOnlyList<Method> methods, @Nullable RequestAudit audit) throws ExternalException {
//        Require.that(areSimilar(methods)).orThrow("The methods are similar to each other.");
//        
//        final @Nonnull Method reference = methods.getNonNullable(0);
//        final @Nullable Entity entity = reference.getEntity();
//        final @Nonnull InternalIdentifier subject = reference.getSubject();
//        final @Nonnull HostIdentifier recipient = reference.getRecipient();
//        final boolean lodged = reference.isLodged();
//        final @Nullable BigInteger value = reference.getValue();
//        
//        if (reference.isOnHost() && !reference.canBeSentByHosts()) { throw InternalException.get("These methods cannot be sent by hosts."); }
//        if (reference.isOnClient() && reference.canOnlyBeSentByHosts()) { throw InternalException.get("These methods cannot be sent by clients."); }
//        
//        if (reference instanceof ExternalQuery) {
//            final @Nonnull ReadOnlyAuthentications authentications;
//            if (reference instanceof IdentityQuery) {
//                authentications = FreezableAuthentications.NONE;
//            } else {
//                final @Nonnull Identity identity = subject.getIdentity();
//                if (entity != null && entity instanceof Role && identity instanceof Person) {
//                    authentications = Contact.get((Role) entity, (Person) identity).getAuthentications();
//                } else {
//                    authentications = FreezableAuthentications.NONE;
//                }
//            }
//            
//            if (authentications.isEmpty()) {
//                return new Request(methods, recipient, subject).send();
//            } else {
//                assert entity != null && entity instanceof Role;
//                final @Nonnull Role role = (Role) entity;
//                final @Nonnull Time time = Time.getCurrent();
//                final @Nonnull FreezableList<Credential> credentials;
//                final @Nullable FreezableList<CertifiedAttributeValue> certificates;
//                final @Nonnull ReadOnlyAgentPermissions permissions = getRequiredPermissions(methods);
//                if (authentications.contains(FreezableAuthentications.IDENTITY_BASED_TYPE)) {
//                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased(role, permissions);
//                    credentials = new FreezableArrayList<Credential>(credential);
//                    certificates = FreezableArrayList.getWithCapacity(authentications.size() - 1);
//                    for (final @Nonnull SemanticType type : authentications) {
//                        if (!type.equals(FreezableAuthentications.IDENTITY_BASED_TYPE)) {
//                            final @Nullable AttributeValue attributeValue = Attribute.get(entity, type).getValue();
//                            if (attributeValue != null && attributeValue.isCertified()) {
//                                final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.castTo(CertifiedAttributeValue.class);
//                                if (certifiedAttributeValue.isValid(time)) { certificates.add(certifiedAttributeValue); }
//                            }
//                        }
//                    }
//                    certificates.freeze();
//                } else {
//                    credentials = FreezableArrayList.getWithCapacity(authentications.size());
//                    for (final @Nonnull SemanticType type : authentications) {
//                        final @Nullable AttributeValue attributeValue = Attribute.get(entity, type).getValue();
//                        if (attributeValue != null && attributeValue.isCertified()) {
//                            final @Nonnull CertifiedAttributeValue certifiedAttributeValue = attributeValue.castTo(CertifiedAttributeValue.class);
//                            if (certifiedAttributeValue.isValid(time)) { credentials.add(ClientCredential.getAttributeBased(role, certifiedAttributeValue, permissions)); }
//                        }
//                    }
//                    certificates = null;
//                }
//                return new CredentialsRequest(methods, recipient, subject, audit, credentials.freeze(), certificates, lodged, value).send();
//            }
//        } else {
//            Require.that(entity != null).orThrow("The entity can only be null in case of external queries.");
//            
//            if (reference instanceof ExternalAction) {
//                if (entity instanceof Role) {
//                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased((Role) entity, getRequiredPermissions(methods));
//                    return new CredentialsRequest(methods, recipient, subject, audit, new FreezableArrayList<Credential>(credential).freeze(), null, lodged, value).send();
//                } else {
//                    return new HostRequest(methods, recipient, subject, entity.getIdentity().getAddress()).send();
//                }
//            } else {
//                assert reference instanceof InternalMethod;
//                if (!(entity instanceof Role)) { throw InternalException.get("The entity has to be a role in case of internal methods."); }
//                final @Nonnull Role role = (Role) entity;
//                final @Nonnull Agent agent = role.getAgent();
//                
//                final @Nonnull Restrictions restrictions = agent.getRestrictions();
//                for (final @Nonnull Method method : methods) {
//                    if (!restrictions.cover(((InternalMethod) method).getRequiredRestrictionsToExecuteMethod())) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The restrictions of the role do not cover the required restrictions."); }
//                }
//                
//                if (reference.getService().equals(CoreService.SERVICE)) {
//                    if (role.isNative()) {
//                        if (!agent.getPermissions().cover(getRequiredPermissions(methods))) { throw RequestException.get(RequestErrorCode.AUTHORIZATION, "The permissions of the client agent do not cover the required permissions."); }
//                        return new ClientRequest(methods, subject, audit, role.toNativeRole().getAgent().getCommitment().addSecret(role.getClient().getSecret())).send();
//                    } else {
//                        final @Nonnull ClientCredential credential = ClientCredential.getRoleBased(role.toNonNativeRole(), getRequiredPermissions(methods));
//                        return new CredentialsRequest(methods, recipient, subject, audit, new FreezableArrayList<Credential>(credential).freeze(), null, lodged, value).send();
//                    }
//                } else {
//                    final @Nonnull ClientCredential credential = ClientCredential.getIdentityBased(role, getRequiredPermissions(methods));
//                    return new CredentialsRequest(methods, recipient, subject, audit, new FreezableArrayList<Credential>(credential).freeze(), null, lodged, value).send();
//                }
//            }
//        }
//    }
    
    /* -------------------------------------------------- Similarity -------------------------------------------------- */
    
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
     */
    @Pure
    public default boolean isSimilarTo(@Nonnull Method<?> other) {
        return Objects.equals(this.getEntity(), other.getEntity())
                && this.getSubject().equals(other.getSubject())
                && this.getRecipient().equals(other.getRecipient())
                && this.getService().equals(other.getService());
    }
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public default void validate() {
        Handler.super.validate();
        
        Validate.that(!willBeSent() || !isOnHost() || canBeSentByHosts()).orThrow("Methods to be sent on hosts have to be sendable by hosts.");
        Validate.that(!willBeSent() || !isOnClient() || canBeSentByClients()).orThrow("Methods to be sent on clients have to be sendable by clients.");
        Validate.that(!hasBeenReceived() || isOnHost()).orThrow("Methods can only be received on hosts and the entity may not be null then.");
    }
    
}
