package net.digitalid.core.host;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.property.value.ReadOnlyVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValueProperty;
import net.digitalid.utility.property.value.WritableVolatileValuePropertyBuilder;
import net.digitalid.utility.validation.annotations.equality.Unequal;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.asymmetrickey.KeyPair;
import net.digitalid.core.client.Client;
import net.digitalid.core.entity.CoreUnit;
import net.digitalid.core.host.account.HostAccount;
import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.keychain.PrivateKeyChain;
import net.digitalid.core.keychain.PublicKeyChain;
import net.digitalid.core.service.Service;

/**
 * A host stores a {@link KeyPair} and is run by a {@link Server}.
 * 
 * TODO: Make sure that the host keys get rotated!
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class Host extends CoreUnit {
    
//    @Pure
//    @Override
//    public @Nonnull String getEntityReference() {
//        return Mapper.REFERENCE;
//    }
    
    /*
    TODO: Support:
    services
    provider
    tokens
    members
    open/close
    */
    
    /* -------------------------------------------------- Identifier -------------------------------------------------- */
    
    /**
     * Returns the identifier of this host.
     */
    @Pure
    public abstract @Nonnull HostIdentifier getIdentifier();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @TODO(task = "Remove this method once derivation statements can throw exceptions", date = "2016-12-14", author = Author.KASPAR_ETTER)
    @Nonnull HostIdentity deriveIdentity(@Nonnull HostIdentifier identifier) {
        try {
            return identifier.resolve();
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    /**
     * Returns the identity of this host.
     */
    @Pure
    @Derive("deriveIdentity(identifier)") // TODO: The identity has to be mapped and not resolved (Mapper.mapHostIdentity(identifier)).
    public abstract @Nonnull HostIdentity getIdentity();
    
    /* -------------------------------------------------- Account -------------------------------------------------- */
    
    /**
     * Returns the account of this host.
     */
    @Pure
    @Derive("HostAccount.with(this, identity)")
    public abstract @Nonnull HostAccount getAccount();
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    @Derive("identifier.asSchemaName()")
    public abstract @Nonnull @CodeIdentifier @MaxSize(61) @Unequal("general") String getName();
    
    /* -------------------------------------------------- Private Key -------------------------------------------------- */
    
    protected final @Nonnull WritableVolatileValueProperty<@Nonnull PrivateKeyChain> protectedPrivateKeyChain = WritableVolatileValuePropertyBuilder.withValue((PrivateKeyChain) null /* TODO: PrivateKeyChainLoader.load(getIdentifier()) */).build();
    
    /**
     * Stores the private key chain of this host.
     */
    public final @Nonnull ReadOnlyVolatileValueProperty<@Nonnull PrivateKeyChain> privateKeyChain = protectedPrivateKeyChain;
    
    /* -------------------------------------------------- Public Key -------------------------------------------------- */
    
    protected final @Nonnull WritableVolatileValueProperty<@Nonnull PublicKeyChain> protectedPublicKeyChain = WritableVolatileValuePropertyBuilder.withValue((PublicKeyChain) null /* TODO: PublicKeyChainLoader.load(getIdentifier()) */).build();
    
    /**
     * Stores the public key chain of this host.
     */
    public final @Nonnull ReadOnlyVolatileValueProperty<@Nonnull PublicKeyChain> publicKeyChain = protectedPublicKeyChain;
    
    /* -------------------------------------------------- Client -------------------------------------------------- */
    
    /**
     * Returns the client associated with this host.
     */
    @Pure
    // TODO: What are the right permissions to pass here? Probably an aggregation of all the services.
    @Derive("net.digitalid.core.client.ClientBuilder.withIdentifier(identifier.getString()).withDisplayName(\"Host \" + identifier.getString()).withPreferredPermissions(net.digitalid.core.permissions.ReadOnlyAgentPermissions.GENERAL_WRITE).build()")
    public abstract @Nonnull Client getClient();
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    protected void initialize() /* throws ExternalException */ {
        try {
            protectedPrivateKeyChain.set(PrivateKeyChainLoader.load(getIdentifier()));
            protectedPublicKeyChain.set(PublicKeyChainLoader.load(getIdentifier()));
        } catch (@Nonnull ExternalException exception) {
            throw new RuntimeException(exception); // TODO
        }
        
        // TODO:
//        try {
//            final @Nonnull Attribute attribute = Attribute.of(getAccount(), PublicKeyChain.TYPE);
//            if (attribute.value().get() == null) {
//                final @Nonnull AttributeValue value;
//                if (Server.hasHost(HostIdentifier.DIGITALID) || identifier.equals(HostIdentifier.DIGITALID)) {
//                    // If the new host is running on the same server as 'core.digitalid.net', certify its public key immediately.
//                    value = new CertifiedAttributeValue(publicKeyChain, identity, PublicKeyChain.TYPE);
//                } else {
//                    value = new UncertifiedAttributeValue(publicKeyChain);
//                }
//                attribute.value().set(value);
//            }
//        } catch (@Nonnull DatabaseException exception) {
//            throw new RuntimeException(exception); // TODO
//        }
    }
    
    /* -------------------------------------------------- CoreUnit -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean isHost() {
        return true;
    }
    
    @Pure
    @Override
    public final boolean isClient() {
        return false;
    }
    
    /* -------------------------------------------------- Other -------------------------------------------------- */
    
    /**
     * Returns whether this host hosts the given identity.
     */
    @Pure
    public boolean hosts(@Nonnull InternalIdentity identity) {
        return identity.getAddress().getHostIdentifier().equals(getIdentifier());
    }
    
    /**
     * Returns whether this host supports the given service.
     */
    @Pure
    public boolean supports(@Nonnull Service service) {
        return true; // TODO!
    }
    
}
