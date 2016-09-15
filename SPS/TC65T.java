/*
 * TC65T.java
 *
 * Basis functionality was inheried from http://www.
 *
 */

package name.gries;

import  com.siemens.icm.io.ATCommand;
import  com.siemens.icm.io.ATCommandFailedException;

/**
 * Handles all ATCommand related commands of the TC65 API interface.
 *
 * @author  Michael Gries
 * @version 1.0
 *
 */
public class TC65T {
 
	protected ATCommand atc;

        public void TC65T() {
            System.out.println("TC65T: " + super.getClass().toString());
 	}
        
        /*
         * Wichtig : ATCommand nicht in Kontruktur da dieser mehrfach durch Unterklassen aufgerufen wird
         */
        public void activateATparser() {
		try {
                    atc = new ATCommand(false);
                } catch (ATCommandFailedException e) {
			System.out.println("Could not create an AT parser");
		}
        }

	// this is the default, ugly wrapper.
	protected boolean sendAT(String command, String expect) {
		try {
			System.out.println(command);
			String response = atc.send(command + "\r");
			System.out.println(response);
			if (response.indexOf(expect) >= 0) {
				return true;
			}
		} catch (ATCommandFailedException e) {
			return false;
		}
		return false;
	}

	protected String getAT(String command, String expect) {
            //System.out.println("get AT");
		try {
			System.out.println(command);
			String response = atc.send(command + "\r");
			System.out.println(response);
			if (response.indexOf(expect) >= 0) {
				return response;
			}
		} catch (ATCommandFailedException e) {
			return null;
		}
		return null;
	}

}  // end class TC65T



