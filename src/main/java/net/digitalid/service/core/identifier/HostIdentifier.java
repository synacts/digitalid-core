package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.key.Caster;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.utility.database.exceptions.DatabaseException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.encoding.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identity.HostIdentity;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.configuration.Database;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.declaration.ColumnDeclaration;

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
        else { throw DatabaseException.get("The mapped identity has a wrong type."); }
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull HostIdentity getIdentity() throws DatabaseException, PacketException, ExternalException, NetworkException {
        return Mapper.getIdentity(this).toHostIdentity();
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
    
    /* -------------------------------------------------- Caster -------------------------------------------------- */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    public static final @Nonnull Caster<Identifier, HostIdentifier> CASTER = new Caster<Identifier, HostIdentifier>() {
        @Pure
        @Override
        protected @Nonnull HostIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.castTo(HostIdentifier.class);
        }
    };
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("host_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<HostIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(CASTER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<HostIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(HostIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<HostIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<HostIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
