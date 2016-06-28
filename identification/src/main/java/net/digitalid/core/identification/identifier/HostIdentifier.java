package net.digitalid.core.identification.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.IdentifierResolver;

/**
 * This interface models host identifiers.
 */
@Immutable
@GenerateSubclass
// TODO: @GenerateConverter
public interface HostIdentifier extends InternalIdentifier {
    
    /* -------------------------------------------------- Digital ID Host Identifier -------------------------------------------------- */
    
    /**
     * Stores the host identifier {@code core.digitalid.net}.
     */
    public final static @Nonnull HostIdentifier DIGITALID = HostIdentifier.with("core.digitalid.net");
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string is a valid host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return InternalIdentifier.isConforming(string) && !string.contains("@");
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns a host identifier with the given string.
     */
    @Pure
    public static @Nonnull HostIdentifier with(@Nonnull @Valid String string) {
        return new HostIdentifierSubclass(string);
    }
    
    /* -------------------------------------------------- Resolve -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public default @Nonnull HostIdentity getIdentity() throws ExternalException {
        return IdentifierResolver.configuration.get().resolve(this).castTo(HostIdentity.class);
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    @Pure
    @Override
    public default @Nonnull HostIdentifier getHostIdentifier() {
        return this;
    }
    
    /* -------------------------------------------------- Host Name -------------------------------------------------- */
    
    /**
     * Returns this host identifier as a host name which can be used as a prefix in database tables.
     * Host identifiers consist of at most 38 characters. If the identifier starts with a digit, the host name is prepended by an underscore.
     */
    @Pure
    @TODO(task = "If we take the host name as the database name and not as table prefixes, then we might be able to increase the length restrictions.", date = "2016-06-19", author = Author.KASPAR_ETTER)
    public default @Nonnull @MaxSize(39) String asHostName() {
        final @Nonnull String string = getString();
        return (Character.isDigit(string.charAt(0)) ? "_" : "") + string.replace(".", "_").replace("-", "$");
    }
    
}
