
.PHONY: all libsysinfo/libsysinfo.so run

MODULES=sysInfo cpuInfo pciInfo usbInfo diskInfo memInfo
HEADERS=$(MODULES:=.h)
MYPROJECT=template
LIBSYSINFO_DIR=libsysinfo

all: $(HEADERS) $(MYPROJECT).class libsysinfo/libsysinfo.so

$(MYPROJECT).class: $(MYPROJECT).java libsysinfo/libsysinfo.so
        javac -cp "lib/*:." $<

$(HEADERS): %.h: %.java
        javac -h . $<

libsysinfo/libsysinfo.so:
        $(MAKE) -C libsysinfo

run: all
        java -cp ".:lib/*" -Djava.library.path=LIBSYSINFO_DIR SysInfoDashboard


