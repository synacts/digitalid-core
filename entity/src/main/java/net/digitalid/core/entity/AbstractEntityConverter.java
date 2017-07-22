package net.digitalid.core.entity;

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
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.conversion.exceptions.RecoveryExceptionBuilder;
import net.digitalid.utility.conversion.interfaces.Decoder;
import net.digitalid.utility.conversion.interfaces.Encoder;
import net.digitalid.utility.conversion.model.CustomAnnotation;
import net.digitalid.utility.conversion.model.CustomField;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.immutable.ImmutableList;
import net.digitalid.utility.storage.Module;
import net.digitalid.utility.storage.TableImplementation;
import net.digitalid.utility.storage.interfaces.Unit;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.unit.CoreUnit;
import net.digitalid.core.unit.GeneralUnit;

import static net.digitalid.utility.conversion.model.CustomType.INTEGER64;

/**
 * Depending on the {@link CoreUnit unit}, an {@link Entity entity} is stored differently.
 * In both cases (on hosts and clients), no additional table is created to store entities.
 */
@Immutable
public abstract class AbstractEntityConverter<@Unspecifiable ENTITY extends Entity> extends TableImplementation<ENTITY, @Nonnull CoreUnit>{
    
    /* -------------------------------------------------- Reference -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull String getSchemaName(@Nonnull Unit unit) {
        if (unit instanceof CoreUnit) {
            final @Nonnull CoreUnit coreUnit = (CoreUnit) unit;
            if (coreUnit.isHost()) { return GeneralUnit.INSTANCE.getName(); }
            else if (coreUnit.isClient()) { return unit.getName(); }
        }
        throw CaseExceptionBuilder.withVariable("unit").withValue(unit).build();
    }
    
    @Pure
    @Override
    public @Nonnull String getTableName(@Nonnull Unit unit) {
        if (unit instanceof CoreUnit) {
            final @Nonnull CoreUnit coreUnit = (CoreUnit) unit;
            if (coreUnit.isHost()) { return "Identity"; } // TODO: Figure out the correct value and ideally reference instead of copy it.
            else if (coreUnit.isClient()) { return "RoleEntry"; }  // TODO: Figure out the correct value and ideally reference instead of copy it.
        }
        throw CaseExceptionBuilder.withVariable("unit").withValue(unit).build();
    }
    
    @Pure
    @Override
    public @Nonnull @NonNullableElements ImmutableList<String> getColumnNames(@Nonnull Unit unit) {
        if (unit instanceof CoreUnit) {
            final @Nonnull CoreUnit coreUnit = (CoreUnit) unit;
            if (coreUnit.isHost()) { return ImmutableList.withElements("identifier"); } // TODO: Figure out the correct value and ideally reference instead of copy it.
            else if (coreUnit.isClient()) { return ImmutableList.withElements("key"); }  // TODO: Figure out the correct value and ideally reference instead of copy it.
        }
        throw CaseExceptionBuilder.withVariable("unit").withValue(unit).build();
    }
    
    /* -------------------------------------------------- Parent Module -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nullable Module getParentModule() {
        return null;
    }
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    private static final @Nonnull ImmutableList<CustomField> fields = ImmutableList.withElements(CustomField.with(INTEGER64, "key", ImmutableList.withElements(CustomAnnotation.with(Pure.class))));
    
    @Pure
    @Override
    public @Nonnull ImmutableList< CustomField> getFields(@Nonnull Representation representation) {
        return fields;
    }
    
    /* -------------------------------------------------- Convert -------------------------------------------------- */
    
    @Pure
    @Override
    public <EXCEPTION extends ConnectionException> void convert(@NonCaptured @Unmodified @Nonnull ENTITY entity, @Nonnull @NonCaptured @Modified Encoder<EXCEPTION> encoder) throws EXCEPTION {
        encoder.encodeInteger64(entity.getKey());
    }
    
    /* -------------------------------------------------- Recover -------------------------------------------------- */
    
    /**
     * Returns the recovered entity.
     */
    @Pure
    protected abstract @Nonnull ENTITY recover(@Nonnull CoreUnit unit, long key) throws DatabaseException, RecoveryException;
    
    @Pure
    @Override
    @SuppressWarnings("unchecked")
    public @Capturable @Nonnull <EXCEPTION extends ConnectionException> ENTITY recover(@Nonnull @NonCaptured Decoder<EXCEPTION> decoder, @Nonnull CoreUnit unit) throws EXCEPTION, RecoveryException {
        final long key = decoder.decodeInteger64();
        try {
            return recover(unit, key);
        } catch (@Nonnull DatabaseException | RecoveryException exception) {
            throw RecoveryExceptionBuilder.withMessage("Could not recover an entity.").withCause(exception).build();
        }
    }
    
}
