package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts and recovers an email person to and from both its internal and external representation.
 */
@Immutable
@GenerateSubclass
public abstract class EmailPersonConverter extends IdentityConverter<EmailPerson> {
    
    /* -------------------------------------------------- Instance -------------------------------------------------- */
    
    public static final @Nonnull EmailPersonConverter INSTANCE = new EmailPersonConverterSubclass(EmailPerson.class);
    
}
