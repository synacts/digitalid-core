package net.digitalid.core.signature.exceptions;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This exception is thrown when a signature has expired.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class ExpiredSignatureException extends SignatureException {}
