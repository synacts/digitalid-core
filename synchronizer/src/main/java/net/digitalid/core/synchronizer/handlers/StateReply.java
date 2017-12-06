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

//package net.digitalid.core.synchronizer.handlers;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//import net.digitalid.utility.annotations.method.Pure;
//import net.digitalid.utility.exceptions.ExternalException;
//import net.digitalid.utility.validation.annotations.type.Immutable;
//
//import net.digitalid.database.annotations.transaction.NonCommitting;
//
//import net.digitalid.core.entity.NonHostEntity;
//import net.digitalid.core.handler.reply.QueryReply;
//import net.digitalid.core.identification.identity.SemanticType;
//import net.digitalid.core.service.Service;
//
///**
// * Replies the state of the given entity.
// * 
// * @see StateQuery
// */
//@Immutable
//final class StateReply extends QueryReply {
//    
//    /**
//     * Stores the semantic type {@code reply.module@core.digitalid.net}.
//     */
//    private static final @Nonnull SemanticType TYPE = SemanticType.map("reply.module@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
//    
//    
//    /**
//     * Stores the state of the given entity.
//     */
//    final @Nonnull Block state;
//    
//    /**
//     * Stores the service to which this state reply belongs.
//     */
//    private final @Nonnull Service service;
//    
//    /**
//     * Creates a query reply for the state of the given account.
//     * 
//     * @param account the account to which this query reply belongs.
//     * @param block the block that contains the state of the account.
//     * @param service the service to which this state reply belongs.
//     */
//    StateReply(@Nonnull NonHostAccount account, @Nonnull Block block, @Nonnull Service service) {
//        super(account);
//        
//        this.state = block;
//        this.service = service;
//    }
//    
//    /**
//     * Creates a query reply that decodes a packet with the given signature for the given entity.
//     * 
//     * @param entity the entity to which this handler belongs.
//     * @param signature the host signature of this handler.
//     * @param number the number that references this reply.
//     * @param block the content which is to be decoded.
//     * 
//     * @ensure hasSignature() : "This handler has a signature.";
//     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
//     */
//    @NonCommitting
//    private StateReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws ExternalException {
//        super(entity, signature, number);
//        
//        this.state = SelfcontainedWrapper.decodeNonNullable(block);
//        this.service = Service.getModule(state.getType()).getService();
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull Block toBlock() {
//        return SelfcontainedWrapper.encodeNonNullable(TYPE, state);
//    }
//    
//    @Pure
//    @Override
//    public @Nonnull String getDescription() {
//        return "Replies the state.";
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull Service getService() {
//        return service;
//    }
//    
//    
//    /**
//     * Updates the state of the given entity without committing.
//     * 
//     * @require isOnClient() : "This method is called on a client.";
//     */
//    @NonCommitting
//    void updateState() throws ExternalException {
//        final @Nonnull StateModule module = Service.getModule(state.getType());
//        final @Nonnull Role role = getRole();
//        module.removeState(role);
//        module.addState(role, state);
//    }
//    
//    
//    @Pure
//    @Override
//    public boolean equals(@Nullable Object object) {
//        return protectedEquals(object) && object instanceof StateReply && this.state.equals(((StateReply) object).state);
//    }
//    
//    @Pure
//    @Override
//    public int hashCode() {
//        return 89 * protectedHashCode() + state.hashCode();
//    }
//    
//    
//    @Pure
//    @Override
//    public @Nonnull SemanticType getType() {
//        return TYPE;
//    }
//    
//    /**
//     * The factory class for the surrounding method.
//     */
//    private static final class Factory extends Reply.Factory {
//        
//        static { Reply.add(TYPE, new Factory()); }
//        
//        @Pure
//        @Override
//        @NonCommitting
//        protected @Nonnull Reply create(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws ExternalException {
//            return new StateReply(entity, signature, number, block);
//        }
//        
//    }
//    
//}
