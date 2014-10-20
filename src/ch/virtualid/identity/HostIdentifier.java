package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.Block;
import ch.xdf.StringWrapper;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class represents identifiers of hosts.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class HostIdentifier extends Identifier implements Immutable {
    
    /**
     * Stores the host identifier {@code virtualid.ch}.
     */
    public final static @Nonnull HostIdentifier VIRTUALID = new HostIdentifier("virtualid.ch");
    
    
    /**
     * Verifies that the given string is a valid host identifier and throws an {@link InvalidEncodingException} otherwise.
     * 
     * @param string the string to verify.
     * 
     * @ensure Identifier.isValid(string) : "The string is a valid identifier.";
     * @ensure Identifier.isHost(string) : "The string denotes a host.";
     */
    public static void verify(@Nonnull String string) throws InvalidEncodingException {
        if (!Identifier.isValid(string) || !Identifier.isHost(string)) throw new InvalidEncodingException("'" + string + "' is not a valid host identifier.");
    }
    
    /**
     * Creates a host identifier with the given string.
     * 
     * @param string the string of the host identifier.
     * 
     * @require Identifier.isValid(string) : "The string is a valid identifier.";
     * @require Identifier.isHost(string) : "The string denotes a host.";
     */
    public HostIdentifier(@Nonnull String string) {
        super(string);
        
        assert Identifier.isHost(string) : "The string denotes a host.";
    }
    
    /**
     * Creates a host identifier from the given block.
     * 
     * @param block the block containing the host identifier.
     * 
     * @require block.getType().isBasedOn(HostIdentity.IDENTIFIER) : "The block is based on the indicated type.";
     */
    public HostIdentifier(@Nonnull Block block) throws InvalidEncodingException {
        this(block, new StringWrapper(block).getString());
    }
    
    /**
     * Creates a host identifier from the given block and string.
     * 
     * @param block the block containing the host identifier.
     * @param string the string of the host identifier.
     * 
     * @require block.getType().isBasedOn(HostIdentity.IDENTIFIER) : "The block is based on the indicated type.";
     */
    HostIdentifier(@Nonnull Block block, @Nonnull String string) throws InvalidEncodingException {
        super(block, string);
        
        if (!Identifier.isHost(string)) throw new InvalidEncodingException("" + this + " is not a host identifier.");
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return HostIdentity.IDENTIFIER;
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentifier getHostIdentifier() {
        return this;
    }
    
    @Pure
    @Override
    public @Nonnull HostIdentity getIdentity() throws SQLException, IOException, PacketException, ExternalException {
        return Mapper.getIdentity(this).toHostIdentity();
    }
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull HostIdentifier get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        return new HostIdentifier(resultSet.getString(columnIndex));
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
