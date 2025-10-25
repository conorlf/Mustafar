package systeminfo;

public class Disk{
    public String name;
    public long blockCount;
    public long usedBlockCount;

    public Disk(String name, long blockCount, long usedBlockCount){
        this.name = name;
        this.blockCount = blockCount;
        this.usedBlockCount = usedBlockCount;
    }
}