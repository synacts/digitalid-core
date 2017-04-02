package net.digitalid.core.host.account;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.exceptions.CaseExceptionBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.factories.AccountFactory;
import net.digitalid.core.host.Host;
import net.digitalid.core.identification.identity.HostIdentity;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.InternalNonHostIdentity;

/**
 * This class models an account on the host.
 * 
 * @see HostAccount
 * @see NonHostAccount
 */
@Immutable
@GenerateConverter
public abstract class Account implements Entity {
    
    /* -------------------------------------------------- Unit -------------------------------------------------- */
    
    @Pure
    @Override
    @Provided
    public abstract @Nonnull Host getUnit();
    
    /* -------------------------------------------------- Identity -------------------------------------------------- */
    
    @Pure
    @Override
    public abstract @Nonnull InternalIdentity getIdentity();
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    @Pure
    @Override
    public long getKey() {
        return getIdentity().getKey();
    }
    
    /* -------------------------------------------------- Recovery -------------------------------------------------- */
    
    /**
     * Returns a potentially locally cached account.
     */
    @Pure
    @Recover
    public static @Nonnull Account with(@Nonnull Host unit, @Nonnull InternalIdentity identity) {
        if (identity instanceof HostIdentity) {
            return HostAccount.with(unit, (HostIdentity) identity);
        } else if (identity instanceof InternalNonHostIdentity) {
            return NonHostAccount.with(unit, (InternalNonHostIdentity) identity);
        } else {
            throw CaseExceptionBuilder.withVariable("identity").withValue(identity).build();
        }
    }
    
    /* -------------------------------------------------- Initializer -------------------------------------------------- */
    
    /**
     * Initializes the account factory.
     */
    @PureWithSideEffects
    @Initialize(target = AccountFactory.class)
    public static void initializeAccountFactory() {
        AccountFactory.configuration.set((host, identity) -> Account.with((Host) host, identity));
    }
    
}
