package net.digitalid.core.clientagent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.conversion.interfaces.Converter;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.interfaces.Database;

import net.digitalid.core.agent.Agent;
import net.digitalid.core.agent.AgentConverter;
import net.digitalid.core.agent.AgentFactory;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.entity.Entity;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.NonHostEntityConverter;
import net.digitalid.core.unit.GeneralUnit;

/**
 *
 */
public class ClientAgentFactory implements AgentFactory {
    
    @Override
    public @Nonnull Agent getAgent(@Nonnull NonHostEntity entity, long key) throws DatabaseException {
        return null;
    }
    
    @Override
    public @Nullable ClientAgent getAgent(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws DatabaseException {
        final @Nonnull Converter<NonHostEntity, Void> whereConverter = NonHostEntityConverter.INSTANCE;
        final @Nullable ClientAgent clientAgent = SQL.selectFirst(ClientAgentSubclass.COMMITMENT_TABLE, null, whereConverter, whereObject, GeneralUnit.INSTANCE);
        final @Nonnull Site site = entity.getSite();
        final @Nonnull String SQL = "SELECT a.agent, a.removed FROM " + site + "client_agent c, " + site + "agent a WHERE c.entity = " + entity + " AND a.entity = " + entity + " AND c.agent = a.agent AND " + Commitment.CONDITION;
        try (@Nonnull PreparedStatement preparedStatement = Database.prepareStatement(SQL)) {
            commitment.set(preparedStatement, 1);
            try (@Nonnull ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) return ClientAgent.get(entity, resultSet.getLong(1), resultSet.getBoolean(2));
                else return null;
            }
        }
        return null;
    }
    
}
