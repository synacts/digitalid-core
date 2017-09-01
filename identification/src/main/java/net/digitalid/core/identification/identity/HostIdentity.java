package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.exceptions.UncheckedExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.identification.identifier.HostIdentifier;

/**
 * This interface models a host identity.
 */
@Immutable
@GenerateSubclass
public interface HostIdentity extends InternalIdentity {
    
    /* -------------------------------------------------- Digital ID Core Identity -------------------------------------------------- */
    
    /**
     * Maps the identity of the Digital ID core host.
     */
    @PureWithSideEffects
    public static @Nonnull HostIdentity mapDigitalIDCoreHostIdentity() {
        try {
            return (HostIdentity) IdentifierResolver.configuration.get().map(Category.HOST, HostIdentifier.DIGITALID);
        } catch (@Nonnull DatabaseException exception) {
            throw UncheckedExceptionBuilder.withCause(exception).build();
        }
    }
    
    /**
     * Stores the host identity of {@code core.digitalid.net}.
     */
    public final static @Nonnull HostIdentity DIGITALID = mapDigitalIDCoreHostIdentity();
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    @Pure
    @Override
    @NonRepresentative
    public @Nonnull HostIdentifier getAddress();
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
}
