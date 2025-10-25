package systeminfo;

import java.util.List;
import java.util.ArrayList;

public class CpuCore {
    public int index;
    public List<CpuTimings> cpuTimings; // list of cput timing samples we have collected.

    public CpuCore (int index) {
        this.index = index;
        this.cpuTimings = new ArrayList<>();
    }
}