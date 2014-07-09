package ch.virtualid.agent;

import ch.virtualid.annotation.Pure;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.NonHostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.util.FreezableLinkedHashSet;
import ch.xdf.Block;
import ch.xdf.ListWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.1
 */
public final class Authentications extends FreezableLinkedHashSet<SemanticType> implements ReadonlyAuthentications {
    
    /**
     * Stores the semantic type {@code identity.based.authentication@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTITY_BASED_AUTHENTICATION = mapSemanticType(NonHostIdentifier.IDENTITY_BASED_AUTHENTICATION);    
    
    /**
     * Stores an empty set of authentications.
     */
    public static final @Nonnull Authentications NONE = new Authentications();
    
    /**
     * Stores an identity-based authentication.
     */
    public static final @Nonnull Authentications IDENTITY_BASED = new Authentications(SemanticType.IDENTITY_BASED_AUTHENTICATION);
    
    /**
     * Creates an empty set of authentications.
     */
    public Authentications() {}
    
    /**
     * Creates new authentications with the given semantic type.
     * 
     * @param type the semantic type used for authentication.
     */
    public Authentications(@Nonnull SemanticType type) {
        add(type);
    }
    
    /**
     * Creates new authentications from the given block.
     * 
     * @param block the block containing the authentications.
     */
    public Authentications(@Nonnull Block block) throws InvalidEncodingException, FailedIdentityException {
        @Nonnull List<Block> elements = new ListWrapper(block).getElements();
        for (@Nonnull Block element : elements) {
            @Nonnull NonHostIdentifier identifier = new NonHostIdentifier(element);
            @Nonnull SemanticType type = identifier.getIdentity().toSemanticType();
            add(type);
        }
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        @Nonnull List<Block> elements = new LinkedList<Block>();
        for (@Nonnull SemanticType type : this) {
            elements.add(type.getAddress().toBlock());
        }
        return new ListWrapper(elements).toBlock();
    }
    
    
    @Override
    public @Nonnull ReadonlyAuthentications freeze() {
        super.freeze();
        return this;
    }
    
    
    @Override
    public @Nonnull String toString() {
        @Nonnull StringBuilder string = new StringBuilder("[");
        for (@Nonnull SemanticType type : this) {
            if (string.length() != 1) string.append(", ");
            string.append(type.getAddress().getString());
        }
        string.append("]");
        return string.toString();
    }
    
    
}
