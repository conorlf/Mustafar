import javax.swing.SwingWorker;
import systeminfo.Computer;


public class SystemInfoWorker extends Thread {
    private volatile boolean running = true;


    public void run() {
        while (true) {   
            // takes around 1 second and collects usage data for that one second
            //cpu.read(1);
            template.sampleCpuUsage();
            template.refreshMemoryInfo();
            //secondsSinceStart++;
        }
    }

    public void stopWorker() {
        running = false;
    }

}
