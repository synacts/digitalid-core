package net.digitalid.core.conversion.exceptions;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A memory exception would be thrown whenever a stream purely in memory throws an exception, which should never happen.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class MemoryException extends StreamException {}
