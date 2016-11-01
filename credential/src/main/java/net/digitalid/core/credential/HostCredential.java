package net.digitalid.core.credential;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class models credentials on the host-side.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class HostCredential extends Credential {}
