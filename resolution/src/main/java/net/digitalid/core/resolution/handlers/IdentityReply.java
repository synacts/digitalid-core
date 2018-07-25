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
package net.digitalid.core.resolution.handlers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.identification.identity.Category;

/**
 * Replies the identity of the given subject.
 * 
 * @see IdentityQuery
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
@TODO(task = "Support predecessors and successor.", date = "2018-05-24", author = Author.KASPAR_ETTER)
public abstract class IdentityReply extends QueryReply<Entity> implements CoreHandler<Entity> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the category of the subject.
     */
    @Pure
    @Invariant(condition = "#.isInternalNonHostIdentity()", message = "The category denotes an internal non-host identity.")
    public abstract @Nonnull Category getCategory();
    
//    /**
//     * Returns the predecessors of the subject.
//     */
//    @Pure
//    public abstract @Nonnull @Frozen ReadOnlyPredecessors getPredecessors();
//    
//    /**
//     * Returns the successor of the subject.
//     */
//    @Pure
//    public abstract @Nullable InternalNonHostIdentifier getSuccessor();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
//    /**
//     * Creates a query reply for the identity of given subject.
//     * 
//     * @param subject the subject of this handler.
//     */
//    @NonCommitting
//    IdentityReply(@Nonnull InternalNonHostIdentifier subject) throws DatabaseException, RequestException {
//        super(subject);
//        
//        if (!subject.isMapped()) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The identity with the identifier " + subject + " does not exist on this host."); }
//        this.category = subject.getMappedIdentity().getCategory();
//        if (!category.isInternalNonHostIdentity()) { throw new SQLException("The category is " + category.name() + " instead of an internal non-host identity."); }
//        if (!FreezablePredecessors.exist(subject)) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The identity with the identifier " + subject + " is not yet initialized."); }
//        this.predecessors = FreezablePredecessors.get(subject);
//        this.successor = Successor.get(subject);
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
//    private IdentityReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException, InternalException {
//        super(entity, signature, number);
//        
//        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
//        this.category = Category.get(tuple.getNonNullableElement(0));
//        if (!category.isInternalNonHostIdentity()) { throw InvalidDeclarationException.get("The category is " + category.name() + " instead of an internal non-host identity.", getSubject(), this); }
//        this.predecessors = new FreezablePredecessors(tuple.getNonNullableElement(1)).freeze();
//        this.successor = tuple.isElementNull(2) ? null : IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(2)).castTo(InternalNonHostIdentifier.class);
//    }
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nonnull Method<Entity> method) {
        return method instanceof IdentityQuery;
    }
    
}
