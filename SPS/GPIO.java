/*
 * GPIO.java
 *
 * Created on 14. August 2008, 17:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package name.gries;
import  java.util.Hashtable;

/**
 * General Purpose Input Output of TC65.
 *
 * @author  Michael Gries
 * @version 1.0
 *
 */
public class GPIO extends TC65Terminal {

    private Hashtable device = new Hashtable();
    private Hashtable port = new Hashtable();

        
    /** Creates a new instance of GPIO */
    public GPIO() {
        device.put("UNUSED1","P1");
        device.put("UNUSED2","P2");
        device.put("UNUSED3","P3");
        device.put("UNUSED4","P4");
        device.put("AUSSENLICHT","P5");
        device.put("UNUSED6","P6");
        device.put("UNUSED7","P7");
        device.put("UNUSED8","P8");
        device.put("TC65_OFF","P9");
        device.put("FLURLICHT","P10");

        port.put("P1", new Integer(0));
        port.put("P2", new Integer(1));
        port.put("P3", new Integer(2));
        port.put("P4", new Integer(3));
        port.put("P5", new Integer(4));
        port.put("P6", new Integer(5));
        port.put("P7", new Integer(6));
        port.put("P8", new Integer(7));
        port.put("P9", new Integer(8));
        port.put("P10", new Integer(9));

    }
 

    public boolean setDevice(String device) {
        return true;
    }
    
    public boolean resetDevice(String device) {
        return true;
    }
    
    public String getDevicePort(String function) {
       System.out.println("getDevicePort " + function);
       String status = "UNKOWN";
        if (device.containsKey(function)) {
            Object o = device.get(function);
            status = (String)o;
        }
        System.out.println("getDevicePort " + status);
        return status;
    }

        public void setPort5() {
            sendAT("AT^SSIO=4,1", "OK");
        }
        
        
        public void setFlurLicht()      { setPort5();       }
        public void resetTestLicht()    { 
              System.out.println(" reset Testlicht");
//            resetPort5();     
        }
 
    


    
}
