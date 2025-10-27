public class memInfo {
    // Native method declarations
    public native void read();

    public native long getTotal();

    public native long getUsed();

    // Static initializer to load the native library
    static {
        try {
            System.loadLibrary("sysinfo");
            System.out.println("memInfo: Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("memInfo: Failed to load native library: " + e.getMessage());
        }
    }
}