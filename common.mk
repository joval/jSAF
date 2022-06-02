# Copyright (C) 2011-2020 JovalCM.com.  All rights reserved.
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
  else ifeq (Darwin, $(findstring Darwin,$(OS)))
    PLATFORM=mac
  endif
  CLN=:
endif
ifeq (win, $(PLATFORM))
    JAVA=$(TOP)/cygwin.sh "$(JAVA_HOME)/bin/java"
    JAR=$(TOP)/cygwin.sh "$(JAVA_HOME)/bin/jar"
    JAVADOC=$(TOP)/cygwin.sh "$(JAVA_HOME)/bin/javadoc"
    JAVAC=$(TOP)/cygwin.sh "$(JAVA_HOME)/bin/javac"
    NUMPROCS=1
else
    JAVA=$(JAVA_HOME)/bin/java
    JAR=$(JAVA_HOME)/bin/jar
    JAVADOC=$(JAVA_HOME)/bin/javadoc
    JAVAC=$(JAVA_HOME)/bin/javac
    ifeq (mac, $(PLATFORM))
        NUMPROCS=$(shell sysctl -n hw.ncpu)
    else
        NUMPROCS=$(shell nproc)
    endif
endif
RAW_JAVA_VERSION:=$(shell $(JAVA) -version 2>&1)
ifeq (18, $(findstring 18, $(findstring "18., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=18
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (17, $(findstring 17, $(findstring "17., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=17
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (16, $(findstring 16, $(findstring "16., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=16
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (15, $(findstring 15, $(findstring "15., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=15
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (14, $(findstring 14, $(findstring "14., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=14
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (13, $(findstring 13, $(findstring "13., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=13
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (12, $(findstring 12, $(findstring "12., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=12
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (11, $(findstring 11, $(findstring "11., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=11
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (10, $(findstring 10, $(findstring "10., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=10
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (9, $(findstring 9, $(findstring "9., $(RAW_JAVA_VERSION))))
    JAVA_VERSION=9
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (1.8, $(findstring 1.8, $(RAW_JAVA_VERSION)))
    JAVA_VERSION=1.8
    JAVADOCFLAGS=-Xdoclint:none -J-Xmx512m
else ifeq (1.7, $(findstring 1.7, $(RAW_JAVA_VERSION)))
    JAVA_VERSION=1.7
    JAVADOCFLAGS=-J-Xmx512m
else ifeq (1.6, $(findstring 1.6, $(RAW_JAVA_VERSION)))
    JAVA_VERSION=1.6
    JAVADOCFLAGS=-J-Xmx512m
else ifeq ("1., $(findstring "1., $(RAW_JAVA_VERSION)))
    $(error "Unsupported older Java version: $(RAW_JAVA_VERSION)")
else
    $(error "Unsupported Java version: $(RAW_JAVA_VERSION)")
endif

NULL:=
SPACE:=$(NULL) # end of the line

Default: all

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
