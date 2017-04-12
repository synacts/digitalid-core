package net.digitalid.core.cache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.generics.Unspecifiable;
import net.digitalid.utility.annotations.method.CallSuper;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Validate;
import net.digitalid.utility.conversion.interfaces.Converter;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateBuilder;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.rootclass.RootClass;
import net.digitalid.utility.time.Time;
import net.digitalid.utility.validation.annotations.generation.Default;
import net.digitalid.utility.validation.annotations.generation.Derive;
import net.digitalid.utility.validation.annotations.math.NonNegative;
import net.digitalid.utility.validation.annotations.type.Immutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.cache.exceptions.CertificateNotFoundExceptionBuilder;
import net.digitalid.core.client.role.Role;
import net.digitalid.core.identification.annotations.AttributeType;
import net.digitalid.core.identification.identity.InternalIdentity;
import net.digitalid.core.identification.identity.SemanticType;
import net.digitalid.core.signature.attribute.AttributeValue;

/**
 * This type models query for the cache.
 * 
 * @invariant getDerivedType().isAttributeFor(getRequestee().getCategory()) : "The type can be used as an attribute for the category of the given requestee.";
 */
@Immutable
@GenerateBuilder
@GenerateSubclass
public abstract class CacheQuery<@Unspecifiable TYPE> extends RootClass {
    
    /* -------------------------------------------------- Fields -------------------------------------------------- */
    
    /**
     * Returns the converter used to recover the attribute content.
     */
    @Pure
    public abstract @Nonnull Converter<TYPE, Void> getConverter();
    
    /**
     * Returns the identity whose attribute content is to be returned.
     */
    @Pure
    public abstract @Nonnull InternalIdentity getRequestee();
    
    /**
     * Returns the role that queries the attribute content or null for hosts.
     */
    @Pure
    public abstract @Nullable Role getRequester();
    
    /**
     * Returns the time at which the cached attribute content has to be fresh.
     */
    @Pure
    @Default("net.digitalid.utility.time.TimeBuilder.build()")
    public abstract @Nonnull @NonNegative Time getExpiration();
    
    /**
     * Returns whether the attribute content should be certified.
     */
    @Pure
    @Default("false")
    public abstract boolean isCertified();
    
    /**
     * Returns the provided type of the attribute content which is to be returned.
     */
    @Pure
    public abstract @Nullable @AttributeType SemanticType getType();
    
    /**
     * Returns the derived type of the attribute content which is to be returned.
     */
    @Pure
    @Derive("type != null ? type : SemanticType.mapWithoutPersistingResult(converter)")
    public abstract @Nonnull @AttributeType SemanticType getDerivedType();
    
    /* -------------------------------------------------- Validation -------------------------------------------------- */
    
    @Pure
    @Override
    @CallSuper
    public void validate() {
        Validate.that(getDerivedType().isAttributeFor(getRequestee().getCategory())).orThrow("The type $ cannot be used as an attribute for the category of the requestee $.", getDerivedType(), getRequestee());
        super.validate();
    }
    
    /* -------------------------------------------------- Execution -------------------------------------------------- */
    
    /**
     * Executes this cache query and returns the queried attribute content.
     * 
     * @throws AttributeNotFoundException if the attribute value is not available.
     * @throws CertificateNotFoundException if the value should be certified but is not.
     */
    @Pure
    @NonCommitting
    public @Nonnull TYPE execute() throws ExternalException {
        final @Nonnull AttributeValue value = Cache.getAttributeValue(getRequester(), getRequestee(), getExpiration(), getDerivedType());
        if (isCertified() && !value.isCertified()) { throw CertificateNotFoundExceptionBuilder.withIdentity(getRequestee()).withType(getDerivedType()).build(); }
        return value.getContent().unpack(getConverter(), null);
    }
    
}
