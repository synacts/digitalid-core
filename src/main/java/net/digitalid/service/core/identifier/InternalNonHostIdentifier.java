package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.block.wrappers.StringWrapper;
import net.digitalid.service.core.converter.NonRequestingConverters;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractNonRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingNonRequestingXDFConverter;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.InternalNonHostIdentity;
import net.digitalid.service.core.identity.Type;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.Locked;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.database.converter.AbstractSQLConverter;
import net.digitalid.utility.database.declaration.ColumnDeclaration;
import net.digitalid.utility.database.exceptions.DatabaseException;
import net.digitalid.utility.database.exceptions.state.value.CorruptParameterValueCombinationException;
import net.digitalid.utility.system.exceptions.InternalException;

/**
 * This class models internal non-host identifiers.
 */
@Immutable
public final class InternalNonHostIdentifier extends InternalIdentifier implements NonHostIdentifier {
    
    /* -------------------------------------------------- Validity -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull InternalNonHostIdentity getMappedIdentity() throws DatabaseException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof InternalNonHostIdentity) { return (InternalNonHostIdentity) identity; }
        else { throw CorruptParameterValueCombinationException.get("The mapped identity has a wrong type."); }
    }
    
    @Pure
    @Locked
    @Override
    @NonCommitting
    public @Nonnull InternalNonHostIdentity getIdentity() throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        final @Nonnull InternalNonHostIdentity identity = Mapper.getIdentity(this).castTo(InternalNonHostIdentity.class);
        // If the returned identity is a type, its fields need to be loaded from the type's attributes.
        if (identity instanceof Type) { ((Type) identity).ensureLoaded(); }
        return identity;
    }
    
    /* -------------------------------------------------- Host Identifier -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return HostIdentifier.get(getString().substring(getString().indexOf("@") + 1));
    }
    
    /* -------------------------------------------------- String with Dot -------------------------------------------------- */
    
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
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = Identifier.DECLARATION.renamedAs("internal_non_host_identifier");
    
    /**
     * Stores the key converter of this class.
     */
    public static final @Nonnull Identifier.StringConverter<InternalNonHostIdentifier> KEY_CONVERTER = new Identifier.StringConverter<>(InternalNonHostIdentifier.class);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractNonRequestingXDFConverter<InternalNonHostIdentifier, Object> XDF_CONVERTER = ChainingNonRequestingXDFConverter.get(KEY_CONVERTER, StringWrapper.getValueXDFConverter(InternalNonHostIdentity.IDENTIFIER));
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<InternalNonHostIdentifier, Object> SQL_CONVERTER = ChainingSQLConverter.get(KEY_CONVERTER, StringWrapper.getValueSQLConverter(DECLARATION));
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull NonRequestingConverters<InternalNonHostIdentifier, Object> CONVERTERS = NonRequestingConverters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
