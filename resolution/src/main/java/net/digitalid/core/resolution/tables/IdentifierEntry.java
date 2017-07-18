package net.digitalid.core.resolution.tables;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.generator.annotations.generators.GenerateTableConverter;
import net.digitalid.utility.rootclass.RootInterface;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;

import net.digitalid.core.identification.identifier.Identifier;

/**
 * This type models an entry in the identifier table.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateTableConverter
public interface IdentifierEntry extends RootInterface {
    
    @Pure
    @PrimaryKey
    public @Nonnull Identifier getIdentifier();
    
    @Pure
    @TODO(task = "This should generate a foreign key on the identity table.", date = "2017-02-24", author = Author.KASPAR_ETTER)
    public long getKey();
    
}
