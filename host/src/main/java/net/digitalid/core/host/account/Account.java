package net.digitalid.core.host.account;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.UnexpectedValueException;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.validation.annotations.generation.Provided;
import net.digitalid.utility.validation.annotations.generation.Recover;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.core.entity.Entity;
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
    
    /* -------------------------------------------------- Host -------------------------------------------------- */
    
    /**
     * Returns the host of this account.
     */
    @Pure
    @Provided
    public abstract @Nonnull Host getHost();
    
    @Pure
    @Override
    public @Nonnull Host getSite() {
        return getHost();
    }
    
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
     * 
     * @param host the host of the account to return.
     * @param identity the identity of the account to return.
     * 
     * @return a new or existing account with the given host and identity.
     */
    @Pure
    @Recover
    public static @Nonnull Account with(@Nonnull Host host, @Nonnull InternalIdentity identity) {
        if (identity instanceof HostIdentity) {
            return HostAccount.with(host, (HostIdentity) identity);
        } else if (identity instanceof InternalNonHostIdentity) {
            return NonHostAccount.with(host, (InternalNonHostIdentity) identity);
        } else {
            throw UnexpectedValueException.with("identity", identity);
        }
    }
    
}
