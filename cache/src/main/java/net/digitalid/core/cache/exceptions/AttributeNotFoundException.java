package net.digitalid.core.cache.exceptions;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This exception is thrown when an attribute cannot be found.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class AttributeNotFoundException extends NotFoundException {}
