package net.digitalid.core.subject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.collaboration.annotations.Review;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collaboration.enumerations.Priority;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.type.Embedded;
import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.subject.SubjectModule;

import net.digitalid.core.entity.CoreSite;
import net.digitalid.core.entity.Entity;

/**
 * This class converts a {@link CoreSubject concept} with its {@link Entity entity} and is used as the {@link SubjectModule#getSubjectConverter() subject converter} of {@link PersistentProperty persistent properties}.
 */
@Immutable
@GenerateSubclass
public abstract class SubjectConverter<ENTITY extends Entity<?>, KEY, CONCEPT extends CoreSubject<ENTITY, KEY>> implements Converter<CONCEPT, @Nonnull CoreSite<?>> {
    
    /* -------------------------------------------------- Concept Module -------------------------------------------------- */
    
    /**
     * Returns the concept module, which contains the entity and actual concept converters.
     */
    @Pure
    public abstract @Nonnull CoreSubjectModule<ENTITY, KEY, CONCEPT> getConceptModule();
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Support @Cached on methods without parameters.", date = "2016-09-24", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.LOW)
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields(@Nonnull Representation representation) {
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(getConceptModule().getEntityConverter()), "entity", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class), CustomAnnotation.with(Embedded.class))),
                CustomField.with(CustomType.TUPLE.of(getConceptModule().getConceptConverter()), "key", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class), CustomAnnotation.with(Embedded.class)/* TODO: Pass them? Probably pass the whole custom field instead. */))
        );
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable CONCEPT concept, @Nonnull @NonCaptured @Modified Encoder<X> encoder) throws X {
        int i = 1;
        i *= getConceptModule().getEntityConverter().convert(concept == null ? null : concept.getEntity(), encoder);
        i *= getConceptModule().getConceptConverter().convert(concept == null ? null : concept, encoder);
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    @Review(comment = "How would you handle the nullable recovered objects?", date = "2016-09-30", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.LOW)
    public @Capturable <X extends ExternalException> @Nullable CONCEPT recover(@Nonnull @NonCaptured @Modified Decoder<X> decoder, @Nonnull CoreSite<?> site) throws X {
        final @Nullable ENTITY entity = getConceptModule().getEntityConverter().recover(decoder, site);
        return entity != null ? getConceptModule().getConceptConverter().recover(decoder, entity) : null;
    }
    
}
