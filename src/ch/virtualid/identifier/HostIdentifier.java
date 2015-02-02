package ch.virtualid.identifier;

import ch.virtualid.annotations.DoesNotCommit;
import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.HostIdentity;
import ch.virtualid.identity.Identity;
import ch.virtualid.identity.Mapper;
import ch.virtualid.interfaces.Immutable;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models host identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public final class HostIdentifier extends InternalIdentifier implements Immutable {
    
    /**
     * Stores the host identifier {@code virtualid.ch}.
     */
    public final static @Nonnull HostIdentifier VIRTUALID = new HostIdentifier("virtualid.ch");
    
    
    /**
     * Returns whether the given string is a valid host identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid host identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        return InternalIdentifier.isConforming(string) && !string.contains("@");
    }
    
    
    /**
     * Creates a host identifier with the given string.
     * 
     * @param string the string of the host identifier.
     * 
     * @require isValid(string) : "The string is a valid host identifier.";
     */
    public HostIdentifier(@Nonnull String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid host identifier.";
    }
    
    
    @Pure
    @Override
    @DoesNotCommit
    public @Nonnull HostIdentity getMappedIdentity() throws SQLException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof HostIdentity) return (HostIdentity) identity;
        else throw new SQLException("The mapped identity has a wrong type.");
    }
    
    @Pure
    @Override
    @DoesNotCommit
    public @Nonnull HostIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException {
        return Mapper.getIdentity(this).toHostIdentity();
    }
    
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return this;
    }
    
    
    /**
     * Returns this host identifier as a host name which can be used as a {@link Site#toString() prefix} in {@link Database database} tables.
     * 
     * @return this host identifier as a host name which can be used as a {@link Site#toString() prefix} in {@link Database database} tables.
     * 
     * @ensure return.length() <= 39 : "The returned string has at most 39 characters.";
     */
    @Pure
    public @Nonnull String asHostName() {
        final @Nonnull String string = getString();
        return (Character.isDigit(string.charAt(0)) ? "_" : "") + string.replace(".", "_").replace("-", "$");
    }
    
}
