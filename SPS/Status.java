/*
 * Status.java
 *
 * Created on 2. November 2008, 20:30
 *
 */

package name.gries;

/**
 *
 * @author Michael Gries
 * @version 8.11.3
 *
 */
public class Status {
    private String voltageRTC;
    private String printer;
    private String absent;
    private String account;
    private String signal;

    
    /** Creates a new instance of Status */
    public Status() {
        voltageRTC = "RTC undefined. ";
        printer = "Printer undefined. ";
        absent = "Astro undefined";
        account = "FONIC ee.cc EUR";
        signal = "Signal undefined";
    }
    
    public String getStatus() {
        String status;
        status= "\n" +
                absent + "\n" +
                printer + "\n" +
                voltageRTC + "\n" +
                signal + "\n" +
                account + "\n" +
                timestamp();
        return  status;
    }
    
    public void updateVoltageRTC(String status) {
        voltageRTC = status;
    }
    
    public void updatePrinter(String status) {
        printer = status;
    }
    
    public void updateAbsent(String status) {
        absent = status;
    }
    
    public void updateAccount(String status) {
        account = status;
    }
    
    public void updateSignalQuality(String status) {
        signal = status;
    }
    
    private String timestamp() {
        java.util.Date now = new java.util.Date(System.currentTimeMillis() );
        String timestamp = now.toString();  // e.g. 'Wed Aug 13 23:47:01 UTC 2008'
        timestamp = timestamp.substring(4,19);
        return timestamp;
    }
    
} // end class Status
