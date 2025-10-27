package systeminfo;

public class DiskBlocks {
    public long used;
    public long total;
    public long free;
    public double usagePercent;


    public DiskBlocks(long used, long total, long free) {
        this.used = used;
        this.total = total;
        this.free = free;
        this.usagePercent =  ( (double) used / total) * 100;
    }    
}