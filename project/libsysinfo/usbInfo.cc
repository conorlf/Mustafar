/*
 *  USB information class.  Executes lsusb and parses output into arrays of buses and
 *  devices.  Parsing is crude and just expects particular fields to be at particular
 *  locations in the output.
 *
 *  Copyright (c) 2024 Mark Burkley (mark.burkley@ul.ie)
 */

#include <cstdio>
#include <cstring>
#include <iostream>
#include <memory>
#include <sstream>
#include <string>
#include <array>
#include <cassert>
#include <unistd.h>

#include "usbInfo.h"

using namespace std;

struct USBDevice
{
    unsigned vendor;
    unsigned product;
};

struct USBBus
{
    static const int maxDevice = 32;
    int deviceCount;
    USBDevice device[maxDevice];
};

class USBInfo
{
public:
    void read();
    int busCount () { return _busCount; }
    int deviceCount (int bus) { return _bus[bus].deviceCount; }
    int vendorID (int bus, int device) { return _bus[bus].device[device].vendor; }
    int productID (int bus, int device) { return _bus[bus].device[device].product; }
    static const int maxBus = 64;

private:
    int _busCount;
    USBBus _bus[maxBus];
    void _parseDevice (char buffer[]);
    int _xtoi (string& s, int idefault);
};

USBInfo usb;

int USBInfo::_xtoi (string& s, int idefault)
{
    int ival;

    try {
        ival = stoi (s, 0, 16);
    }
    catch(exception &err) {
        ival = idefault;
    }

    return ival;
}

void USBInfo::_parseDevice(char buffer[]) {
    string line = buffer;
    istringstream iss(line);
    string tmp, busStr, deviceStr, vidPid;

    // Attempt to read the main tokens
    if (!(iss >> tmp >> busStr >> tmp >> deviceStr >> tmp >> vidPid))
        return; // malformed line, ignore

    // Remove trailing colon from device number
    if (!deviceStr.empty() && deviceStr.back() == ':')
        deviceStr.pop_back();

    // Safely parse bus and device numbers
    int bus = 0, device = 0;
    try {
        bus = stoi(busStr);
        device = stoi(deviceStr);
    } catch (...) {
        return; // ignore malformed numbers
    }

    // Check that vidPid contains ':' before splitting
    auto pos = vidPid.find(':');
    if (pos == string::npos || pos + 1 >= vidPid.size())
        return; // malformed ID, ignore

    string vendorStr = vidPid.substr(0, pos);
    string productStr = vidPid.substr(pos + 1);

    // Assign vendor/product safely
    _bus[bus].device[device].vendor = _xtoi(vendorStr, 0);
    _bus[bus].device[device].product = _xtoi(productStr, 0);

    // Update counts
    if (bus > _busCount)
        _busCount = bus;
    if (device > _bus[bus].deviceCount)
        _bus[bus].deviceCount = device;
}


void USBInfo::read()
{

     // CLEAR OLD DATA FIRST
    _busCount = 0;
    for (int i = 0; i < maxBus; i++) {
        _bus[i].deviceCount = 0;
        // Optionally clear individual device data too
        for (int j = 0; j < USBBus::maxDevice; j++) {
            _bus[i].device[j].vendor = 0;
            _bus[i].device[j].product = 0;
        }
    }

    std::array<char, 4096> buffer;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen("lsusb", "r"), pclose);

    if (!pipe) {
        throw std::runtime_error("Failed to execute lsusb!");
    }

    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe.get()) != nullptr)
        _parseDevice (buffer.data());
}

JNIEXPORT void JNICALL Java_usbInfo_read (JNIEnv *env, jobject obj) {
    usb.read();
}

JNIEXPORT jint JNICALL Java_usbInfo_busCount (JNIEnv *env, jobject obj) {
    return usb.busCount ();
}

JNIEXPORT jint JNICALL Java_usbInfo_deviceCount (JNIEnv *env, jobject obj, jint bus) {
    return usb.deviceCount (bus);
}

JNIEXPORT jint JNICALL Java_usbInfo_vendorID (JNIEnv *env, jobject obj, jint bus, jint device) {
    return usb.vendorID (bus, device);
}

JNIEXPORT jint JNICALL Java_usbInfo_productID (JNIEnv *env, jobject obj, jint bus, jint device) {
    return usb.productID (bus, device);
}

