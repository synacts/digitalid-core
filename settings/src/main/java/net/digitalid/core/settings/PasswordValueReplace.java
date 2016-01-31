package net.digitalid.core.settings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.collections.freezable.FreezableArray;
import net.digitalid.utility.collections.readonly.ReadOnlyArray;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.validation.annotations.type.Immutable;
import net.digitalid.utility.validation.annotations.method.Pure;

import net.digitalid.database.core.annotations.NonCommitting;

import net.digitalid.core.conversion.Block;

import net.digitalid.core.conversion.wrappers.signature.SignatureWrapper;

import net.digitalid.core.conversion.wrappers.structure.TupleWrapper;

import net.digitalid.core.conversion.wrappers.value.string.StringWrapper;

import net.digitalid.core.agent.Restrictions;

import net.digitalid.service.core.dataservice.StateModule;
import net.digitalid.service.core.entity.Entity;

import net.digitalid.core.exceptions.RequestException;

import net.digitalid.service.core.handler.Action;
import net.digitalid.service.core.handler.Method;
import net.digitalid.service.core.handler.core.CoreServiceInternalAction;
import net.digitalid.service.core.identifier.HostIdentifier;
import net.digitalid.service.core.identity.SemanticType;

/**
 * Replaces the value of a {@link Settings password}.
 */
@Immutable
final class PasswordValueReplace extends CoreServiceInternalAction {
    
    /**
     * Stores the semantic type {@code old.password@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType OLD_VALUE = SemanticType.map("old.password@core.digitalid.net").load(Settings.TYPE);
    
    /**
     * Stores the semantic type {@code new.password@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType NEW_VALUE = SemanticType.map("new.password@core.digitalid.net").load(Settings.TYPE);
    
    /**
     * Stores the semantic type {@code replace.password@core.digitalid.net}.
     */
    private static final @Nonnull SemanticType TYPE = SemanticType.map("replace.password@core.digitalid.net").load(TupleWrapper.XDF_TYPE, OLD_VALUE, NEW_VALUE);
    
    
    /**
     * Stores the password whose value is to be replaced.
     */
    private final @Nonnull Settings password;
    
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
     * @param password the password whose value is to be replaced.
     * @param oldValue the old value of the password.
     * @param newValue the new value of the password.
     * 
     * @require password.isOnClient() : "The password is on a client.";
     * @require Password.isValid(oldValue) : "The old value is valid.";
     * @require Password.isValid(newValue) : "The new value is valid.";
     */
    PasswordValueReplace(@Nonnull Settings password, @Nonnull String oldValue, @Nonnull String newValue) {
        super(password.getRole());
        
        assert Settings.isValid(oldValue) : "The old value is valid.";
        assert Settings.isValid(newValue) : "The new value is valid.";
        
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
     * @require signature.hasSubject() : "The signature has a subject.";
     * @require block.getType().isBasedOn(TYPE) : "The block is based on the indicated type.";
     * 
     * @ensure hasSignature() : "This handler has a signature.";
     */
    @NonCommitting
    private PasswordValueReplace(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
        super(entity, signature, recipient);
        
        final @Nonnull ReadOnlyArray<Block> elements = TupleWrapper.decode(block).getNonNullableElements(2);
        this.password = Settings.get(entity.castTo(NonHostEntity.class));
        this.oldValue = StringWrapper.decodeNonNullable(elements.getNonNullable(0));
        this.newValue = StringWrapper.decodeNonNullable(elements.getNonNullable(1));
    }
    
    @Pure
    @Override
    public @Nonnull Block toBlock() {
        final @Nonnull FreezableArray<Block> elements = FreezableArray.get(2);
        elements.set(0, StringWrapper.encodeNonNullable(OLD_VALUE, oldValue));
        elements.set(1, StringWrapper.encodeNonNullable(NEW_VALUE, newValue));
        return TupleWrapper.encode(TYPE, elements.freeze());
    }
    
    @Pure
    @Override
    public @Nonnull String getDescription() {
        return "Replaces the password '" + oldValue + "' with '" + newValue + "'.";
    }
    
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToExecuteMethod() {
        return new Restrictions(true, false, true);
    }
    
    @Pure
    @Override
    public @Nonnull Restrictions getRequiredRestrictionsToSeeAudit() {
        return new Restrictions(true, false, false);
    }
    
    
    @Override
    @NonCommitting
    protected void executeOnBoth() throws DatabaseException {
        password.replaceName(oldValue, newValue);
    }
    
    @Pure
    @Override
    public boolean interferesWith(@Nonnull Action action) {
        return action instanceof PasswordValueReplace;
    }
    
    @Pure
    @Override
    public @Nonnull PasswordValueReplace getReverse() {
        return new PasswordValueReplace(password, newValue, oldValue);
    }
    
    
    @Pure
    @Override
    public boolean equals(@Nullable Object object) {
        if (protectedEquals(object) && object instanceof PasswordValueReplace) {
            final @Nonnull PasswordValueReplace other = (PasswordValueReplace) object;
            return this.password.equals(other.password) && this.oldValue.equals(other.oldValue) && this.newValue.equals(other.newValue);
        }
        return false;
    }
    
    @Pure
    @Override
    public int hashCode() {
        int hash = protectedHashCode();
        hash = 89 * hash + password.hashCode();
        hash = 89 * hash + oldValue.hashCode();
        hash = 89 * hash + newValue.hashCode();
        return hash;
    }
    
    
    @Pure
    @Override
    public @Nonnull SemanticType getType() {
        return TYPE;
    }
    
    @Pure
    @Override
    public @Nonnull StateModule getModule() {
        return PasswordModule.MODULE;
    }
    
    /**
     * The factory class for the surrounding method.
     */
    private static final class Factory extends Method.Factory {
        
        static { Method.add(TYPE, new Factory()); }
        
        @Pure
        @Override
        @NonCommitting
        protected @Nonnull Method create(@Nonnull Entity entity, @Nonnull SignatureWrapper signature, @Nonnull HostIdentifier recipient, @Nonnull Block block) throws DatabaseException, NetworkException, InternalException, ExternalException, RequestException {
            return new PasswordValueReplace(entity, signature, recipient, block);
        }
        
    }
    
}
