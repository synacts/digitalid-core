package net.digitalid.core.data;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.core.annotations.Immutable;
import net.digitalid.core.annotations.Loaded;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.annotations.Validated;
import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.attribute.AttributeValue;
import net.digitalid.core.cache.Cache;
import net.digitalid.core.collections.FreezableLinkedHashMap;
import net.digitalid.core.collections.FreezableMap;
import net.digitalid.core.collections.ReadOnlyCollection;
import net.digitalid.core.entity.Role;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.external.InvalidEncodingException;
import net.digitalid.core.exceptions.packet.PacketError;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.factory.FactoryBasedGlobalFactory;
import net.digitalid.core.factory.Storable;
import net.digitalid.core.identifier.HostIdentifier;
import net.digitalid.core.identifier.IdentifierClass;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.IdentityClass;
import net.digitalid.core.identity.InternalPerson;
import net.digitalid.core.identity.SemanticType;

/**
 * This class models a service.
 * 
 * @see CoreService
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
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
    public @Nonnull HostIdentifier getRecipient(@Nonnull Role role) throws SQLException, PacketException, InvalidEncodingException {
        final @Nullable AttributeValue attributeValue = Attribute.get(role, getType()).getValue();
        if (attributeValue == null) throw new PacketException(PacketError.AUTHORIZATION, "Could not read the attribute value of " + getType().getAddress() + ".");
        return IdentifierClass.create(attributeValue.getContent()).toHostIdentifier();
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
    public static final class Factory extends FactoryBasedGlobalFactory<Service, Object, Identity> {
        
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
