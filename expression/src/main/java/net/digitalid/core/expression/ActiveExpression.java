package net.digitalid.core.expression;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.ownership.Capturable;
import net.digitalid.utility.collections.set.FreezableSet;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.freezable.annotations.NonFrozen;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.node.contact.Contact;

/**
 * This class models active expressions.
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class ActiveExpression extends AbstractExpression {
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that(getExpression().isActive()).orThrow("This expression has to be active but was $.", getString());
        super.validate();
    }
    
    /* -------------------------------------------------- Aggregation -------------------------------------------------- */
    
    /**
     * Returns the contacts denoted by this active expression.
     */
    @Pure
    @NonCommitting
    public @Capturable @Nonnull @NonFrozen FreezableSet<@Nonnull Contact> getContacts() throws DatabaseException {
        return getExpression().getContacts();
    }
    
}
