package net.digitalid.core.concept;

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
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomAnnotation;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.conversion.converter.ValueCollector;
import net.digitalid.utility.conversion.converter.types.CustomType;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.type.Embedded;
import net.digitalid.database.interfaces.Site;
import net.digitalid.database.property.PersistentProperty;
import net.digitalid.database.property.SubjectModule;

import net.digitalid.core.entity.Entity;

/**
 * This class converts a {@link Concept concept} with its {@link Entity entity} and is used as the {@link SubjectModule#getSubjectConverter() subject converter} of {@link PersistentProperty persistent properties}.
 */
@Immutable
@GenerateSubclass
public abstract class ConceptConverter<E extends Entity, K, C extends Concept<E, K>> implements Converter<C, @Nonnull Site> {
    
    /* -------------------------------------------------- Concept Module -------------------------------------------------- */
    
    /**
     * Returns the concept module, which contains the entity and actual concept converters.
     */
    @Pure
    public abstract @Nonnull ConceptModule<E, K, C> getConceptModule();
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Support @Cached on methods without parameters.", date = "2016-09-24", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.LOW)
    public @Nonnull ImmutableList<@Nonnull CustomField> getFields() {
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(getConceptModule().getEntityConverter()), "entity", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class), CustomAnnotation.with(Embedded.class))),
                CustomField.with(CustomType.TUPLE.of(getConceptModule().getConceptConverter()), "key", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class), CustomAnnotation.with(Embedded.class)/* TODO: Pass them? Probably pass the whole custom field instead. */))
        );
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <X extends ExternalException> int convert(@Nullable C concept, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws X {
        int i = 1;
        i *= getConceptModule().getEntityConverter().convert(concept == null ? null : concept.getEntity(), valueCollector);
        i *= getConceptModule().getConceptConverter().convert(concept == null ? null : concept, valueCollector);
        return i;
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    @Review(comment = "How would you handle the nullable recovered objects?", date = "2016-09-30", author = Author.KASPAR_ETTER, assignee = Author.STEPHANIE_STROKA, priority = Priority.LOW)
    public @Capturable <X extends ExternalException> @Nullable C recover(@Nonnull @NonCaptured @Modified SelectionResult<X> selectionResult, @Nonnull Site site) throws X {
        final @Nullable E entity = getConceptModule().getEntityConverter().recover(selectionResult, site);
        return entity != null ? getConceptModule().getConceptConverter().recover(selectionResult, entity) : null;
    }
    
}
