package ch.virtualid.identity;

import ch.virtualid.annotations.Pure;
import ch.virtualid.database.Database;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.exceptions.packet.PacketException;
import ch.virtualid.identifier.InternalNonHostIdentifier;
import ch.virtualid.interfaces.Immutable;
import ch.xdf.StringWrapper;
import java.io.IOException;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * This class models a syntactic type.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 1.9
 */
public final class SyntacticType extends Type implements Immutable {
    
    /**
     * Stores the semantic type {@code @virtualid.ch}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType IDENTITY_IDENTIFIER = SemanticType.create("@virtualid.ch").load(StringWrapper.TYPE);
    
    /**
     * Stores the semantic type {@code nonhost@virtualid.ch}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType NONHOST_IDENTIFIER = SemanticType.create("nonhost@virtualid.ch").load(IDENTITY_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code type@virtualid.ch}.
     * (This hack was necessary to get the initialization working.)
     */
    static final @Nonnull SemanticType TYPE_IDENTIFIER = SemanticType.create("type@virtualid.ch").load(NONHOST_IDENTIFIER);
    
    /**
     * Stores the semantic type {@code syntactic.type@virtualid.ch}.
     */
    public static final @Nonnull SemanticType IDENTIFIER = SemanticType.create("syntactic.type@virtualid.ch").load(TYPE_IDENTIFIER);
    
    
    /**
     * Stores the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     * 
     * @invariant !isLoaded() || numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    private int numberOfParameters;
    
    /**
     * Creates a new identity with the given number and address.
     * 
     * @param number the number that represents this identity.
     * @param address the current address of this identity.
     * 
     * @ensure !isLoaded() : "The type declaration has not yet been loaded.";
     */
    SyntacticType(long number, @Nonnull InternalNonHostIdentifier address) {
        super(number, address);
    }
    
    /**
     * Creates a new syntactic type with the given identifier.
     * 
     * @param identifier the identifier of the new syntactic type.
     * 
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * @require Identifier.isValid(identifier) : "The string is a valid identifier.";
     * @require !Identifier.isHost(identifier) : "The string may not denote a host identifier.";
     * 
     * @ensure !isLoaded() : "The type declaration has not yet been loaded.";
     */
    public static @Nonnull SyntacticType create(@Nonnull String identifier) {
        return Mapper.mapSyntacticType(new InternalNonHostIdentifier(identifier));
    }
    
    
    @Override
    void load() throws SQLException, IOException, PacketException, ExternalException {
        assert !isLoaded() : "The type declaration may not yet have been loaded.";
        
        // TODO: Make the lookup for the number of generic parameters.
        this.numberOfParameters = -1;
        setLoaded();
    }
    
    /**
     * Loads the type declaration from the given parameter.
     * 
     * @param numberOfParameters the number of generic parameters.
     * 
     * @require !isLoaded() : "The type declaration may not yet have been loaded.";
     * @require Database.isMainThread() : "This method may only be called in the main thread.";
     * 
     * @require numberOfParameters >= -1 : "The number of parameters is at least -1.";
     * 
     * @ensure isLoaded() : "The type declaration has been loaded.";
     */
    public @Nonnull SyntacticType load(int numberOfParameters) {
        assert !isLoaded() : "The type declaration may not yet have been loaded.";
        assert Database.isMainThread() : "This method may only be called in the main thread.";
        
        assert numberOfParameters >= -1 : "The number of parameters is at least -1.";
        
        this.numberOfParameters = numberOfParameters;
        setLoaded();
        
        return this;
    }
    
    
    @Pure
    @Override
    public @Nonnull Category getCategory() {
        return Category.SYNTACTIC_TYPE;
    }
    
    
    /**
     * Returns the number of generic parameters of this syntactic type.
     * A value of -1 indicates a variable number of parameters.
     * 
     * @return the number of generic parameters of this syntactic type.
     * 
     * @require isLoaded() : "The type declaration is already loaded.";
     * 
     * @ensure numberOfParameters >= -1 : "The number of parameters is at least -1.";
     */
    @Pure
    public int getNumberOfParameters() {
        assert isLoaded() : "The type declaration is already loaded.";
        
        return numberOfParameters;
    }
    
}
