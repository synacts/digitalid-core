package net.digitalid.core.unit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.storage.TableImplementation;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

/**
 * This class converts a specific {@link CoreUnit unit} so that the unit itself is {@link Provided provided}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class CoreUnitConverter<UNIT extends CoreUnit> extends TableImplementation<UNIT, UNIT> {
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return getType().getSimpleName();
    }
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return Strings.substringUntilLast(getType().getCanonicalName(), '.');
    }
    
    private static final @Nonnull ImmutableList<@Nonnull CustomField> FIELDS = ImmutableList.withElements();
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        return FIELDS;
    }
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nullable UNIT unit, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {}
    
    @Pure
    @Override
    public @Capturable <@Unspecifiable EXCEPTION extends ConnectionException> @Nullable UNIT recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nonnull UNIT unit) throws EXCEPTION {
        return unit;
    }
    
}
