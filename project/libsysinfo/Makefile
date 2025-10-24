MODULES=sysInfo cpuInfo pciInfo usbInfo diskInfo memInfo
SOURCES=$(MODULES:=.cc)
HEADERS=$(MODULES:%=../%.h)

#  Location of openjdk headers.  Change this according to your 
#  version and architecture

ARCH := $(shell uname -m)

ifeq ($(ARCH),aarch64)
        ARCH_NAME := arm64
else 
        ARCH_NAME := amd64
endif

#  arm64 for mac and amd64 for intel
OPENJDK=/usr/lib/jvm/java-21-openjdk-$(ARCH_NAME)/include


libsysinfo.so: $(SOURCES) $(HEADERS)
	g++ -fPIC -I .. -I $(OPENJDK) \
        -I $(OPENJDK)/linux -shared \
        -Wno-ignored-attributes \
        -o libsysinfo.so $(SOURCES)

