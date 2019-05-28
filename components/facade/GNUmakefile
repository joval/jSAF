# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

TOP=$(realpath ../..)

include $(TOP)/common.mk

CLASSPATH="$(CLASSLIB)$(CLN)$(LIBS)$(CLN)$(SRC)"
RUNTIMECP="$(CLASSLIB)$(CLN)$(LIBS)$(CLN)$(FACADE_LIB)"

include classes.mk

CLASS_FILES:=$(foreach class, $(CLASSES), $(BUILD)/$(subst .,/,$(class)).class)
PACKAGES=$(sort $(basename $(CLASSES)))
PACKAGEDIRS=$(subst .,/,$(PACKAGES))

all: $(FACADE_LIB)

$(FACADE_LIB): classes resources
	$(JAR) cvf $@ -C $(BUILD)/ .

$(DOCS):
	$(JAVADOC) -J-Xmx512m -d $(DOCS) -classpath $(CLASSPATH) -subpackages jsaf

clean:
	rm -rf $(BUILD)
	rm -rf $(DOCS)
	rm -f $(FACADE_LIB)

resources:
	rm -f $(BUILD)/*.properties
	cp $(RSRC)/jsafmsg_en.properties $(BUILD)
	cp $(SRC)/jsaf/service/etc.services $(BUILD)/jsaf/service

classes: classdirs
	$(MAKE) -j$(NUMPROCS) classfiles

classdirs: $(foreach pkg, $(PACKAGEDIRS), $(BUILD)/$(pkg)/)

classfiles: $(CLASS_FILES)

$(BUILD)/%.class: $(SRC)/%.java
	$(JAVAC) $(JAVACFLAGS) -d $(BUILD) -classpath $(CLASSPATH) $<

$(BUILD)/%/:
	mkdir -p $(subst PKG,,$@)
