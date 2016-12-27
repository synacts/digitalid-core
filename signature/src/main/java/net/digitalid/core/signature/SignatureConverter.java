package net.digitalid.core.signature;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomAnnotation;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.Decoder;
import net.digitalid.utility.conversion.converter.Encoder;
import net.digitalid.utility.conversion.converter.Representation;
import net.digitalid.utility.conversion.converter.types.CustomType;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.functional.iterables.FiniteIterable;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.immutable.ImmutableMap;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.auxiliary.TimeConverter;

import static net.digitalid.utility.conversion.converter.types.CustomType.TUPLE;

/**
 * This class converts {@link Signature signatures}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class SignatureConverter<@Unspecifiable TYPE> implements Converter<Signature<TYPE>, Void> {
    
    /* -------------------------------------------------- Type Converter -------------------------------------------------- */
    
    @Pure
    public abstract @Nonnull Converter<TYPE, Void> getTypeConverter();
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super Signature<TYPE>> getType() {
        return Signature.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "Signature";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.signature";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    private static final @Nonnull @Frozen ReadOnlyList<@Nonnull CustomField> fields;
    
    static {
        final @Nonnull Map<@Nonnull String, @Nullable Object> time = new HashMap<>();
        
        fields = FreezableArrayList.withElements(
                // TODO
                CustomField.with(TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withMappingsOf(time))))
        );
    }
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull FiniteIterable<@Nonnull CustomField> customFieldForObject = FiniteIterable.of(CustomField.with(CustomType.TUPLE.of(getTypeConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class, ImmutableMap.withNoEntries()))));
        return ImmutableList.withElementsOf(fields.combine(customFieldForObject));
    }
    
    /* -------------------------------------------------- Inheritance -------------------------------------------------- */
    
    // TODO
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <EXCEPTION extends ExternalException> int convert(@NonCaptured @Unmodified @Nullable Signature<TYPE> object, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        return 1;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable <EXCEPTION extends ExternalException> @Nullable Signature<TYPE> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, Void provided) throws EXCEPTION {
        return null;
    }
    
}
