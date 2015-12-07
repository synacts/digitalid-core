package net.digitalid.service.core.auxiliary;

import net.digitalid.database.core.converter.SQL;
import net.digitalid.service.core.converter.xdf.XDF;
import net.digitalid.utility.annotations.state.Immutable;

/**
 * A short string contains at most 64 characters.
 */
@Immutable
public final class ShortString implements XDF<ShortString, Object>, SQL<ShortString, Object> {
    
    public ShortString() {
        // TODO
    }
    
}
