package net.digitalid.core.clientagent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.digitalid.utility.annotations.method.Pure;
import net.digitalid.utility.annotations.method.PureWithSideEffects;
import net.digitalid.utility.conversion.exceptions.RecoveryException;
import net.digitalid.utility.initialization.annotations.Initialize;
import net.digitalid.utility.validation.annotations.type.Stateless;

import net.digitalid.database.conversion.SQL;
import net.digitalid.database.conversion.utility.WhereCondition;
import net.digitalid.database.conversion.utility.WhereConditionBuilder;
import net.digitalid.database.exceptions.DatabaseException;
import net.digitalid.database.property.value.PersistentValuePropertyEntry;

import net.digitalid.core.agent.AgentRetriever;
import net.digitalid.core.commitment.Commitment;
import net.digitalid.core.commitment.CommitmentConverter;
import net.digitalid.core.entity.NonHostEntity;
import net.digitalid.core.entity.NonHostEntityConverter;
import net.digitalid.core.unit.GeneralUnit;

/**
 *
 */
@Stateless
public class ClientAgentRetriever implements AgentRetriever {
    
    @Pure
    @Override
    public @Nullable ClientAgent getAgent(@Nonnull NonHostEntity entity, @Nonnull Commitment commitment) throws DatabaseException, RecoveryException {
        final @Nonnull WhereCondition<NonHostEntity> entityWhereCondition = WhereConditionBuilder.withWhereConverter(NonHostEntityConverter.INSTANCE).withWhereObject(entity).build();
        final WhereCondition<Commitment> commitmentWhereCondition = WhereConditionBuilder.withWhereConverter(CommitmentConverter.INSTANCE).withWhereObject(commitment).build();
        final @Nullable PersistentValuePropertyEntry<ClientAgent, Commitment> clientAgentCommitment = SQL.selectFirst(ClientAgentSubclass.COMMITMENT_TABLE, null, GeneralUnit.INSTANCE, entityWhereCondition, commitmentWhereCondition);
        if (clientAgentCommitment != null) {
            return clientAgentCommitment.getSubject();
        } else {
            return null;
        }
    }
    
    /* -------------------------------------------------- Initialization -------------------------------------------------- */
    
    
    /**
     * Initializes the agent factory.
     */
    @PureWithSideEffects
    @Initialize(target = AgentRetriever.class)
    public static void initializeAgentRetriever() {
        AgentRetriever.configuration.set(new ClientAgentRetriever());
    }
    
}
