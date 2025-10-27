package systeminfo;

public class CpuTimings {
    public double idlePercent;
    public double userPercent;
    public double systemPercent;
    public double usagePercent;

    public CpuTimings (double idlePercent, double userPercent, double systemPercent) {
        this.idlePercent = idlePercent;
        this.userPercent = userPercent;
        this.systemPercent = systemPercent;
        this.usagePercent = userPercent + systemPercent;
    }
}