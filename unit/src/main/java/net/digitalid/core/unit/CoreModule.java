package net.digitalid.core.unit;

import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.storage.Module;
import net.digitalid.utility.validation.annotations.type.Mutable;

/**
 * Since the storage artifact is below the generator, the subclass and builder can no longer be generated there.
 */
@Mutable
@GenerateBuilder
@GenerateSubclass
public abstract class CoreModule extends Module {}
