package ch.virtualid.handler.action.internal;

import ch.virtualid.agent.Restrictions;
import ch.virtualid.annotations.Pure;
import ch.virtualid.concepts.Password;
import ch.virtualid.entity.Entity;
import ch.virtualid.entity.Role;
import ch.virtualid.exceptions.InvalidDeclarationException;
import ch.virtualid.handler.Method;
import ch.virtualid.identity.FailedIdentityException;
import ch.virtualid.identity.HostIdentifier;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.util.FreezableArray;
import ch.virtualid.util.ReadonlyArray;
import ch.xdf.Block;
import ch.xdf.SignatureWrapper;
import ch.xdf.StringWrapper;
import ch.xdf.TupleWrapper;
import ch.xdf.exceptions.InvalidEncodingException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Replaces the value of a {@link Password password}.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class PasswordValueReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.password@virtualid.ch}.
     */
    private static final @Nonnull SemanticType OLD_VALUE = SemanticType.create("old.password@virtualid.ch").load(Password.TYPE);
    
    /**
     * Stores the semantic type {@code new.password@virtualid.ch}.
     */
    private static final @Nonnull SemanticType NEW_VALUE = SemanticType.create("new.password@virtualid.ch").load(Password.TYPE);
    
    /**
     * Stores the semantic type {@code replace.password@virtualid.ch}.
     */
    public static final @Nonnull SemanticType TYPE = SemanticType.create("replace.password@virtualid.ch").load(TupleWrapper.TYPE, OLD_VALUE, NEW_VALUE);
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    
    /**
     * Stores the password whose value is to be replaced.
     */
    private final @Nonnull Password password;
    
    /**
     * Stores the old value of the password.
     * 
     * @invariant Password.isValid(oldValue) : "The old value is valid.";
     */
    private final @Nonnull String oldValue;
    
    /**
     * Stores the new value of the password.
     * 
     * @invariant Password.isValid(newValue) : "The new value is valid.";
     */
    private final @Nonnull String newValue;
    
    /**
     * Creates an internal action to replace the value of the given password.
     * 
     * @param role the role to which this handler belongs.
     * 
     * @require Password.isOnClient() : "The password is on a client.";
     * @require Password.isValid(oldValue) : "The old value is valid.";
     * @require Password.isValid(newValue) : "The new value is valid.";
     */
    public PasswordValueReplace(@Nonnull Password password, @Nonnull String oldValue, @Nonnull String newValue) {
        super((Role) password.getEntityNotNull());
        
        assert Password.isValid(oldValue) : "The old value is valid.";
        assert Password.isValid(newValue) : "The new value is valid.";
        
        this.password = password;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    /**
     * Creates an internal action that decodes the given block.
     * 
     * @param entity the entity to which this handler belongs.
     * @param signature the signature of this handler (or a dummy that just contains a subject).
     * @param recipient the recipient of this method.
     * @param block the content which is to be decoded.
     * 
     * @require signature.getSubject() != null : "The subject of the signature is not null.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure getSignature() != null : "The signature of this handler is not null.";
     */
    public PasswordValueReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
        super(entity, signature, recipient);
        
        assert block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
        
        final @Nonnull ReadonlyArray<Block> elements = new TupleWrapper(block).getElementsNotNull(2);
        this.password = Password.get(entity);
        this.oldValue = new StringWrapper(elements.getNotNull(0)).getString();
        this.newValue = new StringWrapper(elements.getNotNull(1)).getString();
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = new FreezableArray<Block>(2);
        elements.set(0, new StringWrapper(OLD_VALUE, oldValue).toBlock());
        elements.set(1, new StringWrapper(NEW_VALUE, newValue).toBlock());
        return new TupleWrapper(TYPE, elements.freeze()).toBlock();
    }
    
    
    /**
     * Stores the required restrictions for this internal method.
     */
    private static final @Nonnull Restrictions requiredRestrictions = new Restrictions(true, false, true);
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictions() {
        return requiredRestrictions;
    }
    
    /**
     * Stores the audit restrictions for this action.
     */
    private static final @Nonnull Restrictions auditRestrictions = new Restrictions(true, false, false);
    
    @Pure
    @Override
    public @Nonnull Restrictions getAuditRestrictions() {
        return auditRestrictions;
    }
    
    
    @Override
    protected void executeOnBoth() throws SQLException {
        password.replaceName(oldValue, newValue);
    }
    
    
    @Pure
    @Override
    public @Nonnull PasswordValueReplace getReverse() {
        assert isOnClient() : "This method is called on a client.";
        
        return new PasswordValueReplace(password, newValue, oldValue);
    }
    
    
    /**
     * The factory class for the surrounding method.
     */
    protected static final class Factory extends Method.Factory {
        
        static { Method.add(new Factory()); }
        
        @Pure
        @Override
        public @Nonnull SemanticType getType() {
            return TYPE;
        }
        
        @Pure
        @Override
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws InvalidEncodingException, SQLException, FailedIdentityException, InvalidDeclarationException {
            return new PasswordValueReplace(entity, signature, recipient, block);
        }
        
    }
    
}
