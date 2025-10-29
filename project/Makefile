.PHONY: all clean libsysinfo/libsysinfo.so run

MODULES = sysInfo cpuInfo pciInfo usbInfo diskInfo memInfo
HEADERS = $(MODULES:=.h)

JAR_DIR := lib
# Use the version you actually downloaded (update if needed)
JFC_JAR := $(JAR_DIR)/jfreechart-1.5.4.jar:$(JAR_DIR)/jcommon-1.0.24.jar

JAVA_SOURCES := $(wildcard *.java) $(wildcard systeminfo/*.java)

MYPROJECT := template

# Default target
all: $(HEADERS) libsysinfo/libsysinfo.so compile_java

# Compile native headers (JNI)
$(HEADERS): %.h: %.java
	javac -h . $<

# Build libsysinfo native library
libsysinfo/libsysinfo.so:
	$(MAKE) -C libsysinfo

# Compile Java sources
compile_java:
	javac -cp $(JFC_JAR):. $(JAVA_SOURCES)

# Run the program
run: all
	java -cp $(JFC_JAR):. -Djava.library.path=libsysinfo $(MYPROJECT)
