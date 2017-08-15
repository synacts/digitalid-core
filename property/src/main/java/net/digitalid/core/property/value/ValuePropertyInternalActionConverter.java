package net.digitalid.core.property.value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Specifiable;
import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.NonCaptured;
import net.digitalid.utility.annotations.parameter.Modified;
import net.digitalid.utility.annotations.parameter.Unmodified;
import net.digitalid.utility.conversion.enumerations.Representation;
import net.digitalid.utility.conversion.exceptions.ConnectionException;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.conversion.model.CustomType;
import net.digitalid.utility.functional.interfaces.Predicate;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.string.Strings;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.time.TimeConverter;
import net.digitalid.utility.tuples.Pair;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.MaxSize;
import net.digitalid.utility.validation.annotations.string.CodeIdentifier;
import net.digitalid.utility.validation.annotations.string.DomainName;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.property.PersistentProperty;

import net.digitalid.core.compression.Compression;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.method.MethodIndex;
import net.digitalid.core.pack.Pack;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.subject.CoreSubject;

/**
 * This class implements the {@link Converter} for {@link ValuePropertyInternalAction}.
 */
@Immutable
@GenerateSubclass
public abstract class ValuePropertyInternalActionConverter<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Specifiable VALUE, @Specifiable PROVIDED_FOR_VALUE> extends RootClass implements Converter<ValuePropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE>, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity>> {
    
    /* -------------------------------------------------- Property Table -------------------------------------------------- */
    
    /**
     * Returns the property table, which contains the various converters.
     */
    @Pure
    protected abstract @Nonnull SynchronizedValuePropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> getPropertyTable();
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    protected void initialize() {
        super.initialize();
        
        MethodIndex.add(this, getPropertyTable().getActionType());
    }
    
    /* -------------------------------------------------- Type -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Class<? super ValuePropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE>> getType() {
        return ValuePropertyInternalAction.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "ValuePropertyInternalAction";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.property.value";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull SynchronizedValuePropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(propertyTable.getParentModule().getSubjectTable()), "subject", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(TimeConverter.INSTANCE), "oldTime", ImmutableList.withElements(CustomAnnotation.with(Nullable.class))),
                CustomField.with(CustomType.TUPLE.of(TimeConverter.INSTANCE), "newTime", ImmutableList.withElements(CustomAnnotation.with(Nullable.class))),
                CustomField.with(CustomType.TUPLE.of(propertyTable.getValueConverter()), "oldValue", ImmutableList.withElements(CustomAnnotation.with(Nullable.class))),
                CustomField.with(CustomType.TUPLE.of(propertyTable.getValueConverter()), "newValue", ImmutableList.withElements(CustomAnnotation.with(Nullable.class)))
        );
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull ValuePropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE> action, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        final @Nonnull SynchronizedValuePropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        encoder.encodeObject(propertyTable.getParentModule().getCoreSubjectTable(), action.getProperty().getSubject());
        encoder.encodeNullableObject(TimeConverter.INSTANCE, action.getOldTime());
        encoder.encodeNullableObject(TimeConverter.INSTANCE, action.getNewTime());
        encoder.encodeNullableObject(propertyTable.getValueConverter(), action.getOldValue());
        encoder.encodeNullableObject(propertyTable.getValueConverter(), action.getNewValue());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull ValuePropertyInternalAction<ENTITY, KEY, SUBJECT, VALUE> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity> pair) throws EXCEPTION, RecoveryException {
        final @Nullable Signature<Compression<Pack>> signature = pair.get0();
        final @Nonnull Entity entity = pair.get1();
        
        if (!(entity instanceof NonHostEntity)) { throw RecoveryExceptionBuilder.withMessage("Internal actions can only be recovered for non-host entities.").build(); }
        
        final @Nonnull SynchronizedValuePropertyTable<ENTITY, KEY, SUBJECT, VALUE, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        final @Nonnull SUBJECT subject = decoder.decodeObject(propertyTable.getParentModule().getCoreSubjectTable(), (ENTITY) entity);
        final @Nonnull PersistentProperty<?, ?> persistentProperty = subject.getProperty(propertyTable);
        
        if (!(persistentProperty instanceof WritableSynchronizedValueProperty)) { throw RecoveryExceptionBuilder.withMessage("The recovered persistent property is not a synchronized value property.").build(); }
        
        final @Nonnull WritableSynchronizedValueProperty<ENTITY, KEY, SUBJECT, VALUE> synchronizedProperty = (WritableSynchronizedValueProperty<ENTITY, KEY, SUBJECT, VALUE>) persistentProperty;
        
        final @Nullable Time oldTime = decoder.decodeNullableObject(TimeConverter.INSTANCE, null);
        final @Nullable Time newTime = decoder.decodeNullableObject(TimeConverter.INSTANCE, null);
        
        final @Nullable VALUE oldValue = decoder.decodeNullableObject(propertyTable.getValueConverter(), propertyTable.getProvidedObjectExtractor().evaluate(subject));
        final @Nullable VALUE newValue = decoder.decodeNullableObject(propertyTable.getValueConverter(), propertyTable.getProvidedObjectExtractor().evaluate(subject));
        
        final @Nonnull Predicate<? super VALUE> valueValidator = propertyTable.getValueValidator();
        if (!valueValidator.evaluate(oldValue))  { throw RecoveryExceptionBuilder.withMessage(Strings.format("The old value $ is not valid.", oldValue)).build(); }
        if (!valueValidator.evaluate(newValue))  { throw RecoveryExceptionBuilder.withMessage(Strings.format("The new value $ is not valid.", newValue)).build(); }
        
        return new ValuePropertyInternalActionSubclass<>(signature, synchronizedProperty, oldTime, newTime, oldValue, newValue);
    }
    
}
