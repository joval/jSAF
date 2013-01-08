# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

TOP=../..

include $(TOP)/common.mk

CLASSPATH="$(CLASSLIB)$(CLN)$(LIB)$(CLN)$(PROVIDER_DEPS)$(CLN)$(SRC)"

include classes.mk

CLASS_FILES:=$(foreach class, $(CLASSES), $(BUILD)/$(subst .,/,$(class)).class)
PACKAGES=$(sort $(basename $(CLASSES)))
PACKAGEDIRS=$(subst .,/,$(PACKAGES))

all: $(FACADE_LIB)

$(FACADE_LIB): classes resources
	$(JAR) cvf $@ -C $(BUILD)/ .

$(DOCS): $(GEN)
	$(JAVA_HOME)/bin/javadoc -J-Xmx512m -d $(DOCS) -classpath $(CLASSPATH) -subpackages jsaf.identity:jsaf.intf \
	$(SRC)/jsaf/provider/SessionException.java \
	$(SRC)/jsaf/provider/SessionFactory.java \
	$(SRC)/jsaf/provider/windows/Timestamp.java	\
	$(SRC)/jsaf/provider/windows/powershell/PowershellException.java \
	$(SRC)/jsaf/provider/windows/registry/RegistryException.java \
	$(SRC)/jsaf/provider/windows/wmi/WmiException.java

clean:
	rm -rf $(BUILD)
	rm -rf $(DOCS)
	rm -f $(FACADE_LIB)

resources:
	rm -f $(BUILD)/*.properties
	cp $(RSRC)/jsafmsg_en.properties $(BUILD)

classes: classdirs $(CLASS_FILES)

classdirs: $(foreach pkg, $(PACKAGEDIRS), $(BUILD)/$(pkg)/)

$(BUILD)/%.class: $(SRC)/%.java
	$(JAVAC) $(JAVACFLAGS) -d $(BUILD) -classpath $(CLASSPATH) $<

$(BUILD)/%/:
	mkdir -p $(subst PKG,,$@)
