package net.digitalid.core.cache;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.auxiliary.Time;

import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.asymmetrickey.PublicKeyRetriever;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.keychain.PublicKeyChain;

/**
 * This class implements the {@link PublicKeyRetriever}.
 */
@Stateless
@TODO(task = "Remove this class and inject it with a lambda expression instead?", date = "2016-12-03", author = Author.KASPAR_ETTER)
public class PublicKeyRetrieverImplementation implements PublicKeyRetriever {
    
    /* -------------------------------------------------- Retrieval -------------------------------------------------- */
    
    /**
     * Returns the public key chain of the given identity.
     */
    @Pure
    @NonCommitting
    public static @Nonnull PublicKeyChain getPublicKeyChain(@Nonnull HostIdentity identity) throws ExternalException {
        return Cache.getFreshAttributeContent(identity, null, /* TODO: PublicKeyChain.TYPE */ null, /* TODO: PublicKeyChainConverter.INSTANCE */ null, true);
    }
    
    @Pure
    @Override
    @NonCommitting
    public @Nonnull PublicKey getPublicKey(@Nonnull HostIdentity host, @Nonnull Time time) throws ExternalException {
        return getPublicKeyChain(host).getKey(time);
    }
    
    /* -------------------------------------------------- Injection -------------------------------------------------- */
    
    /**
     * Initializes the the public key retriever.
     */
    @Impure
    @Initialize(target = PublicKeyRetriever.class)
    public static void initializePublicKeyRetriever() {
        PublicKeyRetriever.configuration.set(new PublicKeyRetrieverImplementation());
    }
    
}