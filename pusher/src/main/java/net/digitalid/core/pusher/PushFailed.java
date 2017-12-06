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
// TODO

//package net.digitalid.core.pusher;
//
//import java.util.Random;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.collections.freezable.FreezableArray;
//import net.digitalid.utility.collections.readonly.ReadOnlyArray;
//import net.digitalid.utility.conversion.None;
//import net.digitalid.utility.exceptions.InternalException;
//import net.digitalid.utility.exceptions.external.MaskingInvalidEncodingException;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.system.errors.ShouldNeverHappenError;
//import net.digitalid.utility.validation.annotations.type.Immutable;
//
//import net.digitalid.database.annotations.transaction.NonCommitting;
//import net.digitalid.database.core.exceptions.DatabaseException;
//
//import net.digitalid.core.conversion.Block;
//import net.digitalid.core.conversion.wrappers.EncryptionWrapper;
//import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
//import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
//import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;
//import net.digitalid.core.conversion.wrappers.value.integer.Integer64Wrapper;
//import net.digitalid.core.packet.exceptions.RequestErrorCode;
//import net.digitalid.core.packet.exceptions.RequestException;
//
//import net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions;
//import net.digitalid.service.core.concepts.agent.Restrictions;
//import net.digitalid.service.core.dataservice.StateModule;
//import net.digitalid.service.core.entity.Entity;
//import net.digitalid.service.core.entity.NonHostAccount;
//import net.digitalid.service.core.handler.ActionReply;
//import net.digitalid.service.core.handler.ExternalAction;
//import net.digitalid.service.core.handler.Method;
//import net.digitalid.service.core.handler.Reply;
//import net.digitalid.service.core.identifier.HostIdentifier;
//import net.digitalid.service.core.identifier.IdentifierImplementation;
//import net.digitalid.service.core.identifier.InternalIdentifier;
//import net.digitalid.service.core.identity.SemanticType;
//import net.digitalid.service.core.packet.Packet;
//import net.digitalid.service.core.packet.Response;
//import net.digitalid.service.core.storage.Service;
//
///**
// * An action of this type is added to the audit if the {@link Pusher} failed to send an external action.
// * 
// * @see Pusher
// */
//@Immutable
//public final class PushFailed extends ExternalAction {
//    
//    /**
//     * Stores the semantic type {@code number.failed.push@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType NUMBER = SemanticType.map("number.failed.push@core.digitalid.net").load(Integer64Wrapper.XDF_TYPE);
//    
//    /**
//     * Stores the semantic type {@code subject.failed.push@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType SUBJECT = SemanticType.map("subject.failed.push@core.digitalid.net").load(SignatureWrapper.SUBJECT);
//    
//    /**
//     * Stores the semantic type {@code recipient.failed.push@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType RECIPIENT = SemanticType.map("recipient.failed.push@core.digitalid.net").load(EncryptionWrapper.RECIPIENT);
//    
//    /**
//     * Stores the semantic type {@code action.failed.push@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType ACTION = SemanticType.map("action.failed.push@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
//    
//    /**
//     * Stores the semantic type {@code failed.push@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType TYPE = SemanticType.map("failed.push@core.digitalid.net").load(TupleWrapper.XDF_TYPE, NUMBER, SUBJECT, RECIPIENT, ACTION);
//    
//    
//    /**
//     * Stores the number of this failed push.
//     */
//    private final @Nonnull long number;
//    
//    // TODO: Maybe include a string that explains the problem?
//    
//    /**
//     * Stores the action that could not be pushed.
//     */
//    private final @Nonnull ExternalAction action;
//    
//    /**
//     * Creates an external action to indicate a failed push.
//     * 
//     * @param account the account to which this handler belongs.
//     * @param action the action that could not be pushed.
//     * 
//     * @require account.getIdentity().equals(action.getEntityNotNull().getIdentity()) : "The account and the action's entity have the same identity.";
//     */
//    PushFailed(@Nonnull NonHostAccount account, @Nonnull ExternalAction action) {
//        super(account, action.getSubject(), action.getRecipient());
//        
//        Require.that(account.getIdentity().equals(action.getEntityNotNull().getIdentity())).orThrow("The account and the action's entity have the same identity.");
//        
//        this.number = new Random().nextLong();
//        this.action = action;
//    }
//    
//    /**
//     * Creates an external action that decodes the given block.
//     * 
//     * @param entity the entity to which this handler belongs.
//     * @param signature the signature of this handler.
//     * @param recipient the recipient of this method.
//     * @param block the content which is to be decoded.
//     * 
//     * @require signature.hasSubject() : "The signature has a subject.";
//     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
//     * 
//     * @ensure hasSignature() : "This handler has a signature.";
//     */
//    @NonCommitting
//    private PushFailed(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
//        super(entity, signature, recipient);
//        
//        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(4);
//        this.number = Integer64Wrapper.decode(elements.getNonNullable(0));
//        
//        final @Nonnull InternalIdentifier _subject = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(1)).castTo(InternalIdentifier.class);
//        final @Nonnull HostIdentifier _recipient = IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, elements.getNonNullable(2)).castTo(HostIdentifier.class);
//        final @Nonnull Block _block = SelfcontainedWrapper.decodeNonNullable(elements.getNonNullable(3));
//        try {
//            this.action = (ExternalAction) Method.get(entity, SignatureWrapper.encodeWithoutSigning(Packet.SIGNATURE, (Block) null, _subject), _recipient, _block);
//        } catch (@Nonnull RequestException | ClassCastException exception) {
//            throw MaskingInvalidEncodingException.get(exception);
//        }
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Block toBlock() {
//        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(4);
//        elements.set(0, Integer64Wrapper.encode(NUMBER, number));
//        elements.set(1, action.getSubject().toBlock().setType(SUBJECT));
//        elements.set(2, action.getRecipient().toBlock().setType(RECIPIENT));
//        elements.set(3, SelfcontainedWrapper.encodeNonNullable(ACTION, action.toBlock()));
//        return TupleWrapper.encode(TYPE, elements.freeze());
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String getDescription() {
//        return "Failed to push an external action of type " + action.getType().getAddress() + ".";
//    }
//    
//    
//    /**
//     * Returns the number of this failed push.
//     * 
//     * @return the number of this failed push.
//     */
//    @Pure
//    public long getNumber() {
//        return number;
//    }
//    
//    /**
//     * Returns the action that could not be pushed.
//     * 
//     * @return the action that could not be pushed.
//     */
//    @Pure
//    public @Nonnull ExternalAction getAction() {
//        return action;
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull Service getService() {
//        return action.getService();
//    }
//    
//    
//    @Override
//    public @Nullable ActionReply executeOnHost() throws RequestException {
//        throw RequestException.get(RequestErrorCode.METHOD, "Failed push actions cannot be executed on a host.");
//    }
//    
//    @Pure
//    @Override
//    public boolean matches(@Nullable Reply reply) {
//        return reply == null;
//    }
//    
//    @Override
//    @NonCommitting
//    public void executeOnClient() throws DatabaseException {
//        // TODO: Add this failed push to some list where the user can see it (see the Errors module).
//        action.executeOnFailure();
//    }
//    
//    @Override
//    @NonCommitting
//    public void executeOnFailure() throws DatabaseException {
//        throw ShouldNeverHappenError.get("Failed push actions should never be pushed themselves.");
//    }
//    
//    
//    @Pure
//    @Override
//    public boolean isSimilarTo(@Nonnull Method other) {
//        return false;
//    }
//    
//    @Override
//    public @Nullable Response send() throws InternalException {
//        throw InternalException.get("Failed push actions cannot be sent.");
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeAudit() {
//        return action.getFailedAuditPermissions();
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
//        return action.getFailedAuditRestrictions();
//    }
//    
//    
//    @Pure
//    @Override
//    public boolean equals(@Nullable Object object) {
//        if (protectedEquals(object) && object instanceof PushFailed) {
//            final @Nonnull PushFailed other = (PushFailed) object;
//            return this.number == other.number && this.action.equals(other.action);
//        }
//        return false;
//    }
//    
//    @Pure
//    @Override
//    public int hashCode() {
//        int hash = protectedHashCode();
//        hash = 89 * hash + (int) (number ^ (number >>> 32));
//        hash = 89 * hash + action.hashCode();
//        return hash;
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull SemanticType getType() {
//        return TYPE;
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull StateModule getModule() {
//        return PusherModule.MODULE;
//    }
//    
//    /**
//     * The factory class for the surrounding method.
//     */
//    private static final class Factory extends Method.Factory {
//        
//        static { Method.add(TYPE, new Factory()); }
//        
//        @Pure
//        @Override
//        @NonCommitting
//        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws ExternalException {
//            return new PushFailed(entity, signature, recipient, block);
//        }
//        
//    }
//    
//}
