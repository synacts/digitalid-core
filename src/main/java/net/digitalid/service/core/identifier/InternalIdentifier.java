package net.digitalid.service.core.identifier;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.IdentityNotFoundException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models internal identifiers.
 * 
 * @see HostIdentifier
 * @see InternalNonHostIdentifier
 */
@Immutable
public abstract class InternalIdentifier extends IdentifierImplementation {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * The pattern that valid internal identifiers have to match.
     */
    private static final Pattern PATTERN = Pattern.compile("(?:(?:[a-z0-9]+(?:[._-][a-z0-9]+)*)?@)?[a-z0-9]+(?:[.-][a-z0-9]+)*\\.[a-z][a-z]+");
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     * At most 38 characters may follow after the @-symbol.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return IdentifierImplementation.isConforming(string) && PATTERN.matcher(string).matches() && string.length() - string.indexOf("@") < 40;
    }
    
    /**
     * Returns whether the given string is a valid internal identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid internal identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return string.contains("@") ? InternalNonHostIdentifier.isValid(string) : HostIdentifier.isValid(string);
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates an internal identifier with the given string.
     * 
     * @param string the string of the internal identifier.
     */
    InternalIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid internal identifier.";
    }
    
    /**
     * Returns a new internal identifier with the given string.
     * 
     * @param string the string of the new internal identifier.
     * 
     * @return a new internal identifier with the given string.
     */
    @Pure
    public static @Nonnull InternalIdentifier get(@Nonnull @Validated String string) {
        return string.contains("@") ? InternalNonHostIdentifier.get(string) : HostIdentifier.get(string);
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull InternalIdentity getMappedIdentity() throws AbortException;
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull InternalIdentity getIdentity() throws AbortException, PacketException, ExternalException, NetworkException;
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns whether an identity with this internal identifier exists.
     * 
     * @return whether an identity with this internal identifier exists.
     */
    @Pure
    @NonCommitting
    public final boolean exists() throws AbortException, PacketException, ExternalException, NetworkException {
        try {
            Mapper.getIdentity(this);
            return true;
        } catch (@Nonnull IdentityNotFoundException exception) {
            return false;
        }
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    /**
     * Returns the host part of this internal identifier.
     * 
     * @return the host part of this internal identifier.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getHostIdentifier();
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    private static final @Nonnull Caster<InternalIdentifier> CASTER = new Caster<InternalIdentifier>() {
        @Pure
        @Override
        protected @Nonnull InternalIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toInternalIdentifier();
        }
    };
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull XDFConverter<InternalIdentifier> XDF_CONVERTER = new XDFConverter<>(InternalIdentity.IDENTIFIER, CASTER);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<InternalIdentifier> SQL_CONVERTER = new SQLConverter<>(CASTER);
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<InternalIdentifier, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
