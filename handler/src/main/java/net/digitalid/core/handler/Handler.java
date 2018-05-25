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
package net.digitalid.core.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.OrderOfAssignment;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.Reply;
import net.digitalid.core.identification.identifier.InternalIdentifier;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.pack.Packable;
import net.digitalid.core.service.Service;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.unit.annotations.CoreUnitBased;

/**
 * This type provides the features that all handlers share.
 * 
 * @see Method
 * @see Reply
 */
@Immutable
public interface Handler<@Unspecifiable ENTITY extends Entity> extends CoreUnitBased, Packable {
    
    /* -------------------------------------------------- Entity -------------------------------------------------- */
    
    /**
     * Returns the entity to which this handler belongs or null if it is impersonal.
     */
    @Pure
    @Provided
    public @Nullable ENTITY getEntity();
    
    @Pure
    @Override
    public default boolean isOnHost() {
        final @Nullable ENTITY entity = getEntity();
        return entity != null && entity.isOnHost();
    }
    
    @Pure
    @Override
    public default boolean isOnClient() {
        final @Nullable ENTITY entity = getEntity();
        return entity == null || entity.isOnClient();
    }
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    /**
     * Returns the subject that was provided with the builder.
     */
    @Pure
    @Default("null")
    @NonRepresentative
    public @Nullable InternalIdentifier getProvidedSubject();
    
    /**
     * Returns the subject of this handler.
     * 
     * The subject is stored as an identifier because for certain handlers
     * the corresponding identity is not known to (or does not yet) exist.
     */
    @Pure
    @OrderOfAssignment(1)
    @Derive("signature != null ? signature.getSubject() : providedSubject")
    public @Nonnull InternalIdentifier getSubject();
    
    /* -------------------------------------------------- Service -------------------------------------------------- */
    
    /**
     * Returns the service that this handler implements.
     * This allows methods of the same service (and recipient) to be sent together.
     */
    @Pure
    public @Nonnull Service getService();
    
    /* -------------------------------------------------- Signature -------------------------------------------------- */
    
    /**
     * Returns the signature of this handler.
     */
    @Pure
    @Provided
    @Default("null")
    @NonRepresentative
    public @Nullable Signature<Compression<Pack>> getSignature();
    
    /**
     * Returns whether this handler will be sent.
     */
    @Pure
    public default boolean willBeSent() {
        return getSignature() == null;
    }
    
    /**
     * Returns whether this handler has been received.
     */
    @Pure
    public default boolean hasBeenReceived() {
        return getSignature() != null;
    }
    
}
