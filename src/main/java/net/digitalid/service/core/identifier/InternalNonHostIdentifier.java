package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.Type;
import net.digitalid.service.core.identity.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models internal non-host identifiers.
 */
@Immutable
public final class InternalNonHostIdentifier extends InternalIdentifier implements NonHostIdentifier {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validity –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether the given string is a valid non-host identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid non-host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return InternalIdentifier.isConforming(string) && string.contains("@");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a non-host identifier with the given string.
     * 
     * @param string the string of the non-host identifier.
     */
    private InternalNonHostIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid non-host identifier.";
    }
    
    /**
     * Returns a non-host identifier with the given string.
     * 
     * @param string the string of the non-host identifier.
     * 
     * @return a non-host identifier with the given string.
     */
    @Pure
    public static @Nonnull InternalNonHostIdentifier get(@Nonnull @Validated String string) {
        return new InternalNonHostIdentifier(string);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Mapping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull InternalNonHostIdentity getMappedIdentity() throws AbortException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof InternalNonHostIdentity) return (InternalNonHostIdentity) identity;
        else throw AbortException.get("The mapped identity has a wrong type.");
    }
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull InternalNonHostIdentity getIdentity() throws AbortException, PacketException, ExternalException, NetworkException {
        final @Nonnull InternalNonHostIdentity identity = Mapper.getIdentity(this).toInternalNonHostIdentity();
        // If the returned identity is a type, its fields need to be loaded from the type's attributes.
        if (identity instanceof Type) ((Type) identity).ensureLoaded();
        return identity;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Host Identifier –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return HostIdentifier.get(getString().substring(getString().indexOf("@") + 1));
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– String with Dot –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the string of this identifier with a leading dot or @.
     * This is useful for dynamically creating subtypes of existing types.
     * 
     * @return the string of this identifier with a leading dot or @.
     */
    @Pure
    public @Nonnull String getStringWithDot() {
        final @Nonnull String string = getString();
        return (string.startsWith("@") ? "" : ".") + string;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    private static final @Nonnull Caster<InternalNonHostIdentifier> CASTER = new Caster<InternalNonHostIdentifier>() {
        @Pure
        @Override
        protected @Nonnull InternalNonHostIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toInternalNonHostIdentifier();
        }
    };
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory<InternalNonHostIdentifier> ENCODING_FACTORY = new EncodingFactory<>(InternalNonHostIdentity.IDENTIFIER, CASTER);
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull StoringFactory<InternalNonHostIdentifier> STORING_FACTORY = new StoringFactory<>(CASTER);
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<InternalNonHostIdentifier, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
