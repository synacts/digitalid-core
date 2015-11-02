package net.digitalid.service.core.identifier;

import javax.annotation.Nonnull;
import net.digitalid.service.core.exceptions.abort.AbortException;
import net.digitalid.service.core.exceptions.external.ExternalException;
import net.digitalid.service.core.exceptions.external.InvalidEncodingException;
import net.digitalid.service.core.exceptions.network.NetworkException;
import net.digitalid.service.core.exceptions.packet.PacketException;
import net.digitalid.service.core.factory.Factories;
import net.digitalid.service.core.identity.Category;
import net.digitalid.service.core.identity.ExternalIdentity;
import net.digitalid.service.core.identity.Identity;
import net.digitalid.service.core.identity.Person;
import net.digitalid.service.core.identity.resolution.Mapper;
import net.digitalid.utility.annotations.state.Immutable;
import net.digitalid.utility.annotations.state.Pure;
import net.digitalid.utility.annotations.state.Validated;
import net.digitalid.utility.database.annotations.NonCommitting;
import net.digitalid.utility.system.errors.ShouldNeverHappenError;

/**
 * This class models external identifiers.
 * 
 * @see EmailIdentifier
 * @see MobileIdentifier
 */
@Immutable
public abstract class ExternalIdentifier extends IdentifierClass implements NonHostIdentifier {
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Validity –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns whether the given string conforms to the criteria of this class.
     *
     * @param string the string to check.
     * 
     * @return whether the given string conforms to the criteria of this class.
     */
    @Pure
    static boolean isConforming(@Nonnull String string) {
        return IdentifierClass.isConforming(string) && string.contains(":");
    }
    
    /**
     * Returns whether the given string is a valid identifier.
     *
     * @param string the string to check.
     * 
     * @return whether the given string is a valid identifier.
     */
    @Pure
    public static boolean isValid(@Nonnull String string) {
        final int index = string.indexOf(":");
        if (index < 1) return false;
        final @Nonnull String scheme = string.substring(0, index);
        switch (scheme) {
            case "email": return EmailIdentifier.isValid(string);
            case "mobile": return MobileIdentifier.isValid(string);
            default: return false;
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Constructor –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Creates an external identifier with the given string.
     * 
     * @param string the string of the external identifier.
     */
    ExternalIdentifier(@Nonnull @Validated String string) {
        super(string);
        
        assert isValid(string) : "The string is a valid external identifier.";
    }
    
    /**
     * Returns a new external identifier with the given string.
     * 
     * @param string the string of the new external identifier.
     * 
     * @return a new external identifier with the given string.
     */
    @Pure
    public static @Nonnull Identifier get(@Nonnull @Validated String string) {
        final @Nonnull String scheme = string.substring(0, string.indexOf(":"));
        switch (scheme) {
            case "email": return EmailIdentifier.get(string);
            case "mobile": return MobileIdentifier.get(string);
            default: throw new ShouldNeverHappenError("The scheme '" + scheme + "' is not valid.");
        }
    }
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Mapping –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    @Pure
    @Override
    @NonCommitting
    public final @Nonnull Person getMappedIdentity() throws AbortException {
        assert isMapped() : "This identifier is mapped.";
        
        final @Nonnull Identity identity = Mapper.getMappedIdentity(this);
        if (identity instanceof Person) return (Person) identity;
        else throw AbortException.get("The mapped identity has a wrong type.");
    }
    
    @Pure
    @Override
    @NonCommitting
    public abstract @Nonnull Person getIdentity() throws AbortException, PacketException, ExternalException, NetworkException;
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Category –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Returns the category of this external identifier.
     * 
     * @return the category of this external identifier.
     * 
     * @ensure return.isExternalPerson() : "The returned category denotes an external person.";
     */
    @Pure
    public abstract @Nonnull Category getCategory();
    
    /* –––––––––––––––––––––––––––––––––––––––––––––––––– Factories –––––––––––––––––––––––––––––––––––––––––––––––––– */
    
    /**
     * Stores the caster that casts identifiers to this subclass.
     */
    private static final @Nonnull Caster<ExternalIdentifier> CASTER = new Caster<ExternalIdentifier>() {
        @Pure
        @Override
        protected @Nonnull ExternalIdentifier cast(@Nonnull Identifier identifier) throws InvalidEncodingException {
            return identifier.toExternalIdentifier();
        }
    };
    
    /**
     * Stores the encoding factory of this class.
     */
    public static final @Nonnull EncodingFactory<ExternalIdentifier> ENCODING_FACTORY = new EncodingFactory<>(ExternalIdentity.IDENTIFIER, CASTER);
    
    /**
     * Stores the storing factory of this class.
     */
    public static final @Nonnull StoringFactory<ExternalIdentifier> STORING_FACTORY = new StoringFactory<>(CASTER);
    
    /**
     * Stores the factories of this class.
     */
    public static final @Nonnull Factories<ExternalIdentifier, Object> FACTORIES = Factories.get(ENCODING_FACTORY, STORING_FACTORY);
    
}
