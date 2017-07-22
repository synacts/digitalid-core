package net.digitalid.core.client;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collections.collection.ReadOnlyCollection;
import net.digitalid.utility.collections.map.FreezableLinkedHashMapBuilder;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.conversion.exceptions.ConversionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.property.value.ReadOnlyVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValuePropertyBuilder;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeBuilder;
import net.digitalid.utility.validation.annotations.equality.Unequal;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.generation.NonRepresentative;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.Committing;
import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.interfaces.Database;
import net.digitalid.database.property.set.WritablePersistentSimpleSetProperty;
import net.digitalid.database.subject.Subject;
import net.digitalid.database.subject.SubjectModule;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.client.role.NativeRole;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.commitment.CommitmentBuilder;
import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;
import net.digitalid.core.group.ExponentBuilder;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.parameters.Parameters;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.unit.CoreUnit;

/**
 * A client is configured with an identifier and a secret.
 * 
 * TODO: Make sure that the client secret gets rotated!
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class Client extends CoreUnit implements Subject<Client> {
    
    /* -------------------------------------------------- Clients -------------------------------------------------- */
    
    /**
     * Maps all created clients from their identifier.
     */
    private static final @Nonnull @NonFrozen FreezableMap<@Nonnull String, @Nonnull Client> clients = FreezableLinkedHashMapBuilder.build();
    
    /**
     * Returns all created clients.
     */
    @Pure
    public static @Nonnull @NonFrozen ReadOnlyCollection<@Nonnull Client> getClients() {
        return clients.values();
    }
    
    /**
     * Returns the client with the given identifier or throws a {@link RecoveryException} if no such service is found.
     */
    @Pure
    @Recover
    public static @Nonnull Client getClient(@Nonnull String identifier) throws RecoveryException {
        final @Nullable Client client = clients.get(identifier);
        if (client == null) { throw RecoveryExceptionBuilder.withMessage(Strings.format("No client with the identifier $ was found.", identifier)).build(); }
        return client;
    }
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Returns the identifier of this client.
     */
    @Pure
    public abstract @Nonnull @DomainName @MaxSize(62) String getIdentifier();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    @Derive("(Character.isDigit(identifier.charAt(0)) ? \"_\" : \"\") + identifier.replace(\".\", \"_\").replace(\"-\", \"$\")")
    public abstract @Nonnull @CodeIdentifier @MaxSize(63) @Unequal("general") String getName();
    
    /* -------------------------------------------------- Display Name -------------------------------------------------- */
    
    /**
     * Returns the name of this client that is used for accreditation.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull @MaxSize(50) String getDisplayName();
    
    /* -------------------------------------------------- Preferred Permissions -------------------------------------------------- */
    
    /**
     * Returns the preferred permissions of this client.
     */
    @Pure
    @NonRepresentative
    public abstract @Nonnull @Frozen ReadOnlyAgentPermissions getPreferredPermissions();
    
    /* -------------------------------------------------- Secret -------------------------------------------------- */
    
    protected final @Nonnull WritableVolatileValueProperty<@Nonnull Exponent> protectedSecret = WritableVolatileValuePropertyBuilder.withValue(ExponentBuilder.withValue(BigInteger.ZERO).build()).build();
    
    /**
     * Stores the secret of this client.
     */
    public final @Nonnull ReadOnlyVolatileValueProperty<@Nonnull Exponent> secret = protectedSecret;
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    @NonCommitting
    protected void initialize() throws ConversionException {
        super.initialize();
        
        protectedSecret.set(ClientSecretLoader.load(getIdentifier()));
    }
    
    /* -------------------------------------------------- CoreUnit -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean isHost() {
        return false;
    }
    
    @Pure
    @Override
    public final boolean isClient() {
        return true;
    }
    
    /* -------------------------------------------------- Commitment -------------------------------------------------- */
    
    /**
     * Returns a new commitment for the given subject with the given secret.
     */
    @Pure
    @NonCommitting
    protected @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject, @Nonnull Exponent secret) throws ExternalException {
        final @Nonnull HostIdentity host = subject.getHostIdentifier().resolve();
        final @Nonnull Time time = TimeBuilder.build();
        final @Nonnull PublicKey publicKey = PublicKeyRetriever.retrieve(host, time);
        final @Nonnull Element value = publicKey.getAu().pow(secret);
        return CommitmentBuilder.withHost(host).withTime(time).withValue(value.getValue()).withPublicKey(publicKey).build();
    }
    
    /**
     * Returns a new commitment for the given subject.
     */
    @Pure
    @NonCommitting
    public @Nonnull Commitment getCommitment(@Nonnull InternalNonHostIdentifier subject) throws ExternalException {
        return getCommitment(subject, protectedSecret.get());
    }
    
    /**
     * Rotates the secret of this client.
     * 
     * TODO: Make sure that other instances of the same client learn about the key rotation.
     */
    @Committing
    @PureWithSideEffects
    public void rotateSecret() throws InterruptedException, ExternalException {
        final @Nonnull Exponent newSecret = ExponentBuilder.withValue(new BigInteger(Parameters.EXPONENT.get(), new SecureRandom())).build();
        final @Nonnull ReadOnlySet<NativeRole> roles = roles().get();
        Database.instance.get().commit();
        
        for (@Nonnull NativeRole role : roles) {
//            final @Nonnull Commitment newCommitment = getCommitment(role.getIssuer().getAddress(), newSecret);
            // TODO:
//            role.getAgent().setCommitment(newCommitment);
        }
        
        for (@Nonnull NativeRole role : roles) {
            // TODO?
//            role.waitForCompletion(CoreService.SERVICE);
        }
        
        ClientSecretLoader.store(getIdentifier(), newSecret);
        protectedSecret.set(newSecret);
    }
    
    /* -------------------------------------------------- Subject -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Client getUnit() {
        return this;
    }
    
    @Pure
    @Override
    public @Nullable SubjectModule<Client, ?> module() {
        return null;
    }
    
    /* -------------------------------------------------- Roles -------------------------------------------------- */
    
    private final @Nonnull NativeRolesProperty roles = new NativeRolesPropertySubclass(this);
    
    /**
     * Returns the native roles of this client.
     */
    @Pure
    public @Nonnull WritablePersistentSimpleSetProperty<Client, NativeRole> roles() {
        return roles;
    }
    
}
