/*
 * Trace.java
 *
 * Created on 11. August 2008, 21:37
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package name.gries;

import  java.util.Calendar;
import  java.util.Date;
import  java.util.Stack;
import  com.siemens.icm.io.File;

/**
 * Trace functionality for logging and debugging purposes
 *
 * @author  Michael Gries
 * @version 1.0
 */
public class Trace {
    private static Calendar planner;
    private Date now;
    String timestamp;
    Stack log = new Stack(); // debug info for writing to flash memory later on.
    Stack fifo = new Stack(); 
    StringBuffer buffer = new StringBuffer(4096); 
    boolean bThermoPrinter = false;
    
    /** Creates a new instance of Debug */
    public Trace() {
        System.out.println("Trace: " + super.getClass().toString() );
        planner = Calendar.getInstance();
    }
    
    /**
    * similar to System.out.println() but additional a timestamp
    * will be added in format 'YYYY-MMM-DD hh:mm:ss'
    * 
    */
    public void print(String info){
        now = new Date(System.currentTimeMillis() );
        String timestamp = now.toString();  // e.g. 'Wed Aug 13 23:47:01 UTC 2008'
        String heute;
        heute = formatTimestamp(timestamp); // e.g. '2008-Aug-13 23:47:01 '
        String logLine = heute + "- " + info;
        if (bThermoPrinter) { System.out.print("\\0"); } // erase buffer
        System.out.println(logLine);
        if (bThermoPrinter) { 
            System.out.print("\\1"); 
            try {
                java.lang.Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } // print buffer
        String newLogLine = logLine.replace('\r',' ');      // replace CR
        String oneLogLine = newLogLine.replace('\n',':');   // replace LF
        log.push(new String("> " + oneLogLine)); // add to stack
    }
    
    /* convert format of timestamp String
     * String 'Wed Aug 13 23:47:01 UTC 2008' -> 'YYYY-MMM-DD hh:mm:ss'
     *         01234567890123456789012345678 (index count for substring)
     */
    private String formatTimestamp(String timestamp) {
        String sT = timestamp;
        String sTf = "NONE";
        try {
            sTf = sT.substring(24,28) + "-" +     // YYYY 
                  sT.substring(4,7) + "-" +       // MMM 
                  sT.substring(8,10) + "  " +     // DD
                  sT.substring(11,19) + " " +     // hh:mm:ss
                  "";
        } catch (IndexOutOfBoundsException s) {
            System.out.println("Trace.formatTimestamp: " + s);
        }
        return sTf;
    }

    public void printStack() {
        final String HEADER = "****** Log starts ******";
        final String FOOTER = "****** Log ends ********";
        String line;
        //first convert STACK (LIFO) into FIFO
        while (! log.empty()) {
            fifo.push(new String((String) log.pop())); 
        }
        // print out converted FIFO data and buffer for flash file writing
        System.out.println(HEADER);
        buffer.append(HEADER + "\r\n");
        while (! fifo.empty()) {
            line = (String) fifo.pop();
            System.out.println(line);
            buffer.append(line  + "\r\n");
       }
        System.out.println(FOOTER);
        buffer.append(FOOTER + "\r\n"+ "\r\n" );
    }
    
     public String writeStack() {
         /*
        String sLine = "voltage[0][314]=\"3094\" //// mV";
        File f = new File();
        f.open("voltage.txt");
        f.write(sLine,0,1000);
        */
        return buffer.toString();
    }
    
    public static String getAT_CCLKformat (Date now) {
        String sDF = new String("");
        Calendar cal=Calendar.getInstance();
        cal.setTime(now);

        sDF = sDF.concat(String.valueOf(cal.get(Calendar.YEAR)).substring(2,4));
        sDF = sDF.concat("/");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.MONTH)+1));
        sDF = sDF.concat("/");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.DAY_OF_MONTH)));
        sDF = sDF.concat(",");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.HOUR_OF_DAY)));
        sDF = sDF.concat(":");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.MINUTE)));
        sDF = sDF.concat(":");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.SECOND)));

        return sDF;
    }
    
    /**
    * Returns the Day of the TC65 module (format "mm\dd" fixed).
    *
    * Day will be filtered as substring out of ATCommand response.<br>
    * No usage of AT commands while overlapping thrats are used
    */
    public String getAstroDay() {
        now = new Date(System.currentTimeMillis() );
        String sDF = new String("");
        Calendar cal=Calendar.getInstance();
        cal.setTime(now);
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.MONTH)+1));
        sDF = sDF.concat("/");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.DAY_OF_MONTH)));
        return sDF;
    } // getAstroDay      
    
    
    public static String convertTimeToString (Date now) {
        String sDF = new String("");
        Calendar cal=Calendar.getInstance();
        cal.setTime(now);
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.HOUR_OF_DAY)));
        sDF = sDF.concat(":");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.MINUTE)));
        sDF = sDF.concat(":");
        sDF = sDF.concat(CheckNumber(cal.get(Calendar.SECOND)));
        return sDF;
    }
	
    private static String CheckNumber (int data) {
        String number = new String("");
        if (data<10) {
                number=number.concat("0");
        }
        number=number.concat(String.valueOf(data));
        return number;
    }

} // end class Trace
