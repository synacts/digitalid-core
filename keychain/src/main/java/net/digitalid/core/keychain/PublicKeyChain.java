package net.digitalid.core.keychain;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.FreezableLinkedList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.order.StrictlyDescending;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.asymmetrickey.PublicKey;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.identification.identity.SemanticTypeAttributesBuilder;
import net.digitalid.core.identification.identity.SyntacticType;

/**
 * This class models a {@link KeyChain key chain} of {@link PublicKey public keys}.
 */
@Immutable
@GenerateSubclass
@GenerateConverter
public abstract class PublicKeyChain extends KeyChain<PublicKey, PublicKeyChainItem> {
    
    /**
     * Stores the semantic type of the public key chain.
     */
    @TODO(task = "Declare the type correctly.", date = "2017-08-30", author = Author.KASPAR_ETTER)
    public static final @Nonnull @Loaded SemanticType TYPE = SemanticType.map(PublicKeyChainConverter.INSTANCE).load(SemanticTypeAttributesBuilder.withSyntacticBase(SyntacticType.BOOLEAN).withCategories(Category.ONLY_HOST).withCachingPeriod(Time.TROPICAL_YEAR).build());
    
    /**
     * Returns a new key chain with the given time and key.
     * 
     * @param time the time from when on the given key is valid.
     * @param key the key that is valid from the given time on.
     * 
     * @require time.isInPast() : "The time lies in the past.";
     */
    @Pure
    public static @Nonnull PublicKeyChain with(@Nonnull Time time, @Nonnull PublicKey key) {
        Require.that(time.isInPast()).orThrow("The time lies in the past.");
        
        return new PublicKeyChainSubclass(FreezableLinkedList.<PublicKeyChainItem>withElement(buildPublicKeyChainItem(time, key)).freeze());
    }
    
    @Pure
    private static @Nonnull PublicKeyChainItem buildPublicKeyChainItem(@Nonnull Time time, @Nonnull PublicKey publicKey) {
        return PublicKeyChainItemBuilder.withTime(time).withKey(publicKey).build();
    }
    
    @Pure
    @Override
    @Nonnull PublicKeyChainItem buildItem(@Nonnull Time time, @Nonnull PublicKey publicKey) {
        return buildPublicKeyChainItem(time, publicKey);
    }
    
    @Pure
    @Override
    protected @Nonnull PublicKeyChain createKeyChain(@Nonnull @Frozen @NonEmpty @StrictlyDescending ReadOnlyList<@Nonnull PublicKeyChainItem> items) {
        return new PublicKeyChainSubclass(items);
    }
    
}
