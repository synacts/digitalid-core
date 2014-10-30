package ch.virtualid.identifier;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.InternalNonHostIdentity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.identity.Type;
import ch.virtualid.interfaces.Immutable;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class represents non-host identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class NonHostIdentifier extends InternalIdentifier implements Immutable {
    
    /**
     * Returns whether the given string is a valid non-host identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid non-host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return InternalIdentifier.isConforming(string) && string.contains("@");
    }
    
    
    /**
     * Creates a non-host identifier with the given string.
     * 
     * @param string the string of the non-host identifier.
     * 
     * @require isValid(string) : "The string is a valid non-host identifier.";
     */
    public NonHostIdentifier(@Nonnull String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid non-host identifier.";
    }
    
    
    @Pure
    @Override
    public @Nonnull InternalNonHostIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException {
        final @Nonnull InternalNonHostIdentity identity = Mapper.getIdentity(this).toInternalNonHostIdentity();
        if (identity instanceof Type) ((Type) identity).ensureLoaded();
        return identity;
    }
    
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return new HostIdentifier(getString().substring(getString().indexOf("@") + 1));
    }
    
}
