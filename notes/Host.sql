CREATE TABLE IF NOT EXISTS context_name (entity BIGINT NOT NULL, context BIGINT NOT NULL, name VARCHAR(50) NOT NULL COLLATE utf16_bin, PRIMARY KEY (entity, context), FOREIGN KEY (entity) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS context_permission (entity BIGINT NOT NULL, context BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (entity, context, type), FOREIGN KEY (entity) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS context_authentication (entity BIGINT NOT NULL, context BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (entity, context, type), FOREIGN KEY (entity) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS context_subcontext (entity BIGINT NOT NULL, context BIGINT NOT NULL, subcontext BIGINT NOT NULL, position TINYINT, PRIMARY KEY (entity, context, subcontext), FOREIGN KEY (entity) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS context_contact (entity BIGINT NOT NULL, context BIGINT NOT NULL, contact BIGINT NOT NULL, PRIMARY KEY (entity, context, contact), FOREIGN KEY (entity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity))
-> table with preferences of contexts (and also of contacts)
-> be able to mark contexts as deleted? -> accomplished by removing them from the DAG. -> No, just marking them as deleted seems to have many benefits over removing them from the DAG! -> Hm, does not seem to work unfortunately.
-> support icons for contexts.
-> add foreign key constraint on the context value?
-> the value of the context is just a random number.
-> Is a tree better than a DAG? Preventing loops is more complicated in the latter case.

CREATE TABLE IF NOT EXISTS context_preference (entity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (entity, contact, type), FOREIGN KEY (entity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))
-> redefine the preferences as what the identity requests from the contact and not vice versa. -> only for contexts!

CREATE TABLE IF NOT EXISTS contact_permission (entity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (entity, contact, type), FOREIGN KEY (entity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS contact_authentication (entity BIGINT NOT NULL, contact BIGINT NOT NULL, type BIGINT NOT NULL, PRIMARY KEY (entity, contact, type), FOREIGN KEY (entity) REFERENCES map_identity (identity), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))
-> permissions and authentications of contacts do not need to be explicitly removed.

CREATE TABLE IF NOT EXISTS agent (entity BIGINT NOT NULL, agent BIGINT NOT NULL, client BOOLEAN NOT NULL, removed BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY (entity, agent), FOREIGN KEY (identity) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS agent_permission (entity BIGINT NOT NULL, agent BIGINT NOT NULL, type BIGINT NOT NULL, writing BOOLEAN NOT NULL, PRIMARY KEY (agent, preference, type), FOREIGN KEY (agent) REFERENCES agent (agent) ON DELETE CASCADE, FOREIGN KEY (type) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS agent_restrictions (entity BIGINT NOT NULL, agent BIGINT NOT NULL, client BOOLEAN NOT NULL, context BIGINT NOT NULL, writing BOOLEAN NOT NULL, history BIGINT NOT NULL, role BOOLEAN NOT NULL, PRIMARY KEY (agent), FOREIGN KEY (agent) REFERENCES agent (agent) ON DELETE CASCADE)

CREATE TABLE IF NOT EXISTS agent_order (entity BIGINT NOT NULL, stronger BIGINT NOT NULL, weaker BIGINT NOT NULL, PRIMARY KEY (stronger, weaker), FOREIGN KEY (stronger) REFERENCES agent (agent), FOREIGN KEY (weaker) REFERENCES agent (agent))
-> make sure to ignore unreachable contexts (i.e. every context is stronger than a context not reachable from the root)!

CREATE TABLE IF NOT EXISTS client_agent (entity BIGINT NOT NULL, agent BIGINT NOT NULL, host BIGINT NOT NULL, time BIGINT NOT NULL, value BLOB NOT NULL, name VARCHAR(50) NOT NULL COLLATE utf16_bin, icon BLOB NOT NULL, PRIMARY KEY (agent), FOREIGN KEY (agent) REFERENCES agent (agent), FOREIGN KEY (host) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS outgoing_role (entity BIGINT NOT NULL, agent BIGINT NOT NULL, relation BIGINT NOT NULL, context BIGINT NOT NULL, PRIMARY KEY (agent), FOREIGN KEY (agent) REFERENCES agent (agent), FOREIGN KEY (relation) REFERENCES map_identity (identity))
CREATE TABLE IF NOT EXISTS incoming_role (entity BIGINT NOT NULL, issuer BIGINT NOT NULL, relation BIGINT NOT NULL, agent BIGINT NOT NULL, PRIMARY KEY (entity, issuer, relation), FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (relation) REFERENCES map_identity (identity))
-> incoming_role only on server and thus without a client class. (Please note that the agent column references no foreign key!)

CREATE TABLE IF NOT EXISTS certificate (issuer BIGINT NOT NULL, recipient BIGINT NOT NULL, type BIGINT NOT NULL, value LONGBLOB NOT NULL, issuance BIGINT, PRIMARY KEY (issuer, recipient, type), FOREIGN KEY (issuer) REFERENCES map_identity (identity), FOREIGN KEY (recipient) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))
-> Split into certificate_value and certificate_issuance? The latter is not needed on the client-side!

CREATE TABLE IF NOT EXISTS request (identity BIGINT NOT NULL, time BIGINT NOT NULL, client BOOLEAN, role BOOLEAN, context BIGINT, contact BIGINT, type BIGINT, writing BOOLEAN, agent BIGINT, request LONGBLOB NOT NULL, PRIMARY KEY (identity, time), INDEX(time), FOREIGN KEY (identity) REFERENCES map_identity (identity), FOREIGN KEY (agent) REFERENCES agent (agent), FOREIGN KEY (contact) REFERENCES map_identity (identity), FOREIGN KEY (type) REFERENCES map_identity (identity))
-> include context writing -> why? -> No. -> Yes, for access requests and resol.
-> include the type of the service!
-> different on the client-side: only role, time and request type needed.
-> on client-side: pending and executed actions
-> the time column should be unique instead of just an index? Plus make sure that result sets are sorted in ascending time.


Considerations:
- agent_order is only needed for the own role on the client-side. -> No! A client (like an operating system) might act as an intermediary.

- Clients have to store executed but unsubmitted requests also in the database in order to achieve transaction integrity.
- All actions have to be reversible in order that clients can undo already executed but unsuccesfully submitted actions.
-> Contexts cannot be removed if an outgoing role or a client depends on one of its subcontexts (including itself).

- Only access methods: getState and getAgents, where at least the former includes the current time of the host database.

-> The action addAll on the host has to be the first action after the creation of the account. The handler also merges potential predecessors in this step.

-> The hash of the commitment and the token is only part of the accreditation request, which means that clients accredited later on can (respectively do) not authorize this client.

Question:
- Services are both host and client. How to handle this? Possibilities:
	- Client runs in a different process with an own database. -> Writing eveything to streams and reading from such again is a hassle.
	- Client runs in the same process but has a different database. (Could be implemented by separate getConnection()-methods. Not sure.) -> What about the mapper? If identities cannot be passed around, this seems useless.
	- Client runs on same database but uses different tables. -> Only candidate left! :-) How to continue from here? What needs to change?
	- Another possibility: Services are only hosts and issue external actions accordingly. -> They still need to be able to read the contacts and request attributes on behalf of the identity. Doesn't seem to solve the problem.

Similar question: Can there be several clients in the same process? What are the advantages? -> There doesn't seem to be any. -> Yes, every host need its own client (for relocation).


TODOs:
- Does normal AES include integrity checks?
- Protection against various attacks: https://core.telegram.org/techfaq
- Create files for the commitments of clients such that they can be transferred to hosts in order to allow the creation of new identities (besides tokens and passwords)


Regular Expressions:
\[(.+?)\] -> .getNotNull($1)
\[(.+?)\] = (.+?); -> .set($1, $2);
.set\((.+?), (.+?)\); -> \[$1\] = $2;

DNS Lookups:
host -t SRV _vid._tcp.digitalid.net
nslookup -type=SRV _vid._tcp.digitalid.net
