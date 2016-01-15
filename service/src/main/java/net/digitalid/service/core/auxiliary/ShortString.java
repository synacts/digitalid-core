package net.digitalid.service.core.auxiliary;

import net.digitalid.utility.validation.state.Immutable;

import net.digitalid.database.core.converter.sql.SQL;

import net.digitalid.service.core.converter.xdf.XDF;

/**
 * A short string contains at most 64 characters.
 */
@Immutable
public final class ShortString implements XDF<ShortString, Object>, SQL<ShortString, Object> {
    
    public ShortString() {
        // TODO
    }
    
}
