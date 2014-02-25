# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

TOP=../..

include $(TOP)/common.mk

PROVIDER_LIBS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard provider/*)))

CLASSPATH=$(CLASSLIB)$(CLN)$(FACADE_LIB)$(CLN)$(FACADE_DEPS)$(CLN)$(SRC)
RUNTIMECP="$(CLASSPATH)$(CLN).$(CLN)$(PROVIDER_LIBS)"

CLASS_FILE=WinExample.class

all: $(CLASS_FILE)

clean:
	rm -r *.class

%.class: %.java
	$(JAVAC) $(JAVACFLAGS) -classpath "$(CLASSPATH)" $<

test:
	$(JRE) -classpath $(RUNTIMECP) WinExample
