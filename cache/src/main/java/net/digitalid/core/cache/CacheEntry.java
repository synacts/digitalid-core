package net.digitalid.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.pack.Pack;

/**
 * This type models an entry in the role table.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public interface CacheEntry extends RootInterface {
    
    @Pure
    @PrimaryKey
    public @Nonnull /* Role */ Long getRequester();
    
    @Pure
    @PrimaryKey
    public @Nonnull InternalIdentity getRequestee();
    
    @Pure
    @PrimaryKey
    public @Nonnull @AttributeType SemanticType getAttributeType();
    
    @Pure
    @PrimaryKey
    public boolean isFound();
    
    @Pure
    public @Nonnull Time getExpirationTime();
    
    @Pure
    public @Nullable Pack getAttributeValue();
    
    // TODO (as soon as replies can be converted)
//    @Pure
//    public @Nullable Reply<?> getReply();
    
}
