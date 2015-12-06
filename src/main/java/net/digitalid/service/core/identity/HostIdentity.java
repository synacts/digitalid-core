package net.digitalid.service.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.AbstractSQLConverter;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.block.wrappers.value.integer.Integer64Wrapper;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.sql.ChainingSQLConverter;
import net.digitalid.service.core.converter.xdf.AbstractXDFConverter;
import net.digitalid.service.core.converter.xdf.ChainingXDFConverter;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.service.core.identity.resolution.Category;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.errors.InitializationError;
import net.digitalid.utility.system.thread.annotations.MainThread;

/**
 * This class models a host identity.
 */
@Immutable
public final class HostIdentity extends IdentityImplementation implements InternalIdentity {
    
    /* -------------------------------------------------- Digital ID Host Identity -------------------------------------------------- */
    
    /**
     * Maps the given identifier to a new host identity.
     * 
     * @param identifier the identifier which is to be mapped.
     */
    @MainThread
    private static @Nonnull HostIdentity map(@Nonnull HostIdentifier identifier) {
        assert Threading.isMainThread() : "This method may only be called in the main thread.";
        
        try {
            return Mapper.mapHostIdentity(identifier);
        } catch (@Nonnull DatabaseException exception) {
            throw InitializationError.get("The host identity with the identifier " + identifier + " could not be mapped.", exception);
        }
    }
    
    /**
     * Stores the host identity of {@code core.digitalid.net}.
     */
    public static final @Nonnull HostIdentity DIGITALID = HostIdentity.map(HostIdentifier.DIGITALID);
    
    /* -------------------------------------------------- Address -------------------------------------------------- */
    
    /**
     * Stores the address of this host identity.
     */
    private final @Nonnull HostIdentifier address;
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getAddress() {
        return address;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new host identity with the given key and address.
     * 
     * @param key the number that represents this identity.
     * @param address the address of the new host identity.
     */
    HostIdentity(long key, @Nonnull HostIdentifier address) {
        super(key);
        
        this.address = address;
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.HOST;
    }
    
    /* -------------------------------------------------- Merging -------------------------------------------------- */
    
    @Pure
    @Override
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws DatabaseException {
        Mapper.unmap(this);
        throw exception;
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the semantic type {@code host@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.map("host@core.digitalid.net").load(Identity.IDENTIFIER);
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull AbstractXDFConverter<HostIdentity, Object> XDF_CONVERTER = ChainingXDFConverter.get(new Identity.IdentifierConverter<>(HostIdentity.class), Identifier.XDF_CONVERTER.setType(IDENTIFIER));
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull Identity.Declaration DECLARATION = new Identity.Declaration("host_identity", false);
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull AbstractSQLConverter<HostIdentity, Object> SQL_CONVERTER = ChainingSQLConverter.get(new Identity.LongConverter<>(HostIdentity.class), Integer64Wrapper.getValueSQLConverter(DECLARATION));
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<HostIdentity, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
