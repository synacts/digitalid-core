package net.digitalid.core.identification.identity;

import javax.annotation.Nonnull;

import net.digitalid.utility.annotations.method.Impure;
import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.exceptions.ExternalException;
import net.digitalid.utility.generator.annotations.generators.GenerateSubclass;
import net.digitalid.utility.threading.annotations.MainThread;
import net.digitalid.utility.validation.annotations.math.relative.GreaterThanOrEqualTo;
import net.digitalid.utility.validation.annotations.math.relative.LessThanOrEqualTo;
import net.digitalid.utility.validation.annotations.type.Mutable;

import net.digitalid.database.annotations.transaction.NonCommitting;

import net.digitalid.core.annotations.type.Loaded;
import net.digitalid.core.annotations.type.LoadedRecipient;
import net.digitalid.core.annotations.type.NonLoaded;
import net.digitalid.core.annotations.type.NonLoadedRecipient;
import net.digitalid.core.identification.identifier.InternalNonHostIdentifier;

/**
 * This class models a syntactic type.
 */
@Mutable
@GenerateSubclass
public abstract class SyntacticType extends Type {
    
    /* -------------------------------------------------- Mapping -------------------------------------------------- */
    
    /**
     * Maps the syntactic type with the given string, which has to be a valid internal non-host identifier.
     */
    @Pure
    @MainThread
    public static @Nonnull @NonLoaded SyntacticType map(@Nonnull String identifier) {
        return IdentifierResolver.configuration.get().mapSyntacticType(InternalNonHostIdentifier.with(identifier));
    }
    
    /* -------------------------------------------------- Types -------------------------------------------------- */
    
    public static final @Nonnull @Loaded SyntacticType BOOLEAN = SyntacticType.map("boolean@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType INTEGER08 = SyntacticType.map("integer08@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType INTEGER16 = SyntacticType.map("integer16@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType INTEGER32 = SyntacticType.map("integer32@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType INTEGER64 = SyntacticType.map("integer64@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType INTEGER = SyntacticType.map("integer@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType DECIMAL32 = SyntacticType.map("decimal32@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType DECIMAL64 = SyntacticType.map("decimal64@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType STRING01 = SyntacticType.map("string01@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType STRING64 = SyntacticType.map("string64@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType STRING = SyntacticType.map("string@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType BINARY128 = SyntacticType.map("binary128@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType BINARY256 = SyntacticType.map("binary256@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType BINARY = SyntacticType.map("binary@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType TUPLE = SyntacticType.map("tuple@core.digitalid.net").load(-1);
    
    public static final @Nonnull @Loaded SyntacticType LIST = SyntacticType.map("list@core.digitalid.net").load(1);
    
    public static final @Nonnull @Loaded SyntacticType PACK = SyntacticType.map("pack@core.digitalid.net").load(0);
    
    public static final @Nonnull @Loaded SyntacticType SIGNATURE = SyntacticType.map("signature@core.digitalid.net").load(1);
    
    public static final @Nonnull @Loaded SyntacticType ENCRYPTION = SyntacticType.map("encryption@core.digitalid.net").load(1);
    
    public static final @Nonnull @Loaded SyntacticType COMPRESSION = SyntacticType.map("compression@core.digitalid.net").load(1);
    
    /* -------------------------------------------------- Number of Parameters -------------------------------------------------- */
    
    private @GreaterThanOrEqualTo(-1) byte numberOfParameters;
    
    /**
     * Returns the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     */
    @Pure
    @LoadedRecipient
    public @GreaterThanOrEqualTo(-1) byte getNumberOfParameters() {
        return numberOfParameters;
    }
    
    /* -------------------------------------------------- Loaded -------------------------------------------------- */
    
    private boolean loaded = false;
    
    @Pure
    @Override
    public boolean isLoaded() {
        return loaded;
    }
    
    /* -------------------------------------------------- Loading -------------------------------------------------- */
    
    @Impure
    @Override
    @NonCommitting
    @NonLoadedRecipient
    void load() throws ExternalException {
        this.numberOfParameters = TypeLoader.configuration.get().load(this);
        this.loaded = true;
    }
    
    /**
     * Loads the type declaration from the given number of generic parameters.
     */
    @Impure
    @MainThread
    @NonLoadedRecipient
    public @Nonnull @Loaded SyntacticType load(@GreaterThanOrEqualTo(-1) @LessThanOrEqualTo(127) int numberOfParameters) {
        this.numberOfParameters = (byte) numberOfParameters;
        this.loaded = true;
        return this;
    }
    
    /* -------------------------------------------------- Category -------------------------------------------------- */
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.SYNTACTIC_TYPE;
    }
    
}
