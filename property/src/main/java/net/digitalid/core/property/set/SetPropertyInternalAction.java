package net.digitalid.core.property.set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.collections.set.ReadOnlySet;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.functional.interfaces.Predicate;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.storage.Storage;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.annotations.OnClientRecipient;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.PropertyInternalAction;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models the {@link InternalAction internal action} of a {@link WritableSynchronizedSetProperty writable synchronized set property}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
//@GenerateConverter // TODO: Maybe the converter has to be written manually anyway (in order to recover the property). Otherwise, make sure the converter generator can handle generic types.
public abstract class SetPropertyInternalAction<E extends Entity, K, C extends Concept<E, K>, V, R extends ReadOnlySet<@Nonnull @Valid V>, F extends FreezableSet<@Nonnull @Valid V>> extends PropertyInternalAction<E, K, C, WritableSynchronizedSetProperty<E, K, C, V, R, F>> implements Valid.Value<V> {
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Predicate<? super V> getValueValidator() {
        return getProperty().getValueValidator();
    }
    
    /* -------------------------------------------------- Values -------------------------------------------------- */
    
    /**
     * Returns the value added to or removed from the property.
     */
    @Pure
    protected abstract @Nonnull @Valid V getValue();
    
    /**
     * Returns whether the value is added to or removed from the property.
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
    protected void executeOnBoth() throws DatabaseException {
        getProperty().modify(getValue(), isAdded());
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof SetPropertyInternalAction<?, ?, ?, ?, ?, ?> && ((SetPropertyInternalAction<?, ?, ?, ?, ?, ?>) action).getProperty().equals(getProperty());
    }
    
    @Pure
    @Override
    @OnClientRecipient
    public @Nonnull SetPropertyInternalAction<E, K, C, V, R, F> getReverse() throws DatabaseException {
        return SetPropertyInternalActionBuilder.withProperty(getProperty()).withValue(getValue()).withAdded(isAdded()).build();
    }
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredPermissionsToExecuteMethod().evaluate(getProperty().getConcept(), getValue());
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredRestrictionsToExecuteMethod().evaluate(getProperty().getConcept(), getValue());
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredAgentToExecuteMethod().evaluate(getProperty().getConcept(), getValue());
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredPermissionsToSeeMethod().evaluate(getProperty().getConcept(), getValue());
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredRestrictionsToSeeMethod().evaluate(getProperty().getConcept(), getValue());
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredAgentToSeeMethod().evaluate(getProperty().getConcept(), getValue());
    }
    
}
