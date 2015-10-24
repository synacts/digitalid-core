package net.digitalid.service.core.identity;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.identifier.Identifier;
import net.digitalid.utility.database.storing.Storable;
import net.digitalid.service.core.wrappers.Block;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.database.annotations.NonCommitting;

/**
 * This interface models a digital identity.
 * 
 * @see IdentityClass
 * @see InternalIdentity
 * @see ExternalIdentity
 * 
 * @author Kaspar Etter (kaspar.etter@digitalid.net)
 * @version 1.0.0
 */
@Immutable
public interface Identity extends Storable<Identity, Object> {
    
    /**
     * Stores the semantic type {@code @core.digitalid.net}.
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
     * @param exception the exception to be rethrown if this identity has not been merged.
     * 
     * @return whether this identity has been merged.
     */
    @NonCommitting
    public boolean hasBeenMerged(@Nonnull SQLException exception) throws SQLException;
    
    
    /**
     * Returns the address of this identity as a block of the given type.
     * 
     * @param type the semantic type of the block which is to be returned.
     * 
     * @return the address of this identity as a block of the given type.
     * 
     * @require type.isBasedOn(Identity.IDENTIFIER) : "The type is based on an identifier.";
     * 
     * @ensure return.getType().equals(type) : "The returned block has the given type.";
     */
    public @Nonnull Block toBlock(@Nonnull SemanticType type);
    
    /**
     * Returns the address of this identity as a blockable of the given type.
     * 
     * @param type the semantic type of the blockable which is to be returned.
     * 
     * @return the address of this identity as a blockable of the given type.
     * 
     * @require type.isBasedOn(Identity.IDENTIFIER) : "The type is based on an identifier.";
     * 
     * @ensure return.getType().equals(type) : "The returned blockable has the given type.";
     */
    public @Nonnull Blockable toBlockable(@Nonnull SemanticType type);
    
    
    /**
     * Returns this identity as an {@link InternalIdentity}.
     * 
     * @return this identity as an {@link InternalIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalIdentity}.
     */
    @Pure
    public @Nonnull InternalIdentity toInternalIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link ExternalIdentity}.
     * 
     * @return this identity as an {@link ExternalIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ExternalIdentity}.
     */
    @Pure
    public @Nonnull ExternalIdentity toExternalIdentity() throws InvalidEncodingException;
    
    
    /**
     * Returns this identity as a {@link HostIdentity}.
     * 
     * @return this identity as a {@link HostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link HostIdentity}.
     */
    @Pure
    public @Nonnull HostIdentity toHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link NonHostIdentity}.
     * 
     * @return this identity as a {@link NonHostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NonHostIdentity}.
     */
    @Pure
    public @Nonnull NonHostIdentity toNonHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link InternalNonHostIdentity}.
     * 
     * @return this identity as an {@link InternalNonHostIdentity}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalNonHostIdentity}.
     */
    @Pure
    public @Nonnull InternalNonHostIdentity toInternalNonHostIdentity() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link Type}.
     * 
     * @return this identity as a {@link Type}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Type}.
     */
    @Pure
    public @Nonnull Type toType() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link SyntacticType}.
     * 
     * @return this identity as a {@link SyntacticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SyntacticType}.
     */
    @Pure
    public @Nonnull SyntacticType toSyntacticType() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link SemanticType}.
     * 
     * @return this identity as a {@link SemanticType}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link SemanticType}.
     */
    @Pure
    public @Nonnull SemanticType toSemanticType() throws InvalidEncodingException;
    
    
    /**
     * Returns this identity as a {@link Person}.
     * 
     * @return this identity as a {@link Person}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link Person}.
     */
    @Pure
    public @Nonnull Person toPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link InternalPerson}.
     * 
     * @return this identity as an {@link InternalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link InternalPerson}.
     */
    @Pure
    public @Nonnull InternalPerson toInternalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link NaturalPerson}.
     * 
     * @return this identity as a {@link NaturalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link NaturalPerson}.
     */
    @Pure
    public @Nonnull NaturalPerson toNaturalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link ArtificialPerson}.
     * 
     * @return this identity as a {@link ArtificialPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ArtificialPerson}.
     */
    @Pure
    public @Nonnull ArtificialPerson toArtificialPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link ExternalPerson}.
     * 
     * @return this identity as an {@link ExternalPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link ExternalPerson}.
     */
    @Pure
    public @Nonnull ExternalPerson toExternalPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as an {@link EmailPerson}.
     * 
     * @return this identity as an {@link EmailPerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link EmailPerson}.
     */
    @Pure
    public @Nonnull EmailPerson toEmailPerson() throws InvalidEncodingException;
    
    /**
     * Returns this identity as a {@link MobilePerson}.
     * 
     * @return this identity as a {@link MobilePerson}.
     * 
     * @throws InvalidEncodingException if this identity is not an instance of {@link MobilePerson}.
     */
    @Pure
    public @Nonnull MobilePerson toMobilePerson() throws InvalidEncodingException;
    
}
