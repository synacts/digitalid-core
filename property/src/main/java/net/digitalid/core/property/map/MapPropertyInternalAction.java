package net.digitalid.core.property.map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.map.FreezableMap;
import net.digitalid.utility.collections.map.ReadOnlyMap;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.functional.interfaces.Predicate;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.storage.Storage;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.handler.method.action.InternalAction;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.PropertyInternalAction;
import net.digitalid.core.restrictions.Restrictions;
import net.digitalid.core.subject.CoreSubject;
import net.digitalid.core.unit.annotations.OnClientRecipient;

/**
 * This class models the {@link InternalAction internal action} of a {@link WritableSynchronizedMapProperty writable synchronized map property}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
//@GenerateConverter // TODO: Maybe the converter has to be written manually anyway (in order to recover the property). Otherwise, make sure the converter generator can handle generic types.
public abstract class MapPropertyInternalAction<@Unspecifiable ENTITY extends Entity, @Unspecifiable KEY, @Unspecifiable SUBJECT extends CoreSubject<ENTITY, KEY>, @Unspecifiable MAP_KEY, @Unspecifiable MAP_VALUE, @Unspecifiable READONLY_MAP extends ReadOnlyMap<@Nonnull @Valid("key") MAP_KEY, @Nonnull @Valid MAP_VALUE>, @Unspecifiable FREEZABLE_MAP extends FreezableMap<@Nonnull @Valid("key") MAP_KEY, @Nonnull @Valid MAP_VALUE>> extends PropertyInternalAction<ENTITY, KEY, SUBJECT, WritableSynchronizedMapProperty<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, READONLY_MAP, FREEZABLE_MAP>> implements Valid.Key<MAP_KEY>, Valid.Value<MAP_VALUE> {
    
    /* -------------------------------------------------- Validators -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Predicate<? super MAP_KEY> getKeyValidator() {
        return getProperty().getKeyValidator();
    }
    
    @Pure
    @Override
    public @Nonnull Predicate<? super MAP_VALUE> getValueValidator() {
        return getProperty().getValueValidator();
    }
    
    /* -------------------------------------------------- Values -------------------------------------------------- */
    
    /**
     * Returns the key added to or removed from the property.
     */
    @Pure
    protected abstract @Nonnull @Valid("key") MAP_KEY getKey();
    
    /**
     * Returns the value added to or removed from the property.
     */
    @Pure
    protected abstract @Nonnull @Valid MAP_VALUE getValue();
    
    /**
     * Returns whether the key and value are added to or removed from the property.
     */
    @Pure
    protected abstract boolean isAdded();
    
    /* -------------------------------------------------- Action -------------------------------------------------- */
    
    @Pure
    @Override
    @TODO(task = "Allow this to be provided as well?", date = "2016-11-12", author = Author.KASPAR_ETTER)
    public @Nonnull @Frozen ReadOnlyList<Storage> getStoragesToBeSuspended() {
        return super.getStoragesToBeSuspended();
    }
    
    /* -------------------------------------------------- Internal Action -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @PureWithSideEffects
    protected void executeOnBoth() throws DatabaseException, RecoveryException {
        getProperty().modify(getKey(), getValue(), isAdded());
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof MapPropertyInternalAction<?, ?, ?, ?, ?, ?, ?> && ((MapPropertyInternalAction<?, ?, ?, ?, ?, ?, ?>) action).getProperty().equals(getProperty());
    }
    
    @Pure
    @Override
    @OnClientRecipient
    public @Nonnull MapPropertyInternalAction<ENTITY, KEY, SUBJECT, MAP_KEY, MAP_VALUE, READONLY_MAP, FREEZABLE_MAP> getReverse() {
        return MapPropertyInternalActionBuilder.withProperty(getProperty()).withKey(getKey()).withValue(getValue()).withAdded(isAdded()).build();
    }
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredPermissionsToExecuteMethod().evaluate(getProperty().getSubject(), getKey());
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredRestrictionsToExecuteMethod().evaluate(getProperty().getSubject(), getKey());
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredAgentToExecuteMethod().evaluate(getProperty().getSubject(), getKey());
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredPermissionsToSeeMethod().evaluate(getProperty().getSubject(), getKey());
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredRestrictionsToSeeMethod().evaluate(getProperty().getSubject(), getKey());
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredAgentToSeeMethod().evaluate(getProperty().getSubject(), getKey());
    }
    
}
