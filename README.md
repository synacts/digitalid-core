# Digital ID Reference Implementation

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

#### Setup

In order to use the Digital ID reference implementation as a library, download [DigitalID-Client.jar](https://www.digitalid.net/downloads/DigitalID-Client.jar) and add it to the classpath of your favorite [IDE](https://en.wikipedia.org/wiki/Integrated_development_environment).

#### Roles

All the actions and queries are performed on [roles](#clients-and-roles). It is easy to create a new [identity](#digital-identities) and get the so-called native role for it:

```java
final @Nonnull NativeRole role = Main.getClient().openAccount(identifier, Category.NATURAL_PERSON);
```

#### Concepts



#### Locking

Due to concurrency of the synchronization process, ...

#### Annotations

#### Event Handling

Currently, you can observe concepts for certain aspects and be notified when a value changes:

```java
Code
```

However, this mechanism will be replaced by a [JavaFX-like](http://docs.oracle.com/javafx/2/binding/jfxpub-binding.htm) property paradigm in a [future release](#roadmap).

## Overview

What you need to know.

### Concepts

#### Digital Identities

#### Attributes and Certificates

#### Contacts and Contexts

#### Clients and Roles

### Architecture

### Cryptography

## Documentation

Where to find more detailed instructions: www.digitalid.net

## Roadmap

Which features will be released in which version.

## Authors

- Kaspar Etter (kaspar.etter@digitalid.net)

## Contact

- Information: info@digitalid.net
- Support: help@digitalid.net

## License

All rights reserved. The code will be available under an open-source/commercial double-license later on.
