.PHONY: libsysinfo/libsysinfo.so

# List of JNI-enabled Java modules that need header generation.
MODULES=sysInfo cpuInfo pciInfo usbInfo diskInfo memInfo
HEADERS=$(MODULES:=.h)

# JFreeChart and JCommon (installed via apt)
JFC_PATH=/usr/share/java
JFC_JAR=$(JFC_PATH)/jfreechart.jar:$(JFC_PATH)/jcommon.jar

# Java source files (include systeminfo folder)
JAVA_SOURCES=$(wildcard *.java) $(wildcard systeminfo/*.java)

# Main project entry (template.java)
MYPROJECT=template

all: $(HEADERS) libsysinfo/libsysinfo.so compile_java

compile_java:
	javac -cp $(JFC_JAR) $(JAVA_SOURCES)

$(HEADERS): %.h: %.java
	javac -h . $<

libsysinfo/libsysinfo.so:
	$(MAKE) -C libsysinfo
