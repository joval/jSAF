# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

include $(TOP)/../DeveloperTools/install/common.mk

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

CSC=powershell.exe -File $(TOP)/tools/CSharpCompiler.ps1

FACADE=$(COMPONENTS)/facade
FACADE_LIB=$(FACADE)/jSAF.jar
FACADE_DEPS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard $(FACADE)/$(LIBDIR)/*)))
