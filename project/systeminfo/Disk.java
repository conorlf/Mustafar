package systeminfo;

import java.util.List;
import java.util.ArrayList;

public class Disk{
    public int index;
    public String name;
    public DiskBlocks diskBlocks;

    public Disk(int index, String name){
        this.index = index;
        this.name = name;
    }
}