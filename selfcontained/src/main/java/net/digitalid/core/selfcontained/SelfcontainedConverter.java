package net.digitalid.core.selfcontained;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.converter.Converter;
import net.digitalid.utility.conversion.converter.CustomField;
import net.digitalid.utility.conversion.converter.SelectionResult;
import net.digitalid.utility.conversion.converter.ValueCollector;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.logging.exceptions.ExternalException;

/**
 *
 */
public class SelfcontainedConverter<T> implements Converter<Selfcontained<T>, Void> {
    
    @Pure
    @Override
    public @Nonnull ImmutableList<CustomField> getFields() {
        return null;
    }
    
    @Pure
    @Override 
    public <X extends ExternalException> int convert(@Nullable @NonCaptured @Unmodified Selfcontained<T> object, @Nonnull @NonCaptured @Modified ValueCollector<X> valueCollector) throws X {
        return 0;
    }
    
    @Pure
    @Override
    public @Nonnull Selfcontained<T> recover(@Nonnull @NonCaptured @Modified SelectionResult selectionResult, Void externallyProvided) {
//        final @Nonnull SemanticType semanticType = SemanticTypeConverter.INSTANCE.recover(selectionResult, null);
//        final @Nonnull Converter<T, ?> converter = XDF.lookupConverter(semanticType);
//        final @Nullable T object = converter.recover(selectionResult, null);
//        return SelfcontainedBuilder.withSemanticType(semanticType).withObject(object).build();
        return null;
    }
    
    @Pure
    @Override
    public @Nonnull String getName() {
        return null;
    }
}
