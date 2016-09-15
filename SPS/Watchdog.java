/*
 * Watchdog.java
 *
 * Created on 23. November 2008, 20:09
 *
 */

package name.gries;
import  com.siemens.icm.io.ATCommandFailedException;
import  java.util.Date;
import  java.util.Timer;
import  java.util.TimerTask;

/**
 *
 * @author Michael
 */
public class Watchdog extends TC65Terminal {
    
   private String dateReference;  // "YY/MM/DD,hh:mm:ss"
   private Timer timWatchdog = new Timer();
   private RunWatchdog rWD;

    
    /** Creates a new instance of Watchdog */
    public Watchdog() {
        System.out.println("\r\n\n\nWatchdog: Constructor");
        dateReference = "08/10/01,23:22:21";
    }
    
    public boolean dateCompare(String dateToCompare) {
        boolean bDateOK = true; //default
        String dateTest = dateToCompare.substring(0,8);
        System.out.println("\r\n\n\nWatchdog: (dateToCompare) = " + dateTest);
        String dateComp = dateReference.substring(0,8);
        System.out.println("\r\n\n\nWatchdog: (dateReference) = " + dateComp);
        bDateOK = dateComp.equals(dateTest);
        return bDateOK;
    }
    
    public void setDateReference(String DateRef) {
        dateReference = DateRef;
        System.out.println("\r\n\n\nWatchdog: (dateReference) = " + dateReference);
    }
    
    public void activateWatchdog() {
        long tagPeriod = 1000 * 60 * 60 * 1; // ms * s * m * h = x hours
        rWD = new RunWatchdog();
        timWatchdog.scheduleAtFixedRate(rWD, new Date(), tagPeriod);
    }
    
    /** Internal class that provides a TimerTask.   */
    class RunWatchdog extends TimerTask {

        /**
         * Public constructor: 
         */
        public RunWatchdog() {
            System.out.print("RunWatchdog: Constructor");
        }

        /**
         * As the timer fires, this method is invoked. 
         */
        public void run() {
            Debug.print("Watchdog Timer");
        }
    } // end internal class
    
} // end class
