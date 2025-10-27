package systeminfo;

import java.util.List;
import java.util.ArrayList;

public class Computer{
    public Cpu cpu;
    public List<PciBus> pciBuses;
    public List<UsbBus> usbBuses;
    public List<Disk> disks;
    public Memory memory;

    public Computer() {
        this.pciBuses = new ArrayList<>();
        this.usbBuses = new ArrayList<>();
        this.disks = new ArrayList<>();
    }

    public void dumpToConsole() {

        System.out.println("\n=== CPU INFO ===");
        System.out.println("Model: " + this.cpu.model);
        System.out.println("Sockets: " + this.cpu.socketCount + ", Cores per socket: " + this.cpu.coresPerSocket);
        System.out.println("Total cores: " + this.cpu.cores.size());
        System.out.println("L1d: " + this.cpu.l1dCacheSize + ", L1i: " + this.cpu.l1iCacheSize +
                        ", L2: " + this.cpu.l2CacheSize + ", L3: " + this.cpu.l3CacheSize);

        for (CpuCore core : this.cpu.cores) {
            System.out.println("  Core index: " + core.index + " (History size: " + core.cpuTimings.size() + ")");
            
            for (CpuTimings timing : core.cpuTimings) {
                System.out.println(
                    "s | idle=" + timing.idlePercent +
                    "% | user=" + timing.userPercent +
                    "% | system=" + timing.systemPercent + "%"
                );
            }
        }

        System.out.println("\n=== MEMORY INFO ===");
        System.out.println("Total memory: " + this.memory.totalMemory + ", Used memory: " + this.memory.usedMemory);

        System.out.println("\n=== DISK INFO ===");
        for (Disk d : this.disks) {
            System.out.println("Disk: " + d.name + ", Total blocks: " + d.blockCount + ", Used blocks: " + d.usedBlockCount);
        }

        System.out.println("\n=== USB INFO ===");
        for (UsbBus bus : this.usbBuses) {
            System.out.println("USB Bus " + bus.busIndex + " has " + bus.usbDevices.size() + " devices:");
            for (UsbDevice dev : bus.usbDevices) {
                System.out.println("  Device " + dev.deviceIndex + " (Vendor: " + dev.vendorId + ", Product: " + dev.productId + ")");
            }
        }

        System.out.println("\n=== PCI INFO ===");
        for (PciBus bus : this.pciBuses) {
            System.out.println("PCI Bus " + bus.busIndex + " has " + bus.pciDevices.size() + " devices:");
            for (PciDevice dev : bus.pciDevices) {
                System.out.println("  Device index " + dev.deviceIndex + " has " + dev.pciFunctions.size() + " functions:");
                for (PciFunction fun : dev.pciFunctions) {
                    System.out.println("    Function " + fun.functionIndex +
                                    " (Vendor: " + fun.vendorId + ", Product: " + fun.productId + ")");
                }
            }
        }
    }
}