package net.digitalid.core.property.map;

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
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.string.Strings;
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
 * This class implements the {@link Converter} for {@link MapPropertyInternalAction}.
 */
@Immutable
@GenerateSubclass
public abstract class MapPropertyInternalActionConverter<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable MAP_KEY, @Unspecifiable MAP_VALUE, @Specifiable PROVIDED_FOR_KEY, @Specifiable PROVIDED_FOR_VALUE> extends RootClass implements Converter<MapPropertyInternalAction<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE>, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity>> {
    
    /* -------------------------------------------------- Property Table -------------------------------------------------- */
    
    /**
     * Returns the property table, which contains the various converters.
     */
    @Pure
    protected abstract @Nonnull SynchronizedMapPropertyTable<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> getPropertyTable();
    
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
    public @Nonnull Class<? super MapPropertyInternalAction<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE>> getType() {
        return MapPropertyInternalAction.class;
    }
    
    /* -------------------------------------------------- Name -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @CodeIdentifier @MaxSize(63) String getTypeName() {
        return "MapPropertyInternalAction";
    }
    
    /* -------------------------------------------------- Package -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @DomainName String getTypePackage() {
        return "net.digitalid.core.property.map";
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<CustomField> getFields(@Nonnull Representation representation) {
        final @Nonnull SynchronizedMapPropertyTable<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        return ImmutableList.withElements(
                CustomField.with(CustomType.TUPLE.of(propertyTable.getParentModule().getSubjectTable()), "subject", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(propertyTable.getKeyConverter()), "key", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.TUPLE.of(propertyTable.getValueConverter()), "value", ImmutableList.withElements(CustomAnnotation.with(Nonnull.class))),
                CustomField.with(CustomType.BOOLEAN, "added")
        );
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <@Unspecifiable EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull MapPropertyInternalAction<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE> action, @NonCaptured @Modified @Nonnull Encoder<EXCEPTION> encoder) throws EXCEPTION {
        final @Nonnull SynchronizedMapPropertyTable<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        encoder.encodeObject(propertyTable.getParentModule().getCoreSubjectTable(), action.getProperty().getSubject());
        encoder.encodeObject(propertyTable.getKeyConverter(), action.getKey());
        encoder.encodeObject(propertyTable.getValueConverter(), action.getValue());
        encoder.encodeBoolean(action.isAdded());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public <@Unspecifiable EXCEPTION extends ConnectionException> @Nonnull MapPropertyInternalAction<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE> recover(@NonCaptured @Modified @Nonnull Decoder<EXCEPTION> decoder, @Nonnull Pair<@Nullable Signature<Compression<Pack>>, @Nonnull Entity> pair) throws EXCEPTION, RecoveryException {
        final @Nullable Signature<Compression<Pack>> signature = pair.get0();
        final @Nonnull Entity entity = pair.get1();
        
        if (!(entity instanceof NonHostEntity)) { throw RecoveryExceptionBuilder.withMessage("Internal actions can only be recovered for non-host entities.").build(); }
        
        final @Nonnull SynchronizedMapPropertyTable<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, PROVIDED_FOR_KEY, PROVIDED_FOR_VALUE> propertyTable = getPropertyTable();
        final @Nonnull SUBJECT subject = decoder.decodeObject(propertyTable.getParentModule().getCoreSubjectTable(), (ENTITY) entity);
        final @Nonnull PersistentProperty<?, ?> persistentProperty = subject.getProperty(propertyTable);
        
        if (!(persistentProperty instanceof WritableSynchronizedMapProperty)) { throw RecoveryExceptionBuilder.withMessage("The recovered persistent property is not a synchronized map property.").build(); }
        
        final @Nonnull WritableSynchronizedMapProperty<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, ?, ?> synchronizedProperty = (WritableSynchronizedMapProperty<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, ?, ?>) persistentProperty;
        
        final @Nonnull MAP_KEY key = decoder.decodeObject(propertyTable.getKeyConverter(), propertyTable.getProvidedObjectForKeyExtractor().evaluate(subject));
        if (!propertyTable.getKeyValidator().evaluate(key))  { throw RecoveryExceptionBuilder.withMessage(Strings.format("The key $ is not valid.", key)).build(); }
        
        final @Nonnull MAP_VALUE value = decoder.decodeObject(propertyTable.getValueConverter(), propertyTable.getProvidedObjectForValueExtractor().evaluate(subject, key));
        if (!propertyTable.getValueValidator().evaluate(value))  { throw RecoveryExceptionBuilder.withMessage(Strings.format("The value $ is not valid.", value)).build(); }
        
        final boolean added = decoder.decodeBoolean();
        
        return new MapPropertyInternalActionSubclass<>(signature, synchronizedProperty, key, value, added);
    }
    
}
