/*
 *  USB information class.  Executes lsusb and parses output into arrays of buses and
 *  devices.  Parsing is now robust and does not rely on fixed substring positions.
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
    int deviceCount = 0;
    USBDevice device[maxDevice];
};

class USBInfo
{
public:
    USBInfo() : _busCount(0) {}
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
    int _xtoi (const string& s, int idefault);
};

USBInfo usb;

// Convert string to int, safely handles hex (with or without 0x)
int USBInfo::_xtoi(const string& s, int idefault) {
    try {
        size_t idx;
        int ival = stoi(s, &idx, 16);
        return ival;
    } catch (...) {
        return idefault;
    }
}

// Safe parser for lsusb output
void USBInfo::_parseDevice(char buffer[]) {
    string line = buffer;
    istringstream iss(line);

    string tmp, busStr, deviceStr, idStr;
    if (!(iss >> tmp >> busStr >> tmp >> deviceStr >> tmp >> idStr)) {
        // Line does not match expected format
        return;
    }

    // Remove possible trailing colon from device number
    if (!deviceStr.empty() && deviceStr.back() == ':')
        deviceStr.pop_back();

    int bus = stoi(busStr, nullptr, 10);
    int device = stoi(deviceStr, nullptr, 10);

    auto pos = idStr.find(':');
    if (pos == string::npos)
        return; // unexpected ID format

    string vendorStr = idStr.substr(0, pos);
    string productStr = idStr.substr(pos + 1);

    _bus[bus].device[device].vendor = _xtoi(vendorStr, 0);
    _bus[bus].device[device].product = _xtoi(productStr, 0);

    if (bus > _busCount)
        _busCount = bus;
    if (device > _bus[bus].deviceCount)
        _bus[bus].deviceCount = device;
}

void USBInfo::read() {
    std::array<char, 4096> buffer;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen("lsusb", "r"), pclose);

    if (!pipe) {
        throw std::runtime_error("Failed to execute lsusb!");
    }

    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe.get()) != nullptr)
        _parseDevice(buffer.data());
}

// JNI wrappers
JNIEXPORT void JNICALL Java_usbInfo_read(JNIEnv *env, jobject obj) {
    usb.read();
}

JNIEXPORT jint JNICALL Java_usbInfo_busCount(JNIEnv *env, jobject obj) {
    return usb.busCount();
}

JNIEXPORT jint JNICALL Java_usbInfo_deviceCount(JNIEnv *env, jobject obj, jint bus) {
    return usb.deviceCount(bus);
}

JNIEXPORT jint JNICALL Java_usbInfo_vendorID(JNIEnv *env, jobject obj, jint bus, jint device) {
    return usb.vendorID(bus, device);
}

JNIEXPORT jint JNICALL Java_usbInfo_productID(JNIEnv *env, jobject obj, jint bus, jint device) {
    return usb.productID(bus, device);
}
