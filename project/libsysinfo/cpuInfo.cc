/*
 *  CPU information class implementation.  Reads CPU details by executing lscpu
 *  and also reads usage stats.
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
#include <unistd.h>
#include <cstdlib>
#include <cctype>

#include "cpuInfo.h"

using namespace std;

struct CPUStat {
    long long user;    // Changed to long long to prevent overflow
    long long system;
    long long idle;
};

class CPUInfo
{
public:
    CPUInfo() {       // Added constructor to initialize arrays
        for (int i = 0; i < _maxCores; i++) {
            _havePrevStat[i] = false;
            _prevStat[i] = {0, 0, 0};
            _currStat[i] = {0, 0, 0};  // Initialize current stats too
        }
    }
    
    void read(int seconds = 0);
    const char *getModel () { return _model.c_str(); }
    int getCoresPerSocket() { return _coresPerSocket; }
    int getSocketCount() { return _socketCount; }
    int getl1dCacheSize() { return _l1dCacheSize; }
    int getl1iCacheSize() { return _l1iCacheSize; }
    int getl2CacheSize() { return _l2CacheSize; }
    int getl3CacheSize() { return _l3CacheSize; }
    int getStatUser(int core) { return _currStat[core].user; }
    int getStatSystem (int core) { return _currStat[core].system; }
    int getStatIdle (int core) { return _currStat[core].idle; }
private:
    static const int _maxCores = 64;
    string _architecture;
    string _model;
    int _vaddrbits;
    int _paddrbits;
    bool _littleEndian;
    int _threadsPerCore;
    int _coresPerSocket;
    int _socketCount;
    int _l1dCacheSize;
    int _l1iCacheSize;
    int _l2CacheSize;
    int _l3CacheSize;
    int _stoi (string& s, int idefault);
    void _parseInfo (string&, string &);
    void _parseStat (char buffer[]);
    CPUStat _prevStat[_maxCores];
    bool _havePrevStat[_maxCores];
    CPUStat _currStat[_maxCores];
};

CPUInfo cpu;

int CPUInfo::_stoi (string& s, int idefault)
{
    int ival;

    try {
        ival = stoi (s);
    }
    catch(exception &err) {
        ival = idefault;
    }

    return ival;
}

void CPUInfo::_parseInfo (string& key, string &value)
{
    if (key == "Architecture")
        _architecture = value;
    else if (key == "Model name")
        _model = value;
    else if (key == "Byte Order")
        _littleEndian = (value == "Little Endian");  // Fixed: = instead of ==
    else if (key == "Thread(s) per core")
        _threadsPerCore = _stoi (value, 1);
    else if (key == "Core(s) per socket" || key == "Core(s) per cluster")
        _coresPerSocket = _stoi (value, 1);
    else if (key == "Socket(s)")
        _socketCount = _stoi (value, 1);
    else if (key == "L1d cache")
        _l1dCacheSize = _stoi (value, 0);
    else if (key == "L1i cache")
        _l1iCacheSize = _stoi (value, 0);
    else if (key == "L2 cache")
        _l2CacheSize = _stoi (value, 0);
    else if (key == "L3 cache")
        _l3CacheSize = _stoi (value, 0);
}

void CPUInfo::_parseStat (char buffer[])
{
    if (strncmp (buffer, "cpu", 3))
        return;
        
    // FIX: Handle system-wide "cpu " line properly
    int core = -1;
    if (buffer[3] == ' ') {
        core = 0;  // System-wide CPU uses core 0
    } else {
        core = buffer[3] - '0';
    }
    
    if (core < 0 || core >= _maxCores)
        return;

    // FIX: Find the first number in the buffer instead of hardcoded position
    char *p = buffer;
    while (*p && !isdigit(*p)) p++;  // Skip to first digit
    
    CPUStat stat;
    // Use strtoll for more robust parsing (64-bit)
    stat.user = strtoll(p, &p, 10);
    long long nice = strtoll(p, &p, 10);
    stat.system = strtoll(p, &p, 10);
    stat.idle = strtoll(p, &p, 10);
    
    stat.user += nice;
    
    if (_havePrevStat[core]) {
        // Calculate deltas with overflow protection
        long long user_delta = stat.user - _prevStat[core].user;
        long long system_delta = stat.system - _prevStat[core].system;
        long long idle_delta = stat.idle - _prevStat[core].idle;
        
        // Only use positive deltas (handle counter wrap-around)
        if (user_delta >= 0 && user_delta < 10000)  // Reasonable max for 1 second
            _currStat[core].user = user_delta;
        else
            _currStat[core].user = 0;
            
        if (system_delta >= 0 && system_delta < 10000)
            _currStat[core].system = system_delta;
        else
            _currStat[core].system = 0;
            
        if (idle_delta >= 0 && idle_delta < 10000)
            _currStat[core].idle = idle_delta;
        else
            _currStat[core].idle = 0;
            
    } else {
        // First read - initialize with zeros
        _currStat[core].user = 0;
        _currStat[core].system = 0;
        _currStat[core].idle = 0;
    }
    
    _prevStat[core] = stat;
    _havePrevStat[core] = true;
}

void CPUInfo::read(int seconds)
{
    if (seconds)
        sleep (seconds);

    // Only parse lscpu on first call to avoid unnecessary overhead
    static bool firstRead = true;
    if (firstRead) {
        std::array<char, 4096> buffer;
        std::unique_ptr<FILE, decltype(&pclose)> pipe(popen("lscpu -B", "r"), pclose);

        if (pipe) {
            while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe.get()) != nullptr) {
                string line = buffer.data();
                int delim = line.find(':');
                if (delim == string::npos)
                    continue;  // Just skip malformed lines
                string key = line.substr (0, delim);
                delim++;
                while (line[delim] == ' ')
                    delim++;
                string value = line.substr (delim, string::npos);
                // Remove trailing newline
                if (!value.empty() && value[value.length()-1] == '\n')
                    value.erase(value.length()-1);
                _parseInfo (key, value);
            }
        }
        firstRead = false;
    }

    std::array<char, 4096> buffer;
    std::unique_ptr<FILE, decltype(&fclose)> stat(fopen("/proc/stat", "r"), fclose);

    if (!stat) {
        throw std::runtime_error("Failed to open /proc/stat !");
    }

    while (fgets(buffer.data(), static_cast<int>(buffer.size()), stat.get()) != nullptr)
        _parseStat (buffer.data());
}

// Wrap JNI functions with extern "C" to fix linking
extern "C" {

JNIEXPORT void JNICALL Java_cpuInfo_read__ (JNIEnv *env, jobject obj) {
    cpu.read();
}

JNIEXPORT void JNICALL Java_cpuInfo_read__I (JNIEnv *env, jobject obj, jint seconds) {
    cpu.read(seconds);
}

JNIEXPORT jint JNICALL Java_cpuInfo_coresPerSocket (JNIEnv *env, jobject obj) {
   return cpu.getCoresPerSocket();
}

JNIEXPORT jint JNICALL Java_cpuInfo_socketCount (JNIEnv *env, jobject obj) {
   return cpu.getSocketCount();
}

JNIEXPORT jint JNICALL Java_cpuInfo_l1dCacheSize (JNIEnv *env, jobject obj) {
   return cpu.getl1dCacheSize();
}

JNIEXPORT jint JNICALL Java_cpuInfo_l1iCacheSize (JNIEnv *env, jobject obj) {
   return cpu.getl1iCacheSize();
}

JNIEXPORT jint JNICALL Java_cpuInfo_l2CacheSize (JNIEnv *env, jobject obj) {
   return cpu.getl2CacheSize();
}

JNIEXPORT jint JNICALL Java_cpuInfo_l3CacheSize (JNIEnv *env, jobject obj) {
   return cpu.getl3CacheSize();
}

JNIEXPORT jstring JNICALL Java_cpuInfo_getModel (JNIEnv *env, jobject obj)
{
    jstring result = env->NewStringUTF(cpu.getModel());
    return result;
}

JNIEXPORT jint JNICALL Java_cpuInfo_getUserTime (JNIEnv *env, jobject obj, jint core) {
   return cpu.getStatUser (core);
}

JNIEXPORT jint JNICALL Java_cpuInfo_getIdleTime (JNIEnv *env, jobject obj, jint core) {
   return cpu.getStatIdle (core);
}

JNIEXPORT jint JNICALL Java_cpuInfo_getSystemTime (JNIEnv *env, jobject obj, jint core) {
   return cpu.getStatSystem (core);
}

} // extern "C"