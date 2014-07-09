package ch.virtualid.concept;

import ch.virtualid.annotation.Pure;
import ch.virtualid.database.Entity;
import ch.virtualid.identity.NonHostIdentity;
import ch.virtualid.identity.SemanticType;
import ch.virtualid.interfaces.SQLizable;
import ch.xdf.Block;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Description.
 * 
 * @author Kaspar Etter (kaspar.etter@virtualid.ch)
 * @version 0.0
 */
public final class Role extends Concept implements SQLizable {
    
    private final long number;
    
    private final @Nonnull NonHostIdentity issuer;
    
    private final @Nullable SemanticType relation;
    
    private final @Nullable Role recipient;
    
    public Role(@Nonnull Entity connection, long number, @Nonnull NonHostIdentity issuer) {
        super(connection, null);
        
        assert connection.isOnBoth() : "";
        
        this.number = number;
        this.issuer = issuer;
        this.relation = null;
        this.recipient = null;
    }
    
    @Override
    public Block encode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public long getNumber() {
        return number;
    }
    
    public @Nonnull NonHostIdentity getIssuer() {
        return issuer;
    }
    
    // TODO: Implement equals()!
    
    
    /**
     * Returns the given column of the result set as an instance of this class.
     * 
     * @param resultSet the result set to retrieve the data from.
     * @param columnIndex the index of the column containing the data.
     * 
     * @return the given column of the result set as an instance of this class.
     */
    @Pure
    public static @Nonnull Role get(@Nonnull ResultSet resultSet, int columnIndex) throws SQLException {
        throw new UnsupportedOperationException(); // TODO
    }
    
    @Override
    public void set(@Nonnull PreparedStatement preparedStatement, int parameterIndex) throws SQLException {
        preparedStatement.setLong(parameterIndex, number);
    }
    
    @Override
    public @Nonnull String toString() {
        return String.valueOf(number);
    }
    
}
