package systeminfo;

public class CpuTimings {
    public long timestamp;
    public double idlePercent;
    public double userPercent;
    public double systemPercent;
    public double usagePercent;

    public CpuTimings (long timestamp, double idlePercent, double userPercent, double systemPercent) {
        this.timestamp = timestamp;
        this.idlePercent = idlePercent;
        this.userPercent = userPercent;
        this.systemPercent = systemPercent;
        this.usagePercent = userPercent + systemPercent;
    }
}