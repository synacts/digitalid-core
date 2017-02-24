package net.digitalid.core.resolution.tables;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.identification.identifier.Identifier;
import net.digitalid.core.identification.identity.Category;
import net.digitalid.core.identification.identity.Identity;

/**
 * This type models an entry in the identity table.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public interface IdentityEntry extends RootInterface {
    
    @Pure
    @PrimaryKey
    // TODO: @AutoIncrement
    public @Nonnull Identity getIdentity();
    
    @Pure
    public @Nonnull Category getCategory();
    
    @Pure
    public @Nonnull Identifier getAddress();
    
    // TODO: The reply converter does not yet exist.
//    @Pure
//    public @Nullable Reply<?> getReply();
    
}
