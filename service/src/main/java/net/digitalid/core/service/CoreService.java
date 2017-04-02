package net.digitalid.core.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.storage.Module;
import net.digitalid.database.storage.ModuleBuilder;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class models the core service.
 */
@Immutable
@GenerateSubclass
public abstract class CoreService extends Service {
    
    /* -------------------------------------------------- Singleton -------------------------------------------------- */
    
    /**
     * Stores the single instance of this service.
     */
    public static final @Nonnull CoreService INSTANCE = new CoreServiceSubclass(SemanticType.map("@digitalid.net") /* TODO: Identity.IDENTIFIER */, "Core Service", "1.0");
    
    /* -------------------------------------------------- Module -------------------------------------------------- */
    
    /**
     * Stores the root module of this service.
     */
    public static final @Nonnull Module MODULE = ModuleBuilder.withName("core").build();
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient(@Nonnull NonHostEntity entity) {
        return entity.getIdentity().getAddress().getHostIdentifier();
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getRecipient(@Nonnull InternalPerson subject, @Nullable NonHostEntity entity) {
        return subject.getAddress().getHostIdentifier();
    }
    
}
