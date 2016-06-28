package net.digitalid.core.identification.identity;

import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * This interface models an internal person.
 * 
 * @see NaturalPerson
 * @see ArtificialPerson
 */
@Mutable
// TODO: @GenerateConverter
public abstract class InternalPerson extends Person implements InternalNonHostIdentity {}
