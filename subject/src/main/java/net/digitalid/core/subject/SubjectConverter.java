package net.digitalid.core.subject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.subject.SubjectModule;

import net.digitalid.core.entity.CoreUnit;
import net.digitalid.core.entity.Entity;

/**
 * This class converts a {@link CoreSubject core subject} with its {@link Entity entity} and is used as the {@link SubjectModule#getSubjectConverter() subject converter} of {@link PersistentProperty persistent properties}.
 */
@Immutable
@GenerateSubclass
public abstract class SubjectConverter<@Unspecifiable ENTITY extends Entity<?>, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>> implements Converter<SUBJECT, @Nonnull CoreUnit> {
    
    /* -------------------------------------------------- Subject Module -------------------------------------------------- */
    
    /**
     * Returns the core subject module, which contains the entity and actual core subject converters.
     */
    @Pure
    public abstract @Nonnull CoreSubjectModule<ENTITY, KEY, SUBJECT> getSubjectModule();
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Support @Cached on methods without parameters.", date = "2016-09-24", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.LOW)
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(getSubjectModule().getEntityConverter()), "entity", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(getSubjectModule().getCoreSubjectConverter()), "key", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class)/* TODO: Pass them? Probably pass the whole custom field instead. */))
        );
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@Nonnull SUBJECT subject, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        getSubjectModule().getEntityConverter().convert(subject.getEntity(), encoder);
        getSubjectModule().getCoreSubjectConverter().convert(subject, encoder);
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    public @Capturable <@Unspecifiable EXCEPTION extends ConnectionException> @Nullable SUBJECT recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nonnull CoreUnit unit) throws EXCEPTION, RecoveryException {
        final @Nonnull ENTITY entity = getSubjectModule().getEntityConverter().recover(decoder, unit);
        return getSubjectModule().getCoreSubjectConverter().recover(decoder, entity);
    }
    
}
