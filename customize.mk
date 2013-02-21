# Copyright (C) 2013 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
# INSTRUCTIONS:
#
# This file is intended to be modified to suit your build environment.
# $(TOP) is defined to be this directory -- the top-level directory for the jSAF source tree.
#

#
# JAVA_HOME is where you've installed your JDK.
#
JAVA_HOME=$(TOP)/../../tools/jdk1.6.0_26
#JAVA_HOME=$(TOP)/../../tools/jdk1.7.0_03

#
# JRE_HOME is the install path for the JRE that will be used to run the test target. On Windows it must match
# the platform architecture (i.e., 64-bit JRE on 64-bit Windows).
#
#JRE_HOME=$(TOP)/../../tools/jre170_03_x64
JRE_HOME=$(TOP)/../../tools/jre170_03_x86
