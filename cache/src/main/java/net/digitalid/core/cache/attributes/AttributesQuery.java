package net.digitalid.core.cache.attributes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.collections.list.FreezableArrayList;
import net.digitalid.utility.collections.list.FreezableList;
import net.digitalid.utility.collections.set.FreezableLinkedHashSet;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.validation.annotations.elements.NonNullableElements;
import net.digitalid.utility.validation.annotations.size.NonEmpty;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;
import net.digitalid.database.exceptions.DatabaseException;

import net.digitalid.core.attribute.Attribute;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.exceptions.request.RequestException;
import net.digitalid.core.expression.PassiveExpression;
import net.digitalid.core.handler.annotations.Matching;
import net.digitalid.core.handler.annotations.MethodHasBeenReceived;
import net.digitalid.core.handler.method.CoreMethod;
import net.digitalid.core.handler.method.query.ExternalQuery;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.InternalPerson;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.node.contact.Contact;
import net.digitalid.core.permissions.ReadOnlyAgentPermissions;
import net.digitalid.core.signature.Signature;
import net.digitalid.core.signature.attribute.AttributeValue;
import net.digitalid.core.signature.credentials.CredentialsSignature;
import net.digitalid.core.typeset.permissions.ReadOnlyNodePermissions;
import net.digitalid.core.unit.annotations.OnHostRecipient;

/**
 * Queries the given attributes from the given subject.
 * 
 * @see AttributesReply
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
@GenerateConverter
public abstract class AttributesQuery extends ExternalQuery<Entity> implements CoreMethod<Entity> {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the attribute types that are queried.
     */
    @Pure
    public abstract @Nonnull @NonNullableElements /* TODO: @Frozen */ @NonEmpty /* TODO: ReadOnlyAttributeTypeSet */ FreezableLinkedHashSet<@Nonnull @AttributeType SemanticType> getAttributeTypes();
    
    /**
     * Returns whether the published values are queried.
     */
    @Pure
    public abstract boolean isPublished();
    
    /* -------------------------------------------------- Required Authorization -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull ReadOnlyAgentPermissions getRequiredPermissionsToExecuteMethod() {
//        return getAttributeTypes().toAgentPermissions().freeze(); // TODO
        return ReadOnlyAgentPermissions.NONE;
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    @Override
    @NonCommitting
    @OnHostRecipient
    @PureWithSideEffects
    @MethodHasBeenReceived
    public @Nonnull @Matching AttributesReply executeOnHost() throws RequestException, DatabaseException, RecoveryException {
        final @Nonnull FreezableList<AttributeValue> attributeValues = FreezableArrayList.withInitialCapacity(getAttributeTypes().size());
        
        @SuppressWarnings("null") final @Nonnull Entity entity = getEntity();
        @SuppressWarnings("null") final @Nonnull Signature<?> signature = getSignature();
        @SuppressWarnings("null") final boolean isInternalPerson = entity.getIdentity() instanceof InternalPerson;
        if (signature instanceof CredentialsSignature<?> && isInternalPerson) {
            final @Nonnull CredentialsSignature<?> credentialsSignature = (CredentialsSignature<?>) signature;
            
            final @Nullable ReadOnlyNodePermissions contactPermissions;
            if (false /* TODO: credentialsSignature.isIdentityBased() && !credentialsSignature.isRoleBased() */) {
                final @Nonnull InternalPerson issuer = null; // TODO: credentialsSignature.getIssuer();
                final @Nonnull Contact contact = Contact.of((NonHostEntity) entity, issuer);
                contactPermissions = contact.permissions().get();
            } else {
                contactPermissions = null;
            }
            
            for (final @Nonnull SemanticType attributeType : getAttributeTypes()) {
                final @Nonnull Attribute attribute = Attribute.of(entity, attributeType);
                final @Nullable AttributeValue attributeValue = isPublished() ? attribute.value().get() : attribute.unpublished().get();
                if (attributeValue != null) {
                    if (contactPermissions != null && contactPermissions.contains(attributeType)) { attributeValues.add(attributeValue); }
                    else {
                        final @Nullable PassiveExpression attributeVisibility = attribute.visibility().get();
                        if (attributeVisibility != null && attributeVisibility.matches(credentialsSignature)) { attributeValues.add(attributeValue); }
                        else { attributeValues.add(null); }
                    }
                } else { attributeValues.add(null); }
            }
        } else {
            for (final @Nonnull SemanticType attributeType : getAttributeTypes()) {
                final @Nonnull Attribute attribute = Attribute.of(entity, attributeType);
                final @Nullable AttributeValue attributeValue = isPublished() ? attribute.value().get() : attribute.unpublished().get();
                if (attributeValue != null) {
                    if (isInternalPerson) {
                        final @Nullable PassiveExpression attributeVisibility = attribute.visibility().get();
                        if (attributeVisibility != null && attributeVisibility.isPublic()) { attributeValues.add(attributeValue); }
                        else { attributeValues.add(null); }
                    } else { attributeValues.add(attributeValue); }
                } else { attributeValues.add(null); }
            }
        }
        
        return AttributesReplyBuilder.withEntity(entity).withAttributeValues(attributeValues/* TODO: .freeze() */).build();
    }
    
}
