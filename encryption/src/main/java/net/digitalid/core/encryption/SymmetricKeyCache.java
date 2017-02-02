package net.digitalid.core.encryption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.concurrency.map.ConcurrentHashMapBuilder;
import net.digitalid.utility.concurrency.map.ConcurrentMap;
import net.digitalid.utility.configuration.Configuration;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.type.Utility;

import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.auxiliary.TimeBuilder;

import net.digitalid.core.identification.identifier.HostIdentifier;
import net.digitalid.core.symmetrickey.SymmetricKey;
import net.digitalid.core.symmetrickey.SymmetricKeyBuilder;

/**
 * This class caches symmetric keys to reuse them for a given period for the corresponding host.
 */
@Utility
@TODO(task = "Use this class to cache the symmetric keys for a particular host.", date = "2017-02-02", author = Author.KASPAR_ETTER)
public abstract class SymmetricKeyCache {
    
    /**
     * Stores a cached symmetric key for every recipient.
     */
    private static final @Nonnull ConcurrentMap<HostIdentifier, Pair<Time, SymmetricKey>> symmetricKeys = ConcurrentHashMapBuilder.build();
    
    /**
     * Stores whether the caching of symmetric keys is activated.
     */
    public static final @Nonnull Configuration<Boolean> activation = Configuration.with(true);
    
    /**
     * Returns a new or cached symmetric key for the given recipient.
     * 
     * @param recipient the recipient for which a symmetric key is to be returned.
     * @param rotation determines how often the cached symmetric keys are rotated.
     */
    @Pure
    protected static @Nonnull SymmetricKey getSymmetricKey(@Nonnull HostIdentifier recipient, @Nonnull Time rotation) {
        if (activation.get()) {
            final @Nonnull Time time = TimeBuilder.build();
            @Nullable Pair<Time, SymmetricKey> value = symmetricKeys.get(recipient);
            if (value == null || value.get0().isLessThan(time.subtract(rotation))) {
                value = Pair.of(time, SymmetricKeyBuilder.build());
                symmetricKeys.put(recipient, value);
            }
            return value.get1();
        } else {
            return SymmetricKeyBuilder.build();
        }
    }
    
}
