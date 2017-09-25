package net.digitalid.core.signature.credentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.tuples.Pair;

import net.digitalid.core.group.Element;
import net.digitalid.core.group.Exponent;

/**
 *
 */
@GenerateBuilder
@GenerateSubclass
@TODO(task = "This was named 'array' in the old code, which is a terrible name that describes a data structure and not the object. Please rename.", assignee = Author.KASPAR_ETTER, author = Author.STEPHANIE_STROKA, date = "2017-08-16")
public abstract class VerifiableEncryptionParameters {
    
    @Pure
    public abstract @Nonnull Element getAo();
    
    @Pure
    public abstract @Nullable VerifiableEncryptionExponentPair getTwi();

    @Pure
    public abstract @Nullable VerifiableEncryptionExponentPair getTwb();
    
}
