package systeminfo;

public class CpuTimings {
    public int secondsSinceStart;
    public double idlePercent;
    public double userPercent;
    public double systemPercent;
    public double usagePercent;

    public CpuTimings (int secondsSinceStart, double idlePercent, double userPercent, double systemPercent) {
        this.secondsSinceStart = secondsSinceStart;
        this.idlePercent = idlePercent;
        this.userPercent = userPercent;
        this.systemPercent = systemPercent;
        this.usagePercent = userPercent + systemPercent;
    }
}