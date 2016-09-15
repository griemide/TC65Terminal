/*
 * ATListenerSPS.java
 *
 * Created on 14. August 2008, 16:19
 *
 * Javadoc related issues:
 * For classes which are only defined for implements (like this class)
 * additional setting are required in case of using Sun's Netbeans tool:
 * Under 'Project/Properties/Generate Javadoc' activate
 * checkbox 'Include Private and Package private Members'
 * 
 */

package name.gries;
import  com.siemens.icm.io.*; 

/**
 * Listener for blocking type AT Commands of TC65 API.
 *
 * @author  Michael Gries
 * @version 8.10.7
 *
 */
class ATListenerSPS implements ATCommandListener {
   Trace Debug = new Trace();
   // private final static String URC_INCOMING_SMS = "RING";
    private final static String URC_INCOMING_SMS = "+CIEV: mess";
    public String fonicAccount;
    
    public String getAccount() {
        System.out.println("Called: ATListenerSPS.getAccount() = " + fonicAccount);
        return fonicAccount;
    }
    
    public ATListenerSPS() {
        System.out.println("Constructor ATListenerSPS called");
    } 
        ATCommand atLisCmd;
        String response;

    public void ATEvent(String event) {
        String s = event;
        s = s.replace('{','a');
        s = s.replace('\r',' ');
        s = s.replace('\n',' ');
        s = s.trim();
        System.out.println("URC (" + s.length() + ") -> " + s );
		int idx;
		idx = event.indexOf("FONIC");
		if (idx >= 0) {
                    idx = event.indexOf("EUR");
                    if (idx >= 0) {
                    String betrag = event.substring(idx-6,idx-1);
                    fonicAccount = betrag;
                    System.out.println("FONIC Guthaben: " + betrag + " EUR");
                   }
		}
		idx = event.indexOf(URC_INCOMING_SMS);
		if (idx >= 0) {
        		System.out.println(event + "-> handle SMS");
			//handleSms(event, idx);
		}
/*
        Debug.print("URC -> " + Event);
        System.out.print("Out URC =>" + Event);
        if (Event.indexOf("CMTI") >= 0) {
             System.out.print("SMS received" + Event);
        }
        if (Event.indexOf("C") >= 0) {
            System.out.print("C received" + Event);
 
        } else if (Event.indexOf("^SLCC:") >= 0) {
            // take desired action after receiving the reminder 
            //Debug.print("Rufnummer: " + Event.substring(21,35));    
            Debug.print("Rufnummer: " + Event);    
        } else if (Event.indexOf("SCPOL:") >= 0) {
        System.out.print("SCPOL =>" + Event);
        // take desired action after receiving the reminder 
        }
 */
    }

    public void RINGChanged(boolean SignalState) {
        //Debug.print("Interface event: RingChanged");
    }
    public void DCDChanged(boolean SignalState) {
        Debug.print("Interface event: DCDChanged");
    }
    public void DSRChanged(boolean SignalState) {
        Debug.print("Interface event: DSRChanged");
    }
    public void CONNChanged(boolean SignalState) {
        Debug.print("Interface event: CONNChanged");
    }
    
} // end class ATListenerSPS




