package net.digitalid.core.property.value;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collaboration.annotations.TODO;
import net.digitalid.utility.collaboration.enumerations.Author;
import net.digitalid.utility.collections.list.ReadOnlyList;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.Frozen;
import net.digitalid.utility.functional.interfaces.Predicate;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Valid;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.auxiliary.Time;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.storage.Storage;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.concept.Concept;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.annotations.OnClientRecipient;
import net.digitalid.core.handler.method.action.Action;
import net.digitalid.core.handler.method.action.InternalAction;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.property.PropertyInternalAction;
import net.digitalid.core.restrictions.Restrictions;

/**
 * This class models the {@link InternalAction internal action} of a {@link WritableSynchronizedValueProperty writable synchronized value property}.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
//@GenerateConverter // TODO: Maybe the converter has to be written manually anyway (in order to recover the property). Otherwise, make sure the converter generator can handle generic types.
public abstract class ValuePropertyInternalAction<ENTITY extends Entity<?>, KEY, CONCEPT extends Concept<ENTITY, KEY>, VALUE> extends PropertyInternalAction<ENTITY, KEY, CONCEPT, WritableSynchronizedValueProperty<ENTITY, KEY, CONCEPT, VALUE>> implements Valid.Value<VALUE> {
    
    /* -------------------------------------------------- Validator -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Predicate<? super VALUE> getValueValidator() {
        return getProperty().getValueValidator();
    }
    
    /* -------------------------------------------------- Values -------------------------------------------------- */
    
    /**
     * Returns the time of the last modification.
     */
    @Pure
    protected abstract @Nullable Time getOldTime();
    
    /**
     * Returns the current time.
     */
    @Pure
    protected abstract @Nullable Time getNewTime();
    
    /**
     * Returns the old value of the property.
     */
    @Pure
    protected abstract @Valid VALUE getOldValue();
    
    /**
     * Returns the new value of the property.
     */
    @Pure
    protected abstract @Valid VALUE getNewValue();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        super.validate();
        Validate.that(!Objects.equals(getNewValue(), getOldValue())).orThrow("The new value $ may not be the same as the old value $.", getNewValue(), getOldValue());
    }
    
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
        getProperty().replace(getOldTime(), getNewTime(), getOldValue(), getNewValue());
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof ValuePropertyInternalAction<?, ?, ?, ?> && ((ValuePropertyInternalAction<?, ?, ?, ?>) action).getProperty().equals(getProperty());
    }
    
    @Pure
    @Override
    @OnClientRecipient
    public @Nonnull ValuePropertyInternalAction<ENTITY, KEY, CONCEPT, VALUE> getReverse() throws DatabaseException {
        return ValuePropertyInternalActionBuilder.withProperty(getProperty()).withOldValue(getNewValue()).withNewValue(getOldValue()).withOldTime(getNewTime()).withNewTime(getOldTime()).build();
    }
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredPermissionsToExecuteMethod().evaluate(getProperty().getConcept(), getNewValue());
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredRestrictionsToExecuteMethod().evaluate(getProperty().getConcept(), getNewValue());
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToExecuteMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredAgentToExecuteMethod().evaluate(getProperty().getConcept(), getNewValue());
    }
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredPermissionsToSeeMethod().evaluate(getProperty().getConcept(), getNewValue());
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredRestrictionsToSeeMethod().evaluate(getProperty().getConcept(), getNewValue());
    }
    
    @Pure
    @Override
    public @Nullable Agent getRequiredAgentToSeeMethod() {
        return getProperty().getTable().getRequiredAuthorization().getRequiredAgentToSeeMethod().evaluate(getProperty().getConcept(), getNewValue());
    }
    
}
