package net.digitalid.service.core.data;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.service.core.annotations.Loaded;
import net.digitalid.service.core.attribute.Attribute;
import net.digitalid.service.core.attribute.AttributeValue;
import net.digitalid.service.core.cache.Cache;
import net.digitalid.service.core.entity.Role;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.packet.PacketError;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identifier.IdentifierClass;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.IdentityClass;
import net.digitalid.service.core.identity.InternalPerson;
import net.digitalid.service.core.identity.SemanticType;
import net.digitalid.service.core.service.CoreService;
import net.digitalid.service.core.storing.FactoryBasedStoringFactory;
import net.digitalid.service.core.storing.Storable;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.collections.freezable.FreezableLinkedHashMap;
import net.digitalid.utility.collections.freezable.FreezableMap;
import net.digitalid.utility.collections.readonly.ReadOnlyCollection;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This class models a service.
 * 
 * @see CoreService
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public class Service extends StateModule implements Storable<Service, Object> {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Services –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Maps the services that are installed on this server from their type.
     */
    private static final @Nonnull FreezableMap<SemanticType, Service> services = FreezableLinkedHashMap.get();
    
    /**
     * Returns a list of the services installed on this server.
     * 
     * @return a list of the services installed on this server.
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
    public static @Nonnull Service getService(@Nonnull SemanticType type) throws PacketException {
        final @Nullable Service service = services.get(type);
        if (service != null) return service;
        throw new PacketException(PacketError.SERVICE, "No service with the type " + type.getAddress() + " is installed.");
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Type –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the type of this service.
     */
    private final @Nonnull @Loaded SemanticType type;
    
    /**
     * Returns the type of this service.
     * 
     * @return the type of this service.
     */
    @Pure
    public final @Nonnull @Loaded SemanticType getType() {
        return type;
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Title –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Version –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
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
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates a new service with the given name, title and version.
     * 
     * @param name the name of the new service.
     * @param title the title of the new service.
     * @param version the version of the new service.
     */
    protected Service(@Nonnull @Validated String name, @Nonnull @Loaded SemanticType type, @Nonnull String title, @Nonnull String version) {
        super(null, name);
        
        this.type = type;
        this.title = title;
        this.version = version;
        services.put(getType(), this);
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Recipient –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the recipient of internal methods for the given role.
     * 
     * @param role the role for which the recipient is to be returned.
     * 
     * @return the recipient of internal methods for the given role.
     */
    @Pure
    @NonCommitting
    public @Nonnull HostIdentifier getRecipient(@Nonnull Role role) throws SQLException {
        final @Nullable AttributeValue attributeValue = Attribute.get(role, getType()).getValue();
        if (attributeValue == null) throw new SQLException("The role " + role.getIdentity().getAddress() + " has no attribute of type " + getType().getAddress() + ".");
        try {
            return IdentifierClass.create(attributeValue.getContent()).toHostIdentifier();
        } catch (@Nonnull InvalidEncodingException exception) {
            throw new SQLException("The attribute of type " + getType().getAddress() + " of the role " + role.getIdentity().getAddress() + " does not encode a host identifier.", exception);
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
    public @Nonnull HostIdentifier getRecipient(@Nullable Role role, @Nonnull InternalPerson subject) throws SQLException, IOException, PacketException, ExternalException {
        return IdentifierClass.create(Cache.getFreshAttributeContent(subject, role, getType(), false)).toHostIdentifier();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Object –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return getType().toString();
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Storable –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * The factory for this class.
     */
    @Immutable
    public static final class Factory extends FactoryBasedStoringFactory<Service, Object, Identity> {
        
        /**
         * Creates a new factory.
         */
        private Factory() {
            super(SemanticType.IDENTIFIER, IdentityClass.FACTORY); // TODO: Redo after the identity is made storable.
        }
        
        @Pure
        @Override
        public @Nonnull Identity getKey(@Nonnull Service service) {
            return service.getType();
        }
        
        @Pure
        @Override
        public @Nonnull Service getObject(@Nonnull Object none, @Nonnull Identity identity) {
            return getService(identity);
        }
        
    }
    
    /**
     * Stores the factory of this class.
     */
    public static final @Nonnull Factory FACTORY = new Factory();
    
    @Pure
    @Override
    public @Nonnull Factory getFactory() {
        return FACTORY;
    }
    
}
