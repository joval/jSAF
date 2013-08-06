# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

include $(TOP)/customize.mk

Default: all

PLATFORM=unknown
ifeq (Windows, $(findstring Windows,$(OS)))
    PLATFORM=win
    CLN=;
    JAVACFLAGS=-Xlint:unchecked
else
    OS=$(shell uname)
    CLN=:
endif

ifeq (Linux, $(findstring Linux,$(OS)))
    PLATFORM=linux
endif

NULL:=
SPACE:=$(NULL) # end of the line
SHELL=/bin/sh
CWD=$(shell pwd)

JAVA=$(JAVA_HOME)/bin/java
JAVA_VERSION=1.6
ifeq (1.7, $(findstring 1.7,`$(JAVA) -version`))
    JAVA_VERSION=1.7
endif
ifeq (x, x$(JRE_HOME))
    JRE_HOME=$(JAVA_HOME)/jre
endif
JRE=$(JRE_HOME)/bin/java

JAVAC=$(JAVA_HOME)/bin/javac
JAR=$(JAVA_HOME)/bin/jar
CLASSLIB=$(JAVA_HOME)/jre/lib/rt.jar
BUILD=build
DIST=dist
RSRC=rsrc
DOCS=docs/api
SRC=$(TOP)/src
COMPONENTS=$(TOP)/components
LIBDIR=$(RSRC)/lib
LIBS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard $(LIBDIR)/*)))

CSC=powershell.exe -File $(TOP)/tools/CSharpCompiler.ps1

FACADE=$(COMPONENTS)/facade
FACADE_LIB=$(FACADE)/jSAF.jar
FACADE_DEPS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard $(FACADE)/$(LIBDIR)/*)))
PROVIDER=$(COMPONENTS)/provider
PROVIDER_LIB=$(PROVIDER)/jSAF-Provider.jar
PROVIDER_DEPS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard $(PROVIDER)/$(LIBDIR)/*)))
