package ch.virtualid.client;

import ch.virtualid.annotations.Pure;
import ch.virtualid.concept.Aspect;
import ch.virtualid.concept.Instance;
import ch.virtualid.concept.Observer;
import ch.virtualid.cryptography.Parameters;
import ch.virtualid.database.Database;
import ch.virtualid.database.SQLiteConfiguration;
import ch.virtualid.entity.Role;
import ch.virtualid.entity.Site;
import ch.virtualid.exceptions.external.ExternalException;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.io.Directory;
import ch.virtualid.module.client.Roles;
import ch.virtualid.util.FreezableList;
import ch.virtualid.util.ReadonlyList;
import ch.xdf.IntegerWrapper;
import ch.xdf.SelfcontainedWrapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A client is configured with a name and a secret.
 * 
 * TODO: Make sure that the client secret gets rotated!
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 2.0
 */
public final class Client extends Site implements Observer {
    
    /**
     * Stores the aspect of a new role being added to the observed client.
     */
    public static final @Nonnull Aspect ROLE_ADDED = new Aspect(Client.class, "role added");
    
    
    /**
     * Stores the semantic type {@code secret.client@virtualid.ch}.
     */
    public static final @Nonnull SemanticType SECRET = SemanticType.create("secret.client@virtualid.ch").load(IntegerWrapper.TYPE);
    
    
    /**
     * The pattern that valid client names have to match.
     */
    private static final @Nonnull Pattern pattern = Pattern.compile("[a-z_][a-z0-9_$]+", Pattern.CASE_INSENSITIVE);
    
    /**
     * Returns whether the given name is valid for clients.
     * 
     * @param name the client name to check for validity.
     * 
     * @return whether the given name is valid for clients.
     */
    public static boolean isValid(@Nonnull String name) {
        return name.length() <= 40 && pattern.matcher(name).matches() && !(Database.getConfiguration() instanceof SQLiteConfiguration && name.contains("$"));
    }
    
    
    @Pure
    @Override
    public @Nonnull String getReference() {
        return "REFERENCES " + this + "role (role) ON DELETE CASCADE";
    }
    
    
    /**
     * Stores the name of this client.
     */
    private final @Nonnull String name;
    
    /**
     * Stores the secret of this client.
     */
    private final @Nonnull BigInteger secret;
    
    /**
     * Creates a new client with the given name.
     * 
     * @param name the name of the new client.
     * 
     * @require Client.isValid(name) : "The name is valid.";
     */
    public Client(@Nonnull String name) throws SQLException, IOException, ExternalException {
        super(name);
        
        assert Client.isValid(name) : "The name is valid.";
        
        this.name = name;
        
        final @Nonnull File file = new File(Directory.CLIENTS.getPath() +  Directory.SEPARATOR + name + ".client.xdf");
        if (file.exists()) {
            this.secret = new IntegerWrapper(new SelfcontainedWrapper(new FileInputStream(file), true).getElement().checkType(SECRET)).getValue();
        } else {
            final @Nonnull Random random = new SecureRandom();
            this.secret = new BigInteger(Parameters.HASH, random);
            new SelfcontainedWrapper(SelfcontainedWrapper.SELFCONTAINED, new IntegerWrapper(SECRET, secret)).write(new FileOutputStream(file), true);
        }
    }
    
    /**
     * Returns the name of this client.
     * 
     * @return the name of this client.
     */
    @Pure
    public @Nonnull String getName() {
        return name;
    }
    
    /**
     * Returns the secret of this client.
     * 
     * @return the secret of this client.
     */
    @Pure
    public @Nonnull BigInteger getSecret() {
        return secret;
    }
    
    
    /**
     * Stores the roles of this client.
     */
    private @Nullable FreezableList<Role> roles;
    
    /**
     * Returns the roles of this client.
     * 
     * @return the roles of this client.
     */
    @Pure
    public @Nonnull ReadonlyList<Role> getRoles() throws SQLException {
        if (roles == null) roles = Roles.getRoles(this);
        return roles;
    }
    
    /**
     * Adds the given role to the roles of this client.
     * 
     * @param issuer the issuer of the role to add.
     */
    public void addRole(@Nonnull NonHostIdentity issuer) throws SQLException {
        final @Nonnull Role role = Role.add(this, issuer, null, null, true, new SecureRandom().nextLong());
        role.observe(this, Role.REMOVED);
        
        if (roles != null) roles.add(role);
        notify(ROLE_ADDED);
    }
    
    @Override
    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
        if (aspect.equals(Role.REMOVED) && roles != null) {
            roles.remove(instance);
        }
    }
    
}
