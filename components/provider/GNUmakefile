# Copyright (C) 2011 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

TOP=../..

include $(TOP)/common.mk

API=$(FACADE_DEPS)$(CLN)$(FACADE_LIB)
CLASSPATH="$(CLASSLIB)$(CLN)$(API)$(CLN)$(LIBS)$(CLN)$(SRC)"
RUNTIMECP="$(CLASSLIB)$(CLN)$(API)$(CLN)$(LIBS)$(CLN)$(PROVIDER_LIB)"

include classes.mk

CLASS_FILES:=$(foreach class, $(CLASSES), $(BUILD)/$(subst .,/,$(class)).class)
PACKAGES=$(sort $(basename $(CLASSES)))
PACKAGEDIRS=$(subst .,/,$(PACKAGES))

all: $(PROVIDER_LIB)

$(PROVIDER_LIB): classes resources
	$(JAR) cvf $@ -C $(BUILD)/ .

test:
	$(JRE) -Djava.library.path=$(LIBDIR) -classpath $(RUNTIMECP) jsaf.test.Default test.properties

clean:
	rm -rf $(BUILD)
	rm -f $(PROVIDER_LIB)

resources:
	rm -f $(BUILD)/*.ini
	cp $(RSRC)/session.ini $(BUILD)
	rm -f $(BUILD)/jsaf/provider/windows/identity/*.psm1
	cp $(SRC)/jsaf/provider/windows/identity/*.psm1 $(BUILD)/jsaf/provider/windows/identity/
	rm -f $(BUILD)/jsaf/provider/windows/io/*.psm1
	cp $(SRC)/jsaf/provider/windows/io/*.psm1 $(BUILD)/jsaf/provider/windows/io/
	rm -f $(BUILD)/jsaf/provider/windows/powershell/*.psm1
	cp $(SRC)/jsaf/provider/windows/powershell/*.psm1 $(BUILD)/jsaf/provider/windows/powershell/
	rm -f $(BUILD)/jsaf/provider/windows/registry/*.psm1
	cp $(SRC)/jsaf/provider/windows/registry/*.psm1 $(BUILD)/jsaf/provider/windows/registry/

classes: classdirs $(CLASS_FILES)

classdirs: $(foreach pkg, $(PACKAGEDIRS), $(BUILD)/$(pkg)/)

$(BUILD)/%.class: $(SRC)/%.java
	$(JAVAC) $(JAVACFLAGS) -d $(BUILD) -classpath $(CLASSPATH) $<

$(BUILD)/%/:
	mkdir -p $(subst PKG,,$@)
