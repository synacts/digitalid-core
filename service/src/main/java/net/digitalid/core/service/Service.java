package net.digitalid.core.service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.reference.Raw;
import net.digitalid.utility.collections.collection.ReadOnlyCollection;
import net.digitalid.utility.collections.map.FreezableLinkedHashMapBuilder;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.validation.annotations.generation.Normalize;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.exceptions.request.RequestErrorCode;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.identification.identity.SemanticType;

/**
 * This class models a service of the Digital ID protocol.
 */
@Immutable
@GenerateBuilder // TODO: Use the fields of the generated constructor and not the recover method here.
@GenerateSubclass
// TODO: Semantic types cannot be converted yet: @GenerateConverter // TODO: Make sure that only the type is stored (and used for comparison).
public abstract class Service extends RootClass {
    
    /* -------------------------------------------------- Constants -------------------------------------------------- */
    
    /**
     * Stores the core service.
     */
    // TODO: Generate a builder that uses the non-representative fields title and version.
    public static final @Nonnull Service CORE = Service.with(null /* TODO */, "Core Service", "1.0");
//    public static final @Nonnull Service CORE = ServiceBuilder.withType(null /* TODO */).withTitle("Core Service").withVersion("1.0").build();
    
    @Pure
    public static @Nonnull Service with(@Nonnull SemanticType type, @Nonnull String title, @Nonnull String version) {
        return new ServiceSubclass(type, title, version);
    }
    
    /* -------------------------------------------------- Services -------------------------------------------------- */
    
    /**
     * Maps the services that are installed on this site from their type.
     */
    private static final @Nonnull FreezableMap<@Nonnull SemanticType, @Nonnull Service> services = FreezableLinkedHashMapBuilder.build();
    
    /**
     * Adds the given service for the given type and returns that type afterwards.
     */
    @Pure // TODO: Should be impure but this is not allowed for immutable types at the moment.
    static @Nonnull SemanticType addService(@Nonnull SemanticType type, @Raw @Nonnull Service service) {
        services.put(type, service);
        return type;
    }
    
    /**
     * Returns the services installed on this site.
     */
    @Pure
    public static @Nonnull ReadOnlyCollection<@Nonnull Service> getServices() {
        return services.values();
    }
    
    /**
     * Returns the service with the given type or throws a {@link RequestException} if no such service is found.
     */
    @Pure
    @Recover
    public static @Nonnull Service getService(@Nonnull SemanticType type) throws RequestException {
        final @Nullable Service service = services.get(type);
        if (service == null) { throw RequestException.with(RequestErrorCode.SERVICE, "No service with the type $ was found.", type.getAddress().getString()); }
        return service;
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    /**
     * Returns the type of this service.
     */
    @Pure
    @Normalize("Service.addService(type, this)")
    public abstract @Nonnull SemanticType getType();
    
    /* -------------------------------------------------- Title -------------------------------------------------- */
    
    /**
     * Returns the title of this service.
     */
    @Pure
    public abstract @Nonnull String getTitle();
    
    /* -------------------------------------------------- Version -------------------------------------------------- */
    
    /**
     * Returns the version of this service.
     */
    @Pure
    public abstract @Nonnull String getVersion();
    
    /**
     * Returns the title with the version of this service.
     */
    @Pure
    public @Nonnull String getTitleWithVersion() {
        return getTitle() + " (" + getVersion() + ")";
    }
    
    /* -------------------------------------------------- Recipient -------------------------------------------------- */
    
    // TODO: Move the following code to an appropriate location.
    
//    /**
//     * Returns the recipient of internal methods for the given role.
//     */
//    @Pure
//    @NonCommitting
//    public @Nonnull HostIdentifier getRecipient(@Nonnull Role role) throws DatabaseException {
//        final @Nullable AttributeValue attributeValue = Attribute.get(role, getType()).getValue();
//        if (attributeValue == null) { throw DatabaseException.get("The role " + role.getIdentity().getAddress() + " has no attribute of type " + getType().getAddress() + "."); }
//        try {
//            return HostIdentifier.XDF_CONVERTER.decodeNonNullable(None.OBJECT, attributeValue.getContent());
//        } catch (@Nonnull InvalidEncodingException exception) {
//            throw DatabaseException.get("The attribute of type " + getType().getAddress() + " of the role " + role.getIdentity().getAddress() + " does not encode a host identifier.", exception);
//        }
//    }
//    
//    /**
//     * Returns the recipient of external methods for the given subject.
//     */
//    @Pure
//    @NonCommitting
//    public @Nonnull HostIdentifier getRecipient(@Nullable Role role, @Nonnull InternalPerson subject) throws ExternalException {
//        return HostIdentifier.XDF_CONVERTER.decodeNonNullable(None.OBJECT, Cache.getFreshAttributeContent(subject, role, getType(), false));
//    }
    
}
