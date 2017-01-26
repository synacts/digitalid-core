package net.digitalid.core.conversion.exceptions;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A network exception is thrown whenever a network stream is corrupted by external causes.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class NetworkException extends StreamException {}
