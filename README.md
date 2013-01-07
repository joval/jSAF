jSAF&trade;: Java&trade; System Access Facade
=============

jSAF is a set of Java interfaces that provide abstractions to computer system access functions, including process execution, filesystem access, and access to platform-specific constructs (in particular on Windows: the registry, WMI and Powershell). The principle behind jSAF is that it is desirable to create business logic for automation on top of a facade that abstracts the underlying implementation of system access -- be it implemented locally, using built-in remote-access protocols like SSH, or by an agent process running on a remote system.

A reference implementation of a local provider is included.  jSAF was implemented as part of the jOVAL&trade; project.

For more information on the project, visit http://joval.org.
