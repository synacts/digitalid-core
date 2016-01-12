package net.digitalid.service.core.identity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.digitalid.database.core.converter.sql.SQLConverter;
import net.digitalid.service.core.converter.xdf.RequestingXDFConverter;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.system.castable.CastableObject;

/**
 * This class models a digital identity, which can change identifiers and hosts.
 * Note that instances of this class are not necessarily unique (e.g. after identities have been merged).
 * 
 * @see HostIdentity
 * @see NonHostIdentity
 */
@Immutable
abstract class IdentityImplementation extends CastableObject implements Identity {
    
    /* -------------------------------------------------- Key -------------------------------------------------- */
    
    /**
     * Stores the number that represents and indexes this identity.
     * The key remains the same after relocation but changes after merging.
     */
    private volatile long key;
    
    @Pure
    @Override
    public final long getKey() {
        return key;
    }
    
    /**
     * Sets the number that represents this identity.
     * 
     * @param key the new key of this identity.
     */
    final void setKey(long key) {
        this.key = key;
    }
    
    /* -------------------------------------------------- Constructor -------------------------------------------------- */
    
    /**
     * Creates a new identity with the given key.
     * 
     * @param key the number that represents this identity.
     */
    IdentityImplementation(long key) {
        this.key = key;
    }
    
    /* -------------------------------------------------- Object -------------------------------------------------- */
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof IdentityImplementation)) { return false; }
        final @Nonnull IdentityImplementation other = (IdentityImplementation) object;
        return this.key == other.key;
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return (int) (key ^ (key >>> 32));
    }
    
    @Pure
    @Override
    public final @Nonnull String toString() {
        return String.valueOf(key);
    }
    
    /* -------------------------------------------------- XDF Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull RequestingXDFConverter<Identity, Object> getXDFConverter() {
        return XDF_CONVERTER;
    }
    
    /* -------------------------------------------------- SQL Converter -------------------------------------------------- */
    
    @Pure
    @Override
    public final @Nonnull SQLConverter<Identity, Object> getSQLConverter() {
        return SQL_CONVERTER;
    }
    
}
