package systeminfo;

public class Memory{
    public long totalMemory;
    public long usedMemory;
    public double usagePercent;

    public Memory(long totalMemory, long usedMemory) {
        this.totalMemory = totalMemory;
        this.usedMemory = usedMemory;
        this.usagePercent = ( (double) usedMemory / totalMemory) * 100;
    }

    public void update(long totalMemory, long usedMemory) {
        this.totalMemory = totalMemory;
        this.usedMemory = usedMemory;
        this.usagePercent = ( (double) usedMemory / totalMemory) * 100;
    }
}