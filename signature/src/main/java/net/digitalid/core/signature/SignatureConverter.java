package net.digitalid.core.signature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.constraints.PrimaryKey;
import net.digitalid.database.auxiliary.TimeConverter;

import net.digitalid.core.identification.identifier.InternalIdentifierConverter;
import net.digitalid.core.signature.client.ClientSignatureConverter;
import net.digitalid.core.signature.host.HostSignatureConverter;

import static net.digitalid.utility.conversion.model.CustomType.TUPLE;

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
    
    @Pure
    @Override
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        return ImmutableList.withElements(
                CustomField.with(TUPLE.of(TimeConverter.INSTANCE), "time", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class), CustomAnnotation.with(PrimaryKey.class))),
                CustomField.with(TUPLE.of(getTypeConverter()), "object", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(TUPLE.of(InternalIdentifierConverter.INSTANCE), "subject", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)))
        );
    }
    
    /* -------------------------------------------------- Inheritance -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable @NonEmpty ImmutableList<@Nonnull Converter<? extends Signature<TYPE>, Void>> getSubtypeConverters() {
        return ImmutableList.withElements(HostSignatureConverter.getInstance(getTypeConverter()), ClientSignatureConverter.getInstance(getTypeConverter()));
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <EXCEPTION extends ExternalException> int convert(@NonCaptured @Unmodified @Nullable Signature<TYPE> signature, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        int i = 1;
        if (signature != null) {
            encoder.encode(TimeConverter.INSTANCE, signature.getTime());
        } else {
            encoder.encode(TimeConverter.INSTANCE, null);
        }
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable <EXCEPTION extends ExternalException> @Nullable Signature<TYPE> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, Void provided) throws EXCEPTION {
        return null;
    }
    
}
