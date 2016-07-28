package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.contracts.Require;
import net.digitalid.utility.generator.annotations.generators.GenerateConverter;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.logging.exceptions.ExternalException;
import net.digitalid.utility.threading.Threading;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.identification.Category;
import net.digitalid.core.identification.annotations.type.loaded.Loaded;
import net.digitalid.core.identification.annotations.type.loaded.LoadedRecipient;
import net.digitalid.core.identification.annotations.type.loaded.NonLoadedRecipient;

/**
 * This class models a syntactic type.
 */
@Mutable
@GenerateSubclass
@GenerateConverter
public abstract class SyntacticType extends Type {
    
    /* -------------------------------------------------- Semantic Type Hacks -------------------------------------------------- */
    
    // TODO: Document the reason for these hacks more precisely.
    
//    /**
//     * Stores the semantic type {@code @core.digitalid.net}.
//     * (This hack was necessary to get the initialization working.)
//     */
//    static final @Nonnull SemanticType IDENTITY_IDENTIFIER = SemanticType.map("@core.digitalid.net").load(StringWrapper.XDF_TYPE);
//    
//    /**
//     * Stores the semantic type {@code nonhost@core.digitalid.net}.
//     * (This hack was necessary to get the initialization working.)
//     */
//    static final @Nonnull SemanticType NONHOST_IDENTIFIER = SemanticType.map("nonhost@core.digitalid.net").load(IDENTITY_IDENTIFIER);
//    
//    /**
//     * Stores the semantic type {@code type@core.digitalid.net}.
//     * (This hack was necessary to get the initialization working.)
//     */
//    static final @Nonnull SemanticType TYPE_IDENTIFIER = SemanticType.map("type@core.digitalid.net").load(NONHOST_IDENTIFIER);
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    // TODO: Move this to the resolution package?
    
//    /**
//     * Maps the syntactic type with the given identifier.
//     * 
//     * @param identifier the identifier of the syntactic type.
//     * 
//     * @require InternalNonHostIdentifier.isValid(identifier) : "The string is a valid internal non-host identifier.";
//     */
//    @MainThread
//    public static @Nonnull @NonLoaded SyntacticType map(@Nonnull String identifier) {
//        return Mapper.mapSyntacticType(new InternalNonHostIdentifier(identifier));
//    }
    
    /* -------------------------------------------------- Number of Parameters -------------------------------------------------- */
    
    /**
     * Stores the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     * 
     * @invariant !isLoaded() || numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    private byte numberOfParameters;
    
    /**
     * Returns the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     * 
     * @return the number of generic parameters of this syntactic type.
     * 
     * @ensure numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    @Pure
    @LoadedRecipient
    public byte getNumberOfParameters() {
        Require.that(isLoaded()).orThrow("The type declaration is already loaded.");
        
        return numberOfParameters;
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
//    /**
//     * Stores the semantic type {@code parameters.syntactic.type@core.digitalid.net}.
//     */
//    public static final @Nonnull SemanticType PARAMETERS = SemanticType.map("parameters.syntactic.type@core.digitalid.net").load(new Category[] {Category.SYNTACTIC_TYPE}, Time.TROPICAL_YEAR, Integer08Wrapper.XDF_TYPE);
    
    @Impure
    @Override
    @NonCommitting
    void load() throws ExternalException {
        Require.that(!isLoaded()).orThrow("The type declaration is not loaded.");
        
        // TODO: Use the type loader instead!
        
//        this.numberOfParameters = Integer08Wrapper.decode(Cache.getStaleAttributeContent(this, null, PARAMETERS));
//        if (numberOfParameters < -1) { throw InvalidDeclarationException.get("The number of parameters has to be at least -1 but was " + numberOfParameters + ".", getAddress()); }
//        setLoaded();
    }
    
    /**
     * Loads the type declaration from the given parameter.
     * 
     * @param numberOfParameters the number of generic parameters.
     * 
     * @require numberOfParameters >= -1 : "The number of parameters is at least -1.";
     * @require numberOfParameters <= 127 : "The number of parameters is at most 127.";
     */
    @Impure
    @MainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SyntacticType load(int numberOfParameters) {
        Require.that(!isLoaded()).orThrow("The type declaration is not loaded.");
        Require.that(Threading.isMainThread()).orThrow("This method may only be called in the main thread.");
        
        Require.that(numberOfParameters >= -1).orThrow("The number of parameters is at least -1.");
        Require.that(numberOfParameters <= 127).orThrow("The number of parameters is at most 127.");
        
        this.numberOfParameters = (byte) numberOfParameters;
        setLoaded();
        
        return this;
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.SYNTACTIC_TYPE;
    }
    
}
