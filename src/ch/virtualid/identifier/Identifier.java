package ch.virtualid.identifier;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identity.Identity;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This interface models identifiers.
 * 
 * @see IdentifierClass
 * @see NonHostIdentifier
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.0
 */
public interface Identifier extends Immutable, Blockable, SQLizable {
    
    /**
     * Returns the string of this identifier.
     * 
     * @return the string of this identifier.
     * 
     * @ensure IdentifierClass.isValid(string) : "The returned string is a valid identifier.";
     */
    @Pure
    public @Nonnull String getString();
    
    
    /**
     * Returns whether this identifier is mapped.
     * 
     * @return whether this identifier is mapped.
     */
    @Pure
    public boolean isMapped() throws SQLException;
    
    /**
     * Returns whether this identifier is not mapped.
     * 
     * @return whether this identifier is not mapped.
     */
    @Pure
    public boolean isNotMapped() throws SQLException;
    
    /**
     * Returns the mapped identity of this identifier.
     * 
     * @return the mapped identity of this identifier.
     * 
     * @require isMapped() : "This identifier is mapped.";
     */
    @Pure
    public @Nonnull Identity getMappedIdentity() throws SQLException;
    
    /**
     * Returns the identity of this identifier.
     * 
     * @return the identity of this identifier.
     * 
     * @ensure !(result instanceof Type) || ((Type) result).isLoaded() : "If the result is a type, its declaration is loaded.";
     */
    @Pure
    public @Nonnull Identity getIdentity() throws SQLException, IOException, PacketException, ExternalException;
    
    
    /**
     * Returns this identifier as a {@link NonHostIdentifier}.
     * 
     * @return this identifier as a {@link NonHostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link NonHostIdentifier}.
     */
    @Pure
    public @Nonnull NonHostIdentifier toNonHostIdentifier() throws InvalidEncodingException;
    
    
    /**
     * Returns this identifier as an {@link InternalIdentifier}.
     * 
     * @return this identifier as an {@link InternalIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link InternalIdentifier}.
     */
    @Pure
    public @Nonnull InternalIdentifier toInternalIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as a {@link HostIdentifier}.
     * 
     * @return this identifier as a {@link HostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link HostIdentifier}.
     */
    @Pure
    public @Nonnull HostIdentifier toHostIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as a {@link InternalNonHostIdentifier}.
     * 
     * @return this identifier as a {@link InternalNonHostIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link InternalNonHostIdentifier}.
     */
    @Pure
    public @Nonnull InternalNonHostIdentifier toInternalNonHostIdentifier() throws InvalidEncodingException;
    
    
    /**
     * Returns this identifier as an {@link ExternalIdentifier}.
     * 
     * @return this identifier as an {@link ExternalIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link ExternalIdentifier}.
     */
    @Pure
    public @Nonnull ExternalIdentifier toExternalIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as an {@link EmailIdentifier}.
     * 
     * @return this identifier as an {@link EmailIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link EmailIdentifier}.
     */
    @Pure
    public @Nonnull EmailIdentifier toEmailIdentifier() throws InvalidEncodingException;
    
    /**
     * Returns this identifier as a {@link MobileIdentifier}.
     * 
     * @return this identifier as a {@link MobileIdentifier}.
     * 
     * @throws InvalidEncodingException if this identifier is not an instance of {@link MobileIdentifier}.
     */
    @Pure
    public @Nonnull MobileIdentifier toMobileIdentifier() throws InvalidEncodingException;
    
}
