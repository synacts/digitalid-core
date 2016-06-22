package net.digitalid.core.identification.identity;

import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This class models an external person.
 * 
 * @see EmailPerson
 * @see MobilePerson
 */
@Mutable
@GenerateConverter
public abstract class ExternalPerson extends Person {}
