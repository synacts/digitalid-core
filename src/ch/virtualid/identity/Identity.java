package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.Identifier;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This interface models a virtual identity.
 * 
 * @see IdentityClass
 * @see InternalIdentity
 * @see ExternalIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public interface Identity extends Immutable, SQLizable {
    
    /**
     * Stores the semantic type {@code @virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SyntacticType.IDENTITY_IDENTIFIER;
    
    
    /**
     * Returns the number of this identity.
     * 
     * @return the number of this identity.
     */
    @Pure
    public long getNumber();
    
    /**
     * Returns the address of this identity.
     * 
     * @return the address of this identity.
     */
    @Pure
    public @Nonnull Identifier getAddress();
    
    /**
     * Returns the category of this identity.
     * 
     * @return the category of this identity.
     */
    @Pure
    public @Nonnull Category getCategory();
    
    /**
     * Returns whether this identity has been merged and updates the internal number and the identifier.
     * 
     * @return whether this identity has been merged.
     */
    public boolean hasBeenMerged() throws SQLException, IOException, PacketException, ExternalException;
    
    
    /**
     * Returns this identity as an {@link InternalIdentity} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as an {@link InternalIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalIdentity}.
     */
    @Pure
    public @Nonnull InternalIdentity toInternalIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link ExternalIdentity} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as an {@link ExternalIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ExternalIdentity}.
     */
    @Pure
    public @Nonnull ExternalIdentity toExternalIdentity() throws InvalidEncodingException;
    
    
    /**
     * Returns this identity as a {@link HostIdentity} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link HostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link HostIdentity}.
     */
    @Pure
    public @Nonnull HostIdentity toHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link NonHostIdentity} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link NonHostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NonHostIdentity}.
     */
    @Pure
    public @Nonnull NonHostIdentity toNonHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link InternalNonHostIdentity} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as an {@link InternalNonHostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalNonHostIdentity}.
     */
    @Pure
    public @Nonnull InternalNonHostIdentity toInternalNonHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link Type} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link Type}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Type}.
     */
    @Pure
    public @Nonnull Type toType() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link SyntacticType} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link SyntacticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SyntacticType}.
     */
    @Pure
    public @Nonnull SyntacticType toSyntacticType() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link SemanticType} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link SemanticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SemanticType}.
     */
    @Pure
    public @Nonnull SemanticType toSemanticType() throws InvalidEncodingException;
    
    
    /**
     * Returns this identity as a {@link Person} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link Person}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Person}.
     */
    @Pure
    public @Nonnull Person toPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link InternalPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as an {@link InternalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalPerson}.
     */
    @Pure
    public @Nonnull InternalPerson toInternalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link NaturalPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link NaturalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NaturalPerson}.
     */
    @Pure
    public @Nonnull NaturalPerson toNaturalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link ArtificialPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link ArtificialPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ArtificialPerson}.
     */
    @Pure
    public @Nonnull ArtificialPerson toArtificialPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link ExternalPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as an {@link ExternalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ExternalPerson}.
     */
    @Pure
    public @Nonnull ExternalPerson toExternalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link EmailPerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as an {@link EmailPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link EmailPerson}.
     */
    @Pure
    public @Nonnull EmailPerson toEmailPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link MobilePerson} or throws an {@link InvalidEncodingException} if it is not an instance thereof.
     * 
     * @return this identity as a {@link MobilePerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link MobilePerson}.
     */
    @Pure
    public @Nonnull MobilePerson toMobilePerson() throws InvalidEncodingException;
    
}
