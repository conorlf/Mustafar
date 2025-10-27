/*
 * Interface for USB device notification callbacks
 */
@FunctionalInterface
public interface UsbNotificationListener {
    void onNotification(String message);
}