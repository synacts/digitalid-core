# Digital ID Reference Implementation

## Contents

**[Abstract](#abstract)**

**[Bundles](#bundles)**

**[Usage](#usage)**
- [Server](#server)
  - [Requirements](#requirements)
  - [Setup](#setup)
- [Library](#library)
  - [Classpath Setup](#classpath-setup)
  - [Role Creation](#role-creation)
  - [Concept Retrieval](#concept-retrieval)
  - [Attribute Caching](#attribute-caching)
  - [Block Wrapping](#block-wrapping)
  - [Event Listening](#event-listening)
  - [Database Locking](#database-locking)
  - [Other Annotations](#other-annotations)

**[Overview](#overview)**
- [Concepts](#concepts)
  - [Digital Identities](#digital-identities)
  - [Attributes and Certificates](#attributes-and-certificates)
  - [Contacts and Contexts](#contacts-and-contexts)
  - [Clients and Roles](#clients-and-roles)
- [Architecture](#architecture)
- [Cryptography](#cryptography)
- [Synchronization](#synchronization)

**[Documentation](#documentation)**

**[Roadmap](#roadmap)**

**[Authors](#authors)**

**[Contact](#contact)**

**[License](#license)**

## Abstract

Digital ID is a protocol that constitutes an identity layer for the Internet and a semantic alternative to the World Wide Web. It allows you to prove your identity towards others and to look up attributes of others in a decentralized manner. Being freely extensible with services, Digital ID aims to supersede proprietary platforms by establishing a framework of open standards.

## Bundles

The reference implementation is available for [downlaod](https://www.digitalid.net/downloads/) in three different bundles:
- **[DigitalID.jar](https://www.digitalid.net/downloads/DigitalID.jar)** (1 MB): Contains only the class files of the core protocol. The libraries have to be provided separately.
- **[DigitalID-Server.jar](https://www.digitalid.net/downloads/DigitalID-Server.jar)** (7 MB): Includes all the libraries that are required to run the implementation as a server. (Besides non-null annotations and custom taglets, this bundle contains the [MySQL](http://www.mysql.com), [PostgreSQL](http://www.postgresql.org) and [SQLite](http://www.sqlite.org) [JDBC](http://www.oracle.com/technetwork/java/overview-141217.html) drivers.)
- **[DigitalID-Client.jar](https://www.digitalid.net/downloads/DigitalID-Client.jar)** (5 MB): Includes all the libraries that are required for the development of applications. (This bundle contains non-null annotations, custom taglets and the [SQLite](http://www.sqlite.org) [JDBC](http://www.oracle.com/technetwork/java/overview-141217.html) driver.)

## Usage

The Digital ID reference implementation can either directly be run as a server or be used as a library to write services and client applications.

### Server

#### Requirements

##### Java Runtime

The reference implementation requires the [Java SE Runtime Environment 7](http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html) or [newer](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html). You can check your current version (and whether you even have Java) with `java -version` on the command line interface of your operating system. (Please note that the version numbering of Java is decimal. All you need is a version of 1.7 or higher.)

##### Domain Name

In order to set up a host on your server, you need to have a registered domain and configure a subdomain with the name 'id' (e.g. id.example.com) that references your server (either as an 'A' or 'CNAME' [record](https://en.wikipedia.org/wiki/List_of_DNS_record_types)). Please note that the address of a host in Digital ID does **not** contain this subdomain (i.e. it is only example.com). The subdomain is only used internally for resolving host addresses but never presented to the user.

##### Port Number

The server should be reachable on the [port number](https://en.wikipedia.org/wiki/Port_(computer_networking)) 1988. Make sure that no firewall or router blocks the access and add corresponding exceptions or [forwarding rules](https://en.wikipedia.org/wiki/Port_forwarding) otherwise.

##### Database

It is highly recommended that you use MySQL as the database on the server due to its superior locking capabilities.
Additionally, PostgreSQL is not yet supported in the current version because of locking issues.

#### Setup

##### Startup

The server can be started from the [command line](https://en.wikipedia.org/wiki/Command-line_interface) with `java -jar DigitalID-Server.jar`, if you are in the right [working directory](https://en.wikipedia.org/wiki/Working_directory). If you start the server for the first time, you need to configure the database (see the last paragraph for recommendations).

##### Handling

Once the server has started up, you enter an infinite loop that looks like this:

```
You have the following options:
–  0: Exit the server.
–  1: Show the version.
–  2: Show the hosts.
–  3: Create a host.
–  4: Export a host.
–  5: Import a host.
–  6: Show the services.
–  7: Reload the services.
–  8: Activate a service.
–  9: Deactivate a service.
– 10: Change a provider.
– 11: Generate tokens.
– 12: Show members.
– 13: Add members.
– 14: Remove members.
– 15: Open a host.
– 16: Close a host.

Execute the option: _
```

You can now choose the option 3 to create the host. Please enter your domain name without the subdomain 'id' (i.e. only example.com). (Please note that it can take several minutes to generate the cryptographic keys during the creation of a host.)

##### Folders

All Digital ID-related files are stored in an invisible folder in the user's home directory with the name `~/.DigitalID`. This directory has the following subfolders:
- **Hosts**: Contains the public and private keys of all the hosts that run on this server. (The keys of the just created host should now be listed in this directory.)
- **Clients**: Contains the client secrets of all the clients on this account. (Secrets that begin with an underscore belong to the client of the corresponding host.)
- **Services**: Contains the implementations of additional services as [JAR files](https://en.wikipedia.org/wiki/JAR_(file_format)).
- **Data**: Contains the database configuration of the server (and also the database files if clients are run with SQLite).
- **Logs**: Contains the log files of various server (and also client) modules.

##### Certification

Before clients can connect to your host, you need to have its public key certified by [Patria Digitalis](http://www.pd.coop). Please note that this process is not supported yet.

##### Deinstallation

All you have to do to deinstall Digital ID, is to delete the folder `~/.DigitalID`, drop the database and delete `DigitalID-Server.jar`.

### Library

#### Classpath Setup

In order to use the Digital ID reference implementation as a library, download [DigitalID-Client.jar](https://www.digitalid.net/downloads/DigitalID-Client.jar) and add it to the classpath of your favorite [IDE](https://en.wikipedia.org/wiki/Integrated_development_environment).

#### Role Creation

All the actions and queries are performed on [roles](#clients-and-roles). It is easy to create a new [identity](#digital-identities) and get the so-called native role for it:

```java
final @Nonnull Client client = new Client("client_identifier", "Client Name", Image.CLIENT, AgentPermissions.GENERAL_WRITE);
final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("user@example.com");
final @Nonnull NativeRole role = client.openAccount(identifier, Category.NATURAL_PERSON);
```

Instead of creating a new identity, you can also request accreditation for an existing identity:

```java
final @Nonnull Client client = new Client("client_identifier", "Client Name", Image.CLIENT, AgentPermissions.GENERAL_WRITE);
final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("user@example.com");
final @Nonnull InternalNonHostIdentity identity = identifier.getIdentity();
final @Nonnull NativeRole role = client.accredit(identity, "password");
```

#### Concept Retrieval

Once you have such a role, it is easy to retrieve a [concept](#concepts) of that role like an [attribute](#attributes-and-certificates) or a [context](#contacts-and-contexts):

```java
final @Nonnull Attribute attribute = Attribute.get(role, AttributeType.NAME);
final @Nonnull Context context = Context.getRoot(role); // Subcontexts are not yet supported.
```

#### Attribute Caching

The [attributes](#attributes-and-certificates) of other [identities](#digital-identities) are cached locally and can be retrieved as follows:

```java
final @Nonnull InternalNonHostIdentifier identifier = new InternalNonHostIdentifier("friend@example.com");
final @Nonnull Block block = Cache.getFreshAttributeContent(identifier.getIdentity(), role, AttributeType.NAME, false);
```

#### Block Wrapping

Digital ID uses an own encoding which is called the Extensible Data Format (XDF). Values are wrapped into blocks and can be unwrapped as follows:

```java
final @Nonnull String string = new StringWrapper(block).getString(); // Decoding
final @Nonnull Block block = new StringWrapper(AttributeType.NAME, string).toBlock(); // Encoding
```

Please note that the syntax might change in a [future release](#roadmap).

#### Event Listening

Currently, you can observe [concepts](#concepts) for certain aspects and be notified when a value changes:

```java
attribute.observe(this, Attribute.VALUE, Attribute.VISIBILITY, Attribute.RESET); // Adding this object as a listener
attribute.unobserve(this, Attribute.VALUE, Attribute.VISIBILITY, Attribute.RESET); // Removing this object as a listener
```

In order for this to work, the class of this object needs to implement the Observer interface as follows:

```java
class Example implements Observer {
    @Override
    public void notify(@Nonnull Aspect aspect, @Nonnull Instance instance) {
		if (aspect == Attribute.VALUE || aspect == Attribute.RESET) {
            // Do with the instance whose value changed or was reset whatever you want here.
        }
    }
}
```

However, this mechanism will be replaced by a [JavaFX-like](http://docs.oracle.com/javafx/2/binding/jfxpub-binding.htm) property paradigm in a [future release](#roadmap).

#### Database Locking

As the [synchronization](#synchronization) of actions is handled concurrently by separate threads and the locking of SQLite is very primitive, it needs to be done in the client code. Make sure that you **always** acquire the lock before accessing the database as follows:

```java
try {
    Database.lock();
    // Access the database.
	Database.commit();
} catch (@Nonnull SQLException exception) {
    // Handle the error here.
    Database.rollback();
} finally {
    Database.unlock();
}
```

Whether the database is accessed by a method or its called methods is indicated by the annotations `@Committing` and `@NonCommitting`. As their names suggest, they also declare whether the current transaction is committed by the method or not, which might not be desirable if the whole transaction should be rolled back on failure. Moreover, a method that is declared to be `@Committing` guarantees that the transaction is in a committed (or rolled back) state when the method is left without throwing an exception. If the last method in the try-block is committing, then the explicit commit at its end can be omitted.

Furthermore, methods usually indicate whether they expect the database to be locked already (with `@Locked`) or whether this may not be the case in order to prevent deadlocks (with `@NonLocked`). In all other cases (i.e. where the method is `@Committing` or `@NonCommitting` but does not require a certain locking state), the method handles the locking itself. (Please note that the lock is re-entrant for the thread that currently holds it so it does not matter whether the lock is already held or not.)

#### Other Annotations

You find dozens more annotations in the package `net.digitalid.core.annotations`, which should largely be self-explanatory with their corresponding [documentation](#documentation). A design pattern that is used so extensively throughout the code so that it is worth to be mentioned here is `Freezable`. Objects that implement this interface can be changed until you `freeze()` them. From that point onwards they are immutable and can be shared among objects and threads without running into consistency problems. As you would expect by now, there are annotations to indicate whether the object denoted by a variable or parameter is `@Frozen` or `@NonFrozen`. Independent of their freezing state, such objects can be exposed through a `Readonly` interface (that only includes the methods that are `@Pure`).

#### Code Debugging

The reference implementation loosely follows the [design by contract](https://en.wikipedia.org/wiki/Design_by_contract) methodology. You can have the contracts checked by enabling assertions with `java -enableassertions -jar DigitalID-Client.jar ...` or `java -ea -jar DigitalID-Client.jar ...` for short, which is highly recommended during debugging.

## Overview

The following sections give you a short overview of the most important concepts and principles of Digital ID.

### Concepts

#### Digital Identities

Users – natural and artificial persons alike – choose a trusted server to host their digital identity. Every digital identity has a globally dereferenceable identifier that consists of the host address and a user name. The host stores for each virtual identity its attributes and certificates, contacts and contexts, clients and roles.

#### Attributes and Certificates

Attributes are name-value pairs that are associated with a VID. Each attribute has an access policy that determines its visibility towards other virtual identities. The attribute type specifies the format and the semantics of the given value.
Authorities can confirm the correctness of attributes. This does not only increase their credibility, but certificates can also be used for attribute-based access control.

#### Contacts and Contexts

Contacts are references to other VIDs and can be handled efficiently by means of hierarchical contexts. The contact relation is asymmetric and private (i.e. without notification).

#### Clients and Roles

Being just a protocol without a user interface, Virtual ID can only be accessed by clients. You accredit clients to manage your VID and to assume your VID towards others VIDs. Their authorization can be restricted regarding the contexts and attributes that they can read or request from contacts.
You can authorize other VIDs to act in a limited role on your behalf, which is especially useful in case of artificial persons.

### Architecture

### Cryptography

### Synchronization

## Documentation

You find the [Javadoc](http://en.wikipedia.org/wiki/Javadoc) of all public classes and methods at www.digitalid.net/documentation/.

## Roadmap

The current version of the Digital ID reference implementation is 0.6. The following features are planned for major future releases:
- **0.7**: Make the library more consistent and easy to use. Implement a uniform mechanism to change and observe properties of concepts, which includes modifications to the database modules. Improve the error handling and logging.
- **0.8**: Implement the action pusher (which is required for access requests and role issuances) and the certificaton module (so that the public keys of other hosts can be certified). Also support the shortening of credentials.
- **0.9**: Implement the contacts and contexts modules, including their authentications and permissions. Support context-based access requests and role issuances, including an own database module to handle the read receipts.
- **1.0**: Implement the tokens, errors and members modules. Implement the remaining console options. Do extensive integration testing of various features like relocation, merging, exporting and importing of hosts, etc.

## Authors

The reference implementation was written by:
- Kaspar Etter (kaspar.etter@digitalid.net)

## Contact

Don not hesitate to contact us at:
- Information: info@digitalid.net
- Support: help@digitalid.net

## License

All rights reserved. The code will be published under an open-source/commercial double-license later on.
