import javax.swing.SwingWorker;
import systeminfo.Computer;


public class SystemInfoWorker extends Thread {
    private boolean running = true;
    private final Computer computer;
    private final cpuInfo cpu;
    private int secondsSinceStart = 0;

    public SystemInfoWorker(Computer computer, cpuInfo cpu) {
        this.computer = computer;
        this.cpu = cpu;
    }

    public void run() {
        while (true) {     
            // takes around 1 second and collects usage data for that one second
            cpu.read(1);
            template.sampleCpuUsage(computer, cpu);
            secondsSinceStart++;
        }
    }

    public void stopWorker() {
        running = false;
    }

}
