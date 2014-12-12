package ch.virtualid.attribute;

import ch.virtualid.annotations.Pure;
import ch.virtualid.auxiliary.Time;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.external.InvalidEncodingException;
import ch.virtualid.exceptions.external.InvalidSignatureException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.expression.PassiveExpression;
import ch.virtualid.identifier.HostIdentifier;
import ch.virtualid.identifier.InternalIdentifier;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.identity.Category;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.Blockable;
import ch.virtualid.interfaces.Immutable;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import ch.xdf.BooleanWrapper;
import ch.xdf.ClientSignatureWrapper;
import ch.xdf.CredentialsSignatureWrapper;
import ch.xdf.HostSignatureWrapper;
import ch.xdf.ListWrapper;
import ch.xdf.SelfcontainedWrapper;
import ch.xdf.SignatureWrapper;
import ch.xdf.TupleWrapper;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class facilitates the encoding and decoding of attribute values.
 * 
 * @see Attribute
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.7
 */
public final class AttributeValue implements Immutable, Blockable, SQLizable {
    
    /**
     * Stores the semantic type {@code published.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType PUBLISHED = SemanticType.create("published.attribute@virtualid.ch").load(BooleanWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code content.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType CONTENT = SemanticType.create("content.attribute@virtualid.ch").load(SelfcontainedWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code value.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("value.attribute@virtualid.ch").load(SignatureWrapper.TYPE, CONTENT);
    
    /**
     * Stores the semantic type {@code list.attribute@virtualid.ch}.
     */
    public static final @Nonnull SemanticType LIST = SemanticType.create("list.attribute@virtualid.ch").load(ListWrapper.TYPE, TYPE);
    
    
    /**
     * Stores the signature of this attribute value.
     * 
     * @invariant !isCertified() || signature instanceof HostSignatureWrapper : "If this attribute value is certified, it is signed by a host.";
     */
    private final @Nonnull SignatureWrapper signature;
    
    /**
     * Stores the content of this attribute value.
     * 
     * @invariant content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    private final @Nonnull Block content;
    
    /**
     * Creates an uncertified attribute value with the given content.
     * 
     * @param content the content of the uncertified attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public AttributeValue(@Nonnull Block content) {
        assert content.getType().isAttributeType() : "The type of the content denotes an attribute.";
        
        this.signature = new SignatureWrapper(TYPE, new SelfcontainedWrapper(CONTENT, content), null);
        this.content = content;
    }
    
    /**
     * Creates an uncertified attribute value with the given content.
     * 
     * @param content the content of the uncertified attribute value.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public AttributeValue(@Nonnull Blockable content) {
        this(content.toBlock());
    }
    
    /**
     * Creates a certified attribute value with the given content.
     * 
     * @param content the content of the subject which is certified by the issuer.
     * @param subject the identifier of the identity whose content is certified.
     * @param issuer the identifier of the identity which issues the certificate.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * @require Server.hasHost(issuer.getHostIdentifier()) : "The host of the issuer is running on this server.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public AttributeValue(@Nonnull Block content, @Nonnull InternalIdentifier subject, @Nonnull InternalNonHostIdentifier issuer) {
        assert content.getType().isAttributeType() : "The type of the content denotes an attribute.";
        
        this.signature = new HostSignatureWrapper(TYPE, new SelfcontainedWrapper(CONTENT, content), subject, null, issuer);
        this.content = content;
    }
    
    /**
     * Creates a certified attribute value with the given content.
     * 
     * @param content the content of the subject which is certified by the issuer.
     * @param subject the identifier of the identity whose content is certified.
     * @param issuer the identifier of the identity which issues the certificate.
     * 
     * @require content.getType().isAttributeType() : "The type of the content denotes an attribute.";
     * @require Server.hasHost(issuer.getHostIdentifier()) : "The host of the issuer is running on this server.";
     * 
     * @ensure isVerified() : "The signature of this attribute value is verified.";
     */
    public AttributeValue(@Nonnull Blockable content, @Nonnull InternalIdentifier subject, @Nonnull InternalNonHostIdentifier issuer) {
        this(content.toBlock(), subject, issuer);
    }
    
    /**
     * Decodes the given block without verifying the signature.
     * 
     * @param block the block which is to be wrapped and decoded.
     * @param verified whether the signature is already verified.
     * 
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     */
    public AttributeValue(@Nonnull Block block, boolean verified) throws SQLException, IOException, PacketException, ExternalException {
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        this.signature = SignatureWrapper.decodeWithoutVerifying(block, verified, null);
        this.content = new SelfcontainedWrapper(signature.getElementNotNull()).getElement();
        
        if (isCertified() && !(signature instanceof HostSignatureWrapper)) throw new InvalidEncodingException("If this attribute value is certified, it has to be signed by a host.");
        if (isCertified() && ((HostSignatureWrapper) signature).getSigner() instanceof HostIdentifier) throw new InvalidEncodingException("The issuer of a certificate may not be a host.");
    }
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        return signature.toBlock();
    }
    
    
    /**
     * Returns the signature of this attribute value.
     * 
     * @return the signature of this attribute value.
     * 
     * @ensure !isCertified() || return instanceof HostSignatureWrapper : "If this attribute value is certified, it is signed by a host.";
     */
    @Pure
    public @Nonnull SignatureWrapper getSignature() {
        return signature;
    }
    
    /**
     * Returns the content of this attribute value.
     * 
     * @return the content of this attribute value.
     * 
     * @ensure return.getType().isAttributeType() : "The type of the content denotes an attribute.";
     */
    @Pure
    public @Nonnull Block getContent() {
        return content;
    }
    
    
    /**
     * Returns whether this attribute value is certified.
     * 
     * @return whether this attribute value is certified.
     */
    @Pure
    public boolean isCertified() {
        return signature.isSigned();
    }
    
    /**
     * Returns the time when this attribute value was certified.
     * 
     * @return the time when this attribute value was certified.
     * 
     * @require isCertified() : "This attribute value is certified.";
     */
    @Pure
    public @Nonnull Time getTime() {
        assert isCertified() : "This attribute value is certified.";
        
        return signature.getTimeNotNull();
    }
    
    /**
     * Returns the subject of this attribute value's certificate.
     * 
     * @return the subject of this attribute value's certificate.
     * 
     * @require isCertified() : "This attribute value is certified.";
     */
    @Pure
    public @Nonnull InternalIdentifier getSubject() {
        assert isCertified() : "This attribute value is certified.";
        
        return signature.getSubjectNotNull();
    }
    
    /**
     * Returns the issuer of this attribute value's certificate.
     * 
     * @return the issuer of this attribute value's certificate.
     * 
     * @require isCertified() : "This attribute value is certified.";
     */
    @Pure
    public @Nonnull InternalNonHostIdentifier getIssuer() {
        assert isCertified() : "This attribute value is certified.";
        
        return (InternalNonHostIdentifier) ((HostSignatureWrapper) signature).getSigner();
    }
    
    
    /**
     * Returns whether the signature of this attribute value is verified.
     * 
     * @return whether the signature of this attribute value is verified.
     */
    @Pure
    public boolean isVerified() {
        return signature.isVerified();
    }
    
    /**
     * Returns whether the signature of this attribute value is not verified.
     * 
     * @return whether the signature of this attribute value is not verified.
     */
    @Pure
    public boolean isNotVerified() {
        return signature.isNotVerified();
    }
    
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
    public void verify() throws SQLException, IOException, PacketException, ExternalException {
        if (signature instanceof ClientSignatureWrapper || signature instanceof CredentialsSignatureWrapper) throw new InvalidSignatureException("Attribute values can only be certified by hosts.");
        signature.verify();
        if (signature instanceof HostSignatureWrapper) {
            // TODO: Check that the signer has the corresponding delegation for the given attribute and value.
        }
    }
    
    
    /**
     * Stores the semantic type {@code delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType DELEGATION = SemanticType.create("delegation@virtualid.ch").load(TupleWrapper.TYPE, NonHostIdentity.IDENTIFIER, PassiveExpression.TYPE);
    
    /**
     * Stores the semantic type {@code list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType DELEGATIONS = SemanticType.create("list.delegation@virtualid.ch").load(ListWrapper.TYPE, DELEGATION);
    
    /**
     * Stores the semantic type {@code outgoing.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType OUTGOING_DELEGATIONS = SemanticType.create("outgoing.list.delegation@virtualid.ch").load(new Category[] {Category.SEMANTIC_TYPE, Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Stores the semantic type {@code incoming.list.delegation@virtualid.ch}.
     */
    public static final @Nonnull SemanticType INCOMING_DELEGATIONS = SemanticType.create("incoming.list.delegation@virtualid.ch").load(new Category[] {Category.NATURAL_PERSON, Category.ARTIFICIAL_PERSON}, Time.TROPICAL_YEAR, DELEGATIONS);
    
    /**
     * Determines whether the given VID is authorized to certify the given element.
     * 
     * @param identifier the identifier of the certifying VID.
     * @param value the certified value as a selfcontained block.
     * @return {@code true} if the given VID is authorized to certify the given element, {@code false} otherwise.
     * @require identifier != null : "The identifier is not null.";
     * @require value != null : "The value is not null.";
     */
//    @Deprecated
//    private static boolean isAuthorized(String identifier, Block value) throws Exception {
//        assert identifier != null : "The identifier is not null.";
//        assert value != null : "The value is not null.";
//        
//        long vid = Mapper.getVid(identifier);
//        long type = Mapper.getVid(new SelfcontainedWrapper(value).getIdentifier());
//        
//        if (vid == type) return true;
//        
//        // Load the certification delegations of the VID and recurse for each delegation that matches the type and the value.
//        long time = System.currentTimeMillis() + getCachingPeriod(Vid.INCOMING_DELEGATIONS) - getCachingPeriod(type);
//        Block attribute = getAttribute(vid, Vid.INCOMING_DELEGATIONS, time);
//        if (attribute == null) return false;
//        
//        List<Block> incoming_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//        for (Block incoming_delegation : incoming_delegations) {
//            Block[] elements = new TupleWrapper(incoming_delegation).getElementsNotNull(3);
//            if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type) {
//                String restriction = new StringWrapper(elements[2]).getString();
//                Expression expression = Expression.parse(restriction);
//                if (expression.matches(value)) {
//                    // Check that the delegating VID references the current VID with the same type and expression.
//                    identifier = new StringWrapper(elements[1]).getString();
//                    attribute = getAttribute(Mapper.getVid(identifier), Vid.OUTGOING_DELEGATIONS, time);
//                    if (attribute == null) continue;
//                    List<Block> outgoing_delegations = new ListWrapper(new SelfcontainedWrapper(new SignatureWrapper(attribute, false).getElement()).getElement()).getElements();
//                    for (Block outgoing_delegation : outgoing_delegations) {
//                        elements = new TupleWrapper(outgoing_delegation).getElementsNotNull(3);
//                        if (Mapper.getVid(new StringWrapper(elements[0]).getString()) == type && Mapper.getVid(new StringWrapper(elements[1]).getString()) == vid && new StringWrapper(elements[2]).getString().equalsIgnoreCase(restriction)) {
//                            if (isAuthorized(identifier, value)) return true;
//                        }
//                    }
//                }
//            }
//        }
//        
//        return false;
//    }
    
    
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
    public static @Nonnull AttributeValue get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            return new AttributeValue(Block.get(AttributeValue.TYPE, resultSet, columnIndex), true);
        } catch (@Nonnull IOException | PacketException | ExternalException exception) {
            throw new SQLException("The attribute value returned by the database is invalid.", exception);
        }
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        signature.toBlock().set(preparedStatement, parameterIndex);
    }
    
    /**
     * Sets the parameter at the given index of the prepared statement to the given attribute value.
     * 
     * @param attributeValue the attribute value to which the parameter at the given index is to be set.
     * @param preparedStatement the prepared statement whose parameter is to be set.
     * @param parameterIndex the index of the parameter to set.
     */
    public static void set(@Nullable AttributeValue attributeValue, @Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        Block.set(Block.toBlock(attributeValue), preparedStatement, parameterIndex);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) return true;
        if (object == null || !(object instanceof AttributeValue)) return false;
        final @Nonnull AttributeValue other = (AttributeValue) object;
        return this.signature.equals(other.signature);
    }
    
    @Pure
    @Override
    public int hashCode() {
        return signature.hashCode();
    }
    
    @Pure
    @Override
    public @Nonnull String toString() {
        return "Attribute value of type " + content.getType().getAddress() + (isCertified() ? " certified by " + getIssuer() + " for " + getSubject() : "");
    }
    
}
