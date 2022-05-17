# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

TOP=$(realpath ../..)

include $(TOP)/common.mk

PROVIDER_LIBS=$(subst $(SPACE),$(CLN),$(filter %.jar %.zip, $(wildcard provider/*)))

CLASSPATH=$(FACADE_LIB)$(CLN)$(FACADE_DEPS)$(CLN)$(SRC)
RUNTIMECP=".$(CLN)$(PROVIDER_LIBS)"

CLASS_FILES=WinExample.class LinuxExample.class

all: $(CLASS_FILES)

clean:
	rm -r *.class

%.class: %.java
	$(JAVAC) $(JAVACFLAGS) -classpath "$(CLASSPATH)" $<

test:
	$(JAVA) -classpath $(RUNTIMECP) WinExample
