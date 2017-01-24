package net.digitalid.core.exceptions.response;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * A declaration exception indicates that a host returned an invalid declaration.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class DeclarationException extends ResponseException {}
