# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

ifeq (x, x$(JAVA_HOME))
    $(error "Please set the JAVA_HOME environment variable.")
endif
PLATFORM=unknown
ifeq (Windows, $(findstring Windows,$(OS)))
  PLATFORM=win
  CLN=;
else
  OS=$(shell uname)
  ifeq (Linux, $(findstring Linux,$(OS)))
    PLATFORM=linux
  endif
  CLN=:
endif
ifeq (win, $(PLATFORM))
    JAVA=$(TOP)/cygwin.sh $(JAVA_HOME)/bin/java
    JAR=$(TOP)/cygwin.sh $(JAVA_HOME)/bin/jar
    JAVADOC=$(TOP)/cygwin.sh $(JAVA_HOME)/bin/javadoc
    JAVAC=$(TOP)/cygwin.sh $(JAVA_HOME)/bin/javac
    CLASSLIB=$(shell cygpath -w $(JAVA_HOME))\jre\lib\rt.jar
else
    JAVA=$(JAVA_HOME)/bin/java
    JAR=$(JAVA_HOME)/bin/jar
    JAVADOC=$(JAVA_HOME)/bin/javadoc
    JAVAC=$(JAVA_HOME)/bin/javac
    CLASSLIB=$(JAVA_HOME)/jre/lib/rt.jar
endif
RAW_JAVA_VERSION:=$(shell $(JAVA_HOME)/bin/java -version 2>&1)
ifeq (1.8, $(findstring 1.8, $(RAW_JAVA_VERSION)))
    JAVA_VERSION=1.8
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (1.7, $(findstring 1.7, $(RAW_JAVA_VERSION)))
    JAVA_VERSION=1.7
    JAVADOCFLAGS=-J-Xmx512m
else ifeq (1.6, $(findstring 1.6, $(RAW_JAVA_VERSION)))
    JAVA_VERSION=1.6
    JAVADOCFLAGS=-J-Xmx512m
else
    $(error "Unsupported Java version: $(RAW_JAVA_VERSION)")
endif

NULL:=
SPACE:=$(NULL) # end of the line

Default: all

CLASSLIB=$(JAVA_HOME)/jre/lib/rt.jar
BUILD=build
DIST=dist
RSRC=rsrc
DOCS=docs
SRC=$(TOP)/src
COMPONENTS=$(TOP)/components
LIBDIR=$(RSRC)/lib
LIBS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard $(LIBDIR)/*)))

FACADE=$(COMPONENTS)/facade
FACADE_LIB=$(FACADE)/jSAF.jar
FACADE_DEPS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard $(FACADE)/$(LIBDIR)/*)))
