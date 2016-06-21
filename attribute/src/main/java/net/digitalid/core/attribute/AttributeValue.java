package net.digitalid.core.attribute;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.system.castable.Castable;
import net.digitalid.utility.system.castable.CastableObject;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.core.converter.sql.SQL;
import net.digitalid.database.core.exceptions.DatabaseException;

import net.digitalid.core.conversion.Block;
import net.digitalid.core.conversion.exceptions.InvalidReplyParameterValueException;
import net.digitalid.core.conversion.wrappers.SelfcontainedWrapper;
import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;
import net.digitalid.core.conversion.wrappers.structure.ListWrapper;
import net.digitalid.core.conversion.xdf.XDF;
import net.digitalid.core.cryptography.signature.exceptions.InvalidSignatureException;
import net.digitalid.core.packet.exceptions.NetworkException;
import net.digitalid.core.packet.exceptions.RequestException;
import net.digitalid.core.identity.SemanticType;
import net.digitalid.core.identity.annotations.AttributeType;

import net.digitalid.service.core.exceptions.external.encoding.InvalidParameterValueCombinationException;

/**
 * This class facilitates the encoding and decoding of attribute values.
 * 
 * @see Attribute
 * @see CertifiedAttributeValue
 * @see UncertifiedAttributeValue
 */
@Immutable
public abstract class AttributeValue extends CastableObject implements Castable, XDF<AttributeValue, Boolean>, SQL<AttributeValue, Object> {
    
    /**
     * Stores the semantic type {@code content.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType CONTENT = SemanticType.map("content.attribute@core.digitalid.net").load(SelfcontainedWrapper.XDF_TYPE);
    
    /**
     * Stores the semantic type {@code value.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.map("value.attribute@core.digitalid.net").load(SignatureWrapper.XDF_TYPE, CONTENT);
    
    /**
     * Stores the semantic type {@code list.attribute@core.digitalid.net}.
     */
    public static final @Nonnull SemanticType LIST = SemanticType.map("list.attribute@core.digitalid.net").load(ListWrapper.XDF_TYPE, TYPE);
    
    
    /**
     * Stores the content of this attribute value.
     * 
     * @invariant content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    private final @Nonnull Block content;
    
    /**
     * Creates an attribute value with the given content.
     * 
     * @param content the content of the attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    AttributeValue(@Nonnull Block content) {
        Require.that(content.getType().isAttributeType()).orThrow("The type of the content denotes an attribute.");
        
        this.content = content;
    }
    
    /**
     * Decodes the given block without verifying the signature.
     * 
     * @param block the block which is to be wrapped and decoded.
     * @param verified whether the signature is already verified.
     * 
     * @return the attribute value with the content given by the block.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    @Pure
    @NonCommitting
    public static @Nonnull AttributeValue get(@Nonnull Block block, boolean verified) throws ExternalException {
        Require.that(block.getType().isBasedOn(TYPE)).orThrow("The block is based on the indicated type.");
        
        final @Nonnull SignatureWrapper signature = SignatureWrapper.decodeWithoutVerifying(block, verified, null);
        final @Nonnull Block content = SelfcontainedWrapper.decodeNonNullable(signature.getNonNullableElement());
        content.getType().checkIsAttributeType();
        
        if (signature.isSigned()) { return new CertifiedAttributeValue(content, signature); }
        else { return new UncertifiedAttributeValue(content, signature); }
    }
    
    @Pure
    @Override
    public final @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public final @Nonnull Block toBlock() {
        return new Block(TYPE, getSignature().toBlock());
    }
    
    
    /**
     * Returns the content of this attribute value.
     * 
     * @return the content of this attribute value.
     * 
     * @ensure return.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    @Pure
    public final @Nonnull Block getContent() {
        return content;
    }
    
    /**
     * Returns whether this attribute value is certified.
     * 
     * @return whether this attribute value is certified.
     */
    @Pure
    public final boolean isCertified() {
        return this instanceof CertifiedAttributeValue;
    }
    
    
    /**
     * Returns whether this value matches the given attribute.
     * 
     * @param attribute the attribute which needs to be matched.
     * 
     * @return whether this value matches the given attribute.
     */
    @Pure
    public boolean matches(@Nonnull Attribute attribute) {
        return content.getType().equals(attribute.getType());
    }
    
    /**
     * Checks that this value matches the given attribute.
     * 
     * @param attribute the attribute which needs to be matched.
     * 
     * @return this attribute value.
     * 
     * @throws InvalidParameterValueCombinationException otherwise.
     */
    @Pure
    public final @Nonnull AttributeValue checkMatches(@Nonnull Attribute attribute) throws InvalidParameterValueCombinationException {
        if (!matches(attribute)) { throw InvalidParameterValueCombinationException.get("This value does not match the given attribute."); }
        return this;
    }
    
    /**
     * Checks that the content of this value matches the given type.
     * 
     * @param type the type which needs to be matched by the content.
     * 
     * @return this attribute value.
     * 
     * @throws InvalidReplyParameterValueException otherwise.
     * 
     * @ensure getContent().getType().equals(type) : "The content matches the given type.";
     */
    @Pure
    public final @Nonnull AttributeValue checkContentType(@Nonnull @AttributeType SemanticType type) throws InvalidReplyParameterValueException {
        Require.that(type.isAttributeType()).orThrow("The type is an attribute type.");
        
        if (!content.getType().equals(type)) { throw InvalidReplyParameterValueException.get(null, "content type", type.getAddress(), content.getType().getAddress()); }
        return this;
    }
    
    
    /**
     * Returns the signature of this attribute value.
     * 
     * @return the signature of this attribute value.
     */
    @Pure
    public abstract @Nonnull SignatureWrapper getSignature();
    
    /**
     * Verifies the signature of this attribute value.
     * 
     * @throws InvalidSignatureException if the signature is not valid.
     * 
     * @require isNotVerified() : "The signature of this attribute value is not verified.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    @Pure
    @NonCommitting
    public abstract void verify() throws ExternalException;
    
    /**
     * Returns whether the signature of this attribute value is verified.
     * 
     * @return whether the signature of this attribute value is verified.
     */
    @Pure
    public final boolean isVerified() {
        return getSignature().isVerified();
    }
    
    
    /**
     * Stores the data type used to store instances of this class in the database.
     */
    public static final @Nonnull String FORMAT = Block.FORMAT;
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    @NonCommitting
    public static @Nonnull AttributeValue get(@NonCapturable @Nonnull SelectionResult result) throws DatabaseException {
        try {
            return AttributeValue.get(Block.getNotNull(TYPE, resultSet, columnIndex), true);
        } catch (@Nonnull IOException | RequestException | ExternalException exception) {
            throw new SQLException("The attribute value returned by the database is invalid.", exception);
        }
    }
    
    @Override
    @NonCommitting
    public final void set(@NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        toBlock().set(preparedStatement, parameterIndex);
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given attribute value.
     * 
     * @param attributeValue the attribute value to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    @NonCommitting
    public static void set(@Nullable AttributeValue attributeValue, @NonCapturable @Nonnull ValueCollector collector) throws DatabaseException {
        Block.set(Block.toBlock(attributeValue), preparedStatement, parameterIndex);
    }
    
    
    @Pure
    @Override
    public final boolean equals(@Nullable Object object) {
        if (object == this) { return true; }
        if (object == null || !(object instanceof AttributeValue)) { return false; }
        final @Nonnull AttributeValue other = (AttributeValue) object;
        return this.toBlock().equals(other.toBlock());
    }
    
    @Pure
    @Override
    public final int hashCode() {
        return getSignature().hashCode();
    }
    
}
