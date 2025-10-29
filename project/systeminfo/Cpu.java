package systeminfo;

import java.util.List;
import java.util.ArrayList;

public class Cpu {
    public String model;
    public int socketCount;
    public int coresPerSocket;
    public List<CpuCore> cores;
    // project scope assumes 3 levels of cache and does not break it down socket by
    // socket
    public int l1dCacheSize;
    public int l1iCacheSize;
    public int l2CacheSize;
    public int l3CacheSize;

    public Cpu(String model, int socketCount, int coresPerSocket, int l1dCacheSize, int l1iCacheSize, int l2CacheSize,
            int l3CacheSize) {
        this.model = model;
        this.socketCount = socketCount;
        this.coresPerSocket = coresPerSocket;
        this.cores = new ArrayList<>();
        this.l1dCacheSize = l1dCacheSize;
        this.l1iCacheSize = l1iCacheSize;
        this.l2CacheSize = l2CacheSize;
        this.l3CacheSize = l3CacheSize;
        this.cores = new ArrayList<>();
    }
}