package net.digitalid.core.resolution.handlers;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.value.Invariant;

import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.handler.CoreHandler;
import net.digitalid.core.handler.method.Method;
import net.digitalid.core.handler.reply.QueryReply;
import net.digitalid.core.identification.identity.Category;

/**
 * Replies the identity of the given subject.
 * 
 * @see IdentityQuery
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class IdentityReply extends QueryReply<NonHostEntity> implements CoreHandler<NonHostEntity> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the category of the subject.
     */
    @Pure
    @Invariant(condition = "#.isInternalNonHostIdentity()", message = "The category denotes an internal non-host identity.")
    public abstract @Nonnull Category getCategory();
    
    // TODO: Support predecessors and successor.
    
//    /**
//     * Returns the predecessors of the subject.
//     */
//    @Pure
//    public abstract @Nonnull @Frozen ReadOnlyPredecessors getPredecessors();
//    
//    /**
//     * Returns the successor of the subject.
//     */
//    @Pure
//    public abstract @Nullable InternalNonHostIdentifier getSuccessor();
    
    /* -------------------------------------------------- Constructors -------------------------------------------------- */
    
    // TODO
    
//    /**
//     * Creates a query reply for the identity of given subject.
//     * 
//     * @param subject the subject of this handler.
//     */
//    @NonCommitting
//    IdentityReply(@Nonnull InternalNonHostIdentifier subject) throws DatabaseException, RequestException {
//        super(subject);
//        
//        if (!subject.isMapped()) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The identity with the identifier " + subject + " does not exist on this host."); }
//        this.category = subject.getMappedIdentity().getCategory();
//        if (!category.isInternalNonHostIdentity()) { throw new SQLException("The category is " + category.name() + " instead of an internal non-host identity."); }
//        if (!FreezablePredecessors.exist(subject)) { throw RequestException.get(RequestErrorCode.IDENTIFIER, "The identity with the identifier " + subject + " is not yet initialized."); }
//        this.predecessors = FreezablePredecessors.get(subject);
//        this.successor = Successor.get(subject);
//    }
//    
//    /**
//     * Creates a query reply that decodes a packet with the given signature for the given entity.
//     * 
//     * @param entity the entity to which this handler belongs.
//     * @param signature the host signature of this handler.
//     * @param number the number that references this reply.
//     * @param block the content which is to be decoded.
//     * 
//     * @ensure hasSignature() : "This handler has a signature.";
//     * @ensure !isOnHost() : "Query replies are never decoded on hosts.";
//     */
//    private IdentityReply(@Nullable NonHostEntity entity, @Nonnull HostSignatureWrapper signature, long number, @Nonnull Block block) throws InvalidEncodingException, InternalException {
//        super(entity, signature, number);
//        
//        final @Nonnull TupleWrapper tuple = TupleWrapper.decode(block);
//        this.category = Category.get(tuple.getNonNullableElement(0));
//        if (!category.isInternalNonHostIdentity()) { throw InvalidDeclarationException.get("The category is " + category.name() + " instead of an internal non-host identity.", getSubject(), this); }
//        this.predecessors = new FreezablePredecessors(tuple.getNonNullableElement(1)).freeze();
//        this.successor = tuple.isElementNull(2) ? null : IdentifierImplementation.XDF_CONVERTER.decodeNonNullable(None.OBJECT, tuple.getNonNullableElement(2)).castTo(InternalNonHostIdentifier.class);
//    }
    
    /* -------------------------------------------------- Matching -------------------------------------------------- */
    
    @Pure
    @Override
    public boolean matches(@Nonnull Method<NonHostEntity> method) {
        return method instanceof IdentityQuery;
    }
    
}
