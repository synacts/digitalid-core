package net.digitalid.core.identifier;

import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.core.annotations.NonCommitting;
import net.digitalid.core.annotations.Pure;
import net.digitalid.core.database.Database;
import net.digitalid.core.entity.Site;
import net.digitalid.core.exceptions.external.ExternalException;
import net.digitalid.core.exceptions.packet.PacketException;
import net.digitalid.core.identity.HostIdentity;
import net.digitalid.core.identity.Identity;
import net.digitalid.core.identity.Mapper;
import net.digitalid.core.interfaces.Immutable;

/**
 * This class models host identifiers.
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0
 */
public final class HostIdentifier extends InternalIdentifier implements Immutable {
    
    /**
     * Stores the host identifier {@code core.digitalid.net}.
     */
    public final static @Nonnull HostIdentifier DIGITALID = new HostIdentifier("core.digitalid.net");
    
    
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
    @NonCommitting
    public @Nonnull HostIdentity getMappedIdentity() throws SQLException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof HostIdentity) return (HostIdentity) identity;
        else throw new SQLException("The mapped identity has a wrong type.");
    }
    
    @Pure
    @Override
    @NonCommitting
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
