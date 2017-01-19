package net.digitalid.core.conversion.exceptions;

import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This exception is thrown whenever a network or file stream is corrupted by external causes.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class StreamException extends ConnectionException {}
