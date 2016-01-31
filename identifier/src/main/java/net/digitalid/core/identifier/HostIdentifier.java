package net.digitalid.core.identifier;

import javax.annotation.Nonnull;

import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.exceptions.InternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.state.Validated;

import net.digitalid.database.core.Database;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.database.core.exceptions.state.value.CorruptParameterValueCombinationException;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;

import net.digitalid.core.conversion.NonRequestingConverters;

import net.digitalid.core.conversion.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.core.conversion.xdf.NonRequestingXDFConverter;

import net.digitalid.core.exceptions.NetworkException;
import net.digitalid.core.exceptions.RequestException;

import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.Identity;

import net.digitalid.core.resolution.Mapper;

/**
 * This class models host identifiers.
 */
@Immutable
public final class HostIdentifier extends InternalIdentifier {
    
    /* -------------------------------------------------- Digital ID Host Identifier -------------------------------------------------- */
    
    /**
     * Stores the host identifier {@code core.digitalid.net}.
     */
    public final static @Nonnull HostIdentifier DIGITALID = new HostIdentifier("core.digitalid.net");
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
    /**
     * Returns whether the given string is a valid host identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return InternalIdentifier.isConforming(string) && !string.contains("@");
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a host identifier with the given string.
     * 
     * @param string the string of the host identifier.
     */
    private HostIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid host identifier.";
    }
    
    /**
     * Returns a host identifier with the given string.
     * 
     * @param string the string of the host identifier.
     * 
     * @return a host identifier with the given string.
     */
    @Pure
    public static @Nonnull HostIdentifier get(@Nonnull @Validated String string) {
        return new HostIdentifier(string);
    }
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull HostIdentity getMappedIdentity() throws DatabaseException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof HostIdentity) { return (HostIdentity) identity; }
        else { throw CorruptParameterValueCombinationException.get("The mapped identity has a wrong type."); }
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull HostIdentity getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        return Mapper.getIdentity(this).castTo(HostIdentity.class);
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return this;
    }
    
    /* -------------------------------------------------- Host Name -------------------------------------------------- */
    
    /**
     * Returns this host identifier as a host name which can be used as a {@link Site#toString() prefix} in {@link Database database} tables.
     * Host identifiers consist of at most 38 characters. If the identifier starts with a digit, the host name is prepended by an underscore.
     * 
     * @return this host identifier as a host name which can be used as a {@link Site#toString() prefix} in {@link Database database} tables.
     * 
     * @ensure return.length() <= 39 : "The returned string has at most 39 characters.";
     */
    @Pure
    public @Nonnull String asHostName() {
        final @Nonnull String string = getString();
        return (Character.isDigit(string.charAt(0)) ? "_" : "") + string.replace(".", "_").replace("-", "$");
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("host_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<HostIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(HostIdentifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull NonRequestingXDFConverter<HostIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(HostIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<HostIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<HostIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
