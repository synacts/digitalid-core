package net.digitalid.service.core.identifier;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.block.wrappers.value.string.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.NonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.external.notfound.IdentityNotFoundException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.InternalIdentity;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.system.exceptions.external.ExternalException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

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
    public abstract @Nonnull InternalIdentity getMappedIdentity() throws DatabaseException;
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull InternalIdentity getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException;
    
    /* -------------------------------------------------- Existence -------------------------------------------------- */
    
    /**
     * Returns whether an identity with this internal identifier exists.
     * 
     * @return whether an identity with this internal identifier exists.
     */
    @Pure
    @NonCommitting
    public final boolean exists() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
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
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("internal_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<InternalIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(InternalIdentifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull NonRequestingXDFConverter<InternalIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(InternalIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<InternalIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<InternalIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
