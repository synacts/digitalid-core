package net.digitalid.service.core.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.annotations.NonCommitting;
import net.digitalid.database.core.converter.sql.ChainingSQLConverter;
import net.digitalid.database.core.converter.sql.SQL;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.database.core.declaration.ColumnDeclaration;
import net.digitalid.database.core.exceptions.DatabaseException;
import net.digitalid.service.core.CoreService;
import net.digitalid.service.core.auxiliary.None;
import net.digitalid.service.core.cache.Cache;
import net.digitalid.service.core.concepts.attribute.Attribute;
import net.digitalid.service.core.concepts.attribute.AttributeValue;
import net.digitalid.service.core.converter.Converters;
import net.digitalid.service.core.converter.key.NonRequestingKeyConverter;
import net.digitalid.service.core.converter.xdf.ChainingRequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.RequestingXDFConverter;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.request.RequestErrorCode;
import net.digitalid.service.core.exceptions.request.RequestException;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;
import net.digitalid.utility.system.exceptions.external.ExternalException;
import net.digitalid.utility.system.exceptions.external.InvalidEncodingException;
import net.digitalid.utility.system.exceptions.external.MaskingInvalidEncodingException;
import net.digitalid.utility.system.exceptions.internal.InternalException;

/**
 * This class models a service of the Digital ID protocol.
 * 
 * @see CoreService
 */
@Immutable
public class Service extends DelegatingSiteStorageImplementation implements XDF<Service, Object>,  SQL<Service, Object> {
    
    /* -------------------------------------------------- Services -------------------------------------------------- */
    
    /**
     * Maps the services that are installed on this server from their type.
     */
    private static final @Nonnull FreezableMap<SemanticType, Service> services = FreezableLinkedHashMap.get();
    
    /**
     * Returns a collection of the services installed on this server.
     * 
     * @return a collection of the services installed on this server.
     */
    @Pure
    public static @Nonnull ReadOnlyCollection<Service> getServices() {
        return services.values();
    }
    
    /**
     * Returns the service with the given type.
     * 
     * @param type the type of the desired service.
     * 
     * @return the service with the given type.
     */
    @Pure
    public static @Nonnull Service getService(@Nonnull SemanticType type) throws RequestException {
        final @Nullable Service service = services.get(type);
        if (service == null) { throw RequestException.get(RequestErrorCode.SERVICE, "No service with the type " + type.getAddress() + " is installed."); }
        return service;
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Stores the type of this service.
     */
    private final @Nonnull SemanticType type;
    
    /**
     * Returns the type of this service.
     * 
     * @return the type of this service.
     */
    @Pure
    public final @Nonnull SemanticType getType() {
        return type;
    }
    
    /* -------------------------------------------------- Title -------------------------------------------------- */
    
    /**
     * Stores the title of this service.
     */
    private final @Nonnull String title;
    
    /**
     * Returns the title of this service.
     * 
     * @return the title of this service.
     */
    @Pure
    public final @Nonnull String getTitle() {
        return title;
    }
    
    /* -------------------------------------------------- Version -------------------------------------------------- */
    
    /**
     * Stores the version of this service.
     */
    private final @Nonnull String version;
    
    /**
     * Returns the version of this service.
     * 
     * @return the version of this service.
     */
    @Pure
    public final @Nonnull String getVersion() {
        return version;
    }
    
    /**
     * Returns the title with the version of this service.
     * 
     * @return the title with the version of this service.
     */
    @Pure
    public final @Nonnull String getTitleWithVersion() {
        return title + " (" + version + ")";
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new service with the given name, title and version.
     * 
     * @param name the name of the new service.
     * @param title the title of the new service.
     * @param version the version of the new service.
     */
    protected Service(@Nonnull @Validated String name, @Nonnull SemanticType type, @Nonnull String title, @Nonnull String version) {
        super(null, name);
        
        this.type = type;
        this.title = title;
        this.version = version;
        services.put(getType(), this);
    }
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    /**
     * Returns the recipient of internal methods for the given role.
     * 
     * @param role the role for which the recipient is to be returned.
     * 
     * @return the recipient of internal methods for the given role.
     */
    @Pure
    @NonCommitting
    public @Nonnull HostIdentifier getRecipient(@Nonnull Role role) throws DatabaseException {
        final @Nullable AttributeValue attributeValue = Attribute.get(role, getType()).getValue();
        if (attributeValue == null) { throw DatabaseException.get("The role " + role.getIdentity().getAddress() + " has no attribute of type " + getType().getAddress() + "."); }
        try {
            return HostIdentifier.XDF_CONVERTER.decodeNonNullable(None.OBJECT, attributeValue.getContent());
        } catch (@Nonnull InvalidEncodingException exception) {
            throw DatabaseException.get("The attribute of type " + getType().getAddress() + " of the role " + role.getIdentity().getAddress() + " does not encode a host identifier.", exception);
        }
    }
    
    /**
     * Returns the recipient of external methods for the given subject.
     * 
     * @param role the role that sends the method or null for hosts.
     * @param subject the subject for which the recipient is to be returned.
     * 
     * @return the recipient of external methods for the given subject.
     */
    @Pure
    @NonCommitting
    public @Nonnull HostIdentifier getRecipient(@Nullable Role role, @Nonnull InternalPerson subject) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        return HostIdentifier.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getFreshAttributeContent(subject, role, getType(), false));
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return getType().toString();
    }
    
    /* -------------------------------------------------- Key Converter -------------------------------------------------- */
    
    /**
     * Stores the key converter of this class.
     */
    private static final @Nonnull NonRequestingKeyConverter<Service, Object, SemanticType, Object> KEY_CONVERTER = new NonRequestingKeyConverter<Service, Object, SemanticType, Object>() {
        
        @Pure
        @Override
        public @Nonnull SemanticType convert(@Nonnull Service service) {
            return service.getType();
        }
        
        @Pure
        @Override
        public @Nonnull Service recover(@Nonnull Object none, @Nonnull SemanticType type) throws InvalidEncodingException {
            try {
                return getService(type);
            } catch (@Nonnull RequestException exception) {
                throw MaskingInvalidEncodingException.get(exception);
            }
        }
        
    };
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    /**
     * Stores the XDF converter of this class.
     */
    public static final @Nonnull RequestingXDFConverter<Service, Object> XDF_CONVERTER = ChainingRequestingXDFConverter.get(KEY_CONVERTER, SemanticType.XDF_CONVERTER);
    
    @Pure
    @Override
    public @Nonnull RequestingXDFConverter<Service, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    /**
     * Stores the declaration of this class.
     */
    public static final @Nonnull ColumnDeclaration DECLARATION = SemanticType.DECLARATION.renamedAs("service");
    
    /**
     * Stores the SQL converter of this class.
     */
    public static final @Nonnull SQLConverter<Service, Object> SQL_CONVERTER = ChainingSQLConverter.get(DECLARATION, KEY_CONVERTER, SemanticType.SQL_CONVERTER);
    
    @Pure
    @Override
    public @Nonnull SQLConverter<Service, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
    /* -------------------------------------------------- Converters -------------------------------------------------- */
    
    /**
     * Stores the converters of this class.
     */
    public static final @Nonnull Converters<Service, Object> CONVERTERS = Converters.get(XDF_CONVERTER, SQL_CONVERTER);
    
}
