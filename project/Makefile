.PHONY: libsysinfo/libsysinfo.so

MODULES = sysInfo cpuInfo pciInfo usbInfo diskInfo memInfo
HEADERS = $(MODULES:=.h)

JAR_DIR := lib
JFC_JAR := $(JAR_DIR)/jfreechart-1.5.4.jar:$(JAR_DIR)/jcommon-1.0.24.jar

JAVA_SOURCES := $(wildcard *.java) $(wildcard systeminfo/*.java)

MYPROJECT := template

all: $(HEADERS) libsysinfo/libsysinfo.so compile_java

compile_java:
	@echo "Compiling Java sources with JFreeChart 1.5.4..."
	javac -cp $(JFC_JAR) $(JAVA_SOURCES)

$(HEADERS): %.h: %.java
	javac -h . $<

libsysinfo/libsysinfo.so:
	$(MAKE) -C libsysinfo

