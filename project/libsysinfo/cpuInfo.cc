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

#include "cpuInfo.h"

using namespace std;

struct CPUStat {
    long user;
    long nice;
    long system;
    long idle;
    long iowait;
    long irq;
    long softirq;
    long steal;
    long guest;
    long guest_nice;
};

class CPUInfo
{
public:
    void read(int seconds = 0);
    const char *getModel () { return _model.c_str(); }
    int getCoresPerSocket() { return _coresPerSocket; }
    int getSocketCount() { return _socketCount; }
    int getl1dCacheSize() { return _l1dCacheSize; }
    int getl1iCacheSize() { return _l1iCacheSize; }
    int getl2CacheSize() { return _l2CacheSize; }
    int getl3CacheSize() { return _l3CacheSize; }
    int getStatUser(int core) { return _userPercent; }
    int getStatSystem (int core) { return _systemPercent; }
    int getStatIdle (int core) { return _idlePercent; }
private:
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
    
    // CPU usage percentages
    int _userPercent;
    int _systemPercent;
    int _idlePercent;
    
    // Previous stats for delta calculation
    CPUStat _prevStat;
    bool _havePrevStat;
    
    int _stoi (string& s, int idefault);
    void _parseInfo (string&, string &);
    bool _parseOverallCpu (const string& line, CPUStat& stat);
    void _calculateUsage(const CPUStat& curr, const CPUStat& prev);
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
    int ival;

    if (key == "Architecture")
        _architecture = value;
    else if (key == "Model name")
        _model = value;
    else if (key == "Byte Order")
        _littleEndian = (value.find("Little Endian") != string::npos);
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

bool CPUInfo::_parseOverallCpu (const string& line, CPUStat& stat)
{
    // Look for the overall "cpu " line (with space)
    if (line.find("cpu ") != 0) 
        return false;

    istringstream stream(line.substr(4)); // Skip "cpu "
    stream >> stat.user >> stat.nice >> stat.system >> stat.idle 
           >> stat.iowait >> stat.irq >> stat.softirq >> stat.steal 
           >> stat.guest >> stat.guest_nice;
           
    return !stream.fail();
}

void CPUInfo::_calculateUsage(const CPUStat& curr, const CPUStat& prev)
{
    // Calculate differences
    long idle = curr.idle - prev.idle;
    long iowait = curr.iowait - prev.iowait;
    long user = curr.user - prev.user;
    long nice = curr.nice - prev.nice;
    long system = curr.system - prev.system;
    long irq = curr.irq - prev.irq;
    long softirq = curr.softirq - prev.softirq;
    
    // Total time spent
    long totalIdle = idle + iowait;
    long totalNonIdle = user + nice + system + irq + softirq;
    long total = totalIdle + totalNonIdle;
    
    // Calculate percentages (avoid division by zero)
    if (total > 0) {
        _idlePercent = (totalIdle * 100) / total;
        _userPercent = ((user + nice) * 100) / total;
        _systemPercent = ((system + irq + softirq) * 100) / total;
        
        // Ensure percentages are sane
        if (_idlePercent < 0) _idlePercent = 0;
        if (_idlePercent > 100) _idlePercent = 100;
        if (_userPercent < 0) _userPercent = 0;
        if (_userPercent > 100) _userPercent = 100;
        if (_systemPercent < 0) _systemPercent = 0;
        if (_systemPercent > 100) _systemPercent = 100;
    } else {
        _idlePercent = 0;
        _userPercent = 0;
        _systemPercent = 0;
    }
}

void CPUInfo::read(int seconds)
{
    if (seconds > 0) {
        sleep(seconds);
    }

    // Read CPU configuration using lscpu
    std::array<char, 4096> buffer;
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen("lscpu", "r"), pclose);

    if (!pipe) {
        throw std::runtime_error("Failed to execute lscpu!");
    }

    while (fgets(buffer.data(), static_cast<int>(buffer.size()), pipe.get()) != nullptr) {
        string line = buffer.data();
        size_t delim = line.find(':');
        if (delim == string::npos)
            continue;
            
        string key = line.substr(0, delim);
        // Skip whitespace after colon
        size_t valueStart = delim + 1;
        while (valueStart < line.length() && (line[valueStart] == ' ' || line[valueStart] == '\t')) {
            valueStart++;
        }
        string value = line.substr(valueStart);
        // Remove trailing newline
        if (!value.empty() && value[value.length()-1] == '\n') {
            value = value.substr(0, value.length()-1);
        }
        
        _parseInfo(key, value);
    }

    // Read overall CPU statistics from /proc/stat
    std::unique_ptr<FILE, decltype(&fclose)> stat(fopen("/proc/stat", "r"), fclose);

    if (!stat) {
        throw std::runtime_error("Failed to open /proc/stat !");
    }

    CPUStat currentStat;
    bool foundOverallCpu = false;
    
    while (fgets(buffer.data(), static_cast<int>(buffer.size()), stat.get()) != nullptr) {
        string line = buffer.data();
        if (_parseOverallCpu(line, currentStat)) {
            foundOverallCpu = true;
            break;
        }
    }

    if (foundOverallCpu) {
        if (_havePrevStat) {
            _calculateUsage(currentStat, _prevStat);
        }
        _prevStat = currentStat;
        _havePrevStat = true;
    }
}

// JNI implementations
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
   return cpu.getStatUser(core);
}

JNIEXPORT jint JNICALL Java_cpuInfo_getIdleTime (JNIEnv *env, jobject obj, jint core) {
   return cpu.getStatIdle(core);
}

JNIEXPORT jint JNICALL Java_cpuInfo_getSystemTime (JNIEnv *env, jobject obj, jint core) {
   return cpu.getStatSystem(core);
}