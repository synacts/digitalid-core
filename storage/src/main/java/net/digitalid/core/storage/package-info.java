/**
 * Provides classes that model several storages.
 * <p>
 * <ul>
 * <li>The interface {@link ClientStorage} introduces the methods {@link ClientStorage#createTables(net.digitalid.database.core.site.Site)} and {@link ClientStorage#deleteTables(net.digitalid.database.core.site.Site)}.
 * <li>The interface {@link HostStorage} introduces the methods {@link HostStorage#exportAll(net.digitalid.service.core.site.host.Host)} and {@link HostStorage#importAll(net.digitalid.service.core.site.host.Host, net.digitalid.service.core.block.Block)}.
 * <li>The interface {@link SiteStorage} introduces the methods {@link SiteStorage#getState(net.digitalid.service.core.entity.NonHostEntity, net.digitalid.service.core.concepts.agent.ReadOnlyAgentPermissions, net.digitalid.service.core.concepts.agent.Restrictions, net.digitalid.service.core.concepts.agent.Agent)}, {@link SiteStorage#addState(net.digitalid.service.core.entity.NonHostEntity, net.digitalid.service.core.block.Block)} and {@link SiteStorage#removeState(net.digitalid.service.core.entity.NonHostEntity)}.
 * </ul>
 */
package net.digitalid.core.state;
