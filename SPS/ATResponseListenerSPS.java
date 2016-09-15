/*
 * ATResponseListenerSPS.java
 *
 * Created on 17. August 2008, 22:43
 *
 * Javadoc related issues:
 * For classes which are only defined for implements (like this class)
 * additional setting are required in case of using Sun's Netbeans tool:
 * Under 'Project/Properties/Generate Javadoc' activate
 * checkbox 'Include Private and Package private Members'
 * 
 */

package name.gries;
import  com.siemens.icm.io.ATCommand; 
import  com.siemens.icm.io.ATCommandResponseListener; 
import  com.siemens.icm.io.ATCommandFailedException; 

/**
 * Listener for NON-blocking type AT Commands of TC65 API.
 *
 * @author  Michael Gries
 * @version 1.0
 *
 */
class ATResponseListenerSPS implements ATCommandResponseListener {
 
String listen_for;
 
    public ATResponseListenerSPS(String awaited_response) {
        System.out.println("Constructor ATResponseListenerSPS called");
        listen_for = awaited_response;
    }

    public void ATResponse(String Response) {
        System.out.println("ATResponseListenerSPS: " + Response);
        if (Response.indexOf(listen_for) >= 0) {
            System.out.println("ATResponceListenerSPS received: " + Response);
            /*
            try {
                ATCommand atCmd = new ATCommand(false);
                String response = "a";
                // SMS Text Mode vorbereiten und Nachricht senden
                response = atCmd.send("AT+CMGF=1" + "\r");
                response = atCmd.send("AT+CMGS=01702237454" + "\r");
                 response = atCmd.send("connected" + (char) 26);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                //
            } catch (ATCommandFailedException e) {
                System.out.println(e);
            } catch (IllegalStateException s) {
                System.out.println(s);
            } catch (IllegalArgumentException s) {
                System.out.println(s);
            } finally {
                System.out.println("AT Response Listener startApp()-finally");
            } 
             */
        } // end if
            System.out.println("ResponseListener OK");
    } // ATResponse
    
} // end class ATResponseListenerSPS
