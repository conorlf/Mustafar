#include <stdio.h>
#include <string.h>
#include <iostream>
#include <sstream>
#include <string>
#include <unistd.h>
#include <stdlib.h>
#include <ctype.h>
#include <climits>  // Add this

#include "cpuInfo.h"

using namespace std;

struct CPUStat {
    long long user;    // Change to long long to prevent overflow
    long long system;
    long long idle;
};

class CPUInfo
{
public:
    CPUInfo() {
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
    CPUStat _currStat[_maxCores];  // Store as long long but return as int
};

CPUInfo cpu;

// ... _stoi and _parseInfo remain the same ...

void CPUInfo::_parseStat (char buffer[])
{
    if (strncmp (buffer, "cpu", 3))
        return;
        
    int core = -1;
    if (buffer[3] == ' ') {
        core = 0;  // System-wide CPU uses core 0
    } else {
        core = buffer[3] - '0';
    }
    
    if (core < 0 || core >= _maxCores)
        return;

    // Find first digit
    char *p = buffer;
    while (*p && !isdigit(*p)) p++;

    CPUStat stat;
    stat.user = strtoll(p, &p, 10);      // Use strtoll for 64-bit
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
        if (user_delta >= 0 && user_delta < INT_MAX) 
            _currStat[core].user = user_delta;
        else
            _currStat[core].user = 0;
            
        if (system_delta >= 0 && system_delta < INT_MAX) 
            _currStat[core].system = system_delta;
        else
            _currStat[core].system = 0;
            
        if (idle_delta >= 0 && idle_delta < INT_MAX) 
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

// ... rest of your code (read function and JNI) remains the same ...

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

