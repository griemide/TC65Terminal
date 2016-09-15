/*
 * TC65Terminal.java
 *
 * Basis functionality was inheried from http://www.
 *
 */

package name.gries;

import  com.siemens.icm.io.ATCommand;
import  com.siemens.icm.io.ATCommandFailedException;
import  com.siemens.icm.io.ATStringConverter;

import  com.siemens.icm.io.*;
import  java.io.*;
import  com.siemens.icm.io.file.*;
import  java.util.Enumeration;
import  javax.microedition.io.*;
import  java.util.Date;
import  java.util.Calendar;
import  java.util.Hashtable;

/**
 * Handles all ATCommand related commands of the TC65 API interface.
 *
 * @author  Michael Gries
 * @version 1.0
 *
 */public  class TC65Terminal {
     
     static GPIO gpio   = new GPIO();;
 
     private boolean bAbwesenheit = false;
             
	// AT-related
	private final static String URC_INCOMING_SMS = "+CMTI: \"MT\",";

	private final static char AT_CTRL_Z = (char) 26;

	private final static String SMS_TEXT_READY = ">";

	// TC65-related
	private final static int MT_CAPACITY = 50;

	private final static String FILE_CONN_FLASH_FS = "file:///a:/storage/";

	// OTAP-related
	private final static String OTAP_KEYWORD = "AF104";

	private final static String OTAP_PWD = "PWD";

	private final static String OTAP_COMMAND = "COMMAND";

	private final static String OTAP_ARGS = "ARGS";

	private final static String OTAP_NL = "\n";

	private final static String OTAP_SECRET = "tc";

	// Config-related
	private final static String FACTORY_GPRS_OPTS = ";bearer_type=gprs;access_point=internet;dns=213.055.128.001;timeout=40000";

	// informe if the timer of the module has been synchronized correctly
	private boolean synced_Time = false;

	private static Calendar calendar;

	// difference between the actual system time and the calendar received
	private static long offsetCalendar;

	private Hashtable propertiesTable;

	protected ATCommand ata;
        protected ATListenerSPS atLis;
        
        private String fonicGuthaben;

        private static String gprsPoint =  "orangeinternet";
        private static String gprsUser = "gonk";
        private static String gprsPass = "zonk";
       
        Trace Debug;
     

	public TC65Terminal() {
            System.out.println("TC65Terminal: " + super.getClass().toString());
            //System.out.println("TC65Terminal" + TC65Terminal.class.toString());
            //System.out.println(super.toString());
            Debug   = new Trace();

	}
        
        /*
         * Wichtig : ATCommand nicht in Kontruktur da dieser mehrfach durch Unterklassen aufgerufen wird
         */
        public void activateATparser() {
		try {
			ata = new ATCommand(false);
                        atLis = new ATListenerSPS();
                        ata.addListener(atLis);
                        ATResponseListenerSPS ringListener = new ATResponseListenerSPS("RING"); // for 'URC: +CUSD''
                        String command = "AT^SJNET=GPRS," + gprsPoint + "," + gprsUser + "," + gprsPass + "\r";
                        System.out.println("GPRS Setup: send " + command);
                        String response;
                        response = ata.send(command);

                        if (response.indexOf("OK") >= 0) {
                            System.out.println("GPRS Setup: OK " + gprsPoint);
                        } else {
                            System.out.println("GPRS Setup: fails -> " +response);
                        }
                } catch (ATCommandFailedException e) {
			System.out.println("Could not create an AT parser");
		}
        }

        
        public void uploadFtp(String TerminalID){
            try {    
                String sFilename;
                //System.out.println("FtpDemo: new FTP at www.gries.name");
                Ftp k = new Ftp("82.165.87.24", "3376-227", "45780176");
                sFilename = "voltage"+TerminalID+".txt";
                k.uploadAppend("RTC", sFilename, false);			 
                //System.out.println("Upload append succesful! ");
               
                Ftp a = new Ftp("82.165.87.24", "3376-227", "45780176");
                //a.uploadAppend("AF104", "AF104.log", false);			 
                sFilename = "log"+TerminalID+".txt";
                a.uploadAppend("AF104", sFilename, false);			 
                //System.out.println("Upload append succesful! ");

                Ftp s = new Ftp("82.165.87.24", "3376-227", "45780176");
                sFilename = "signal"+TerminalID+".txt";
                s.uploadAppend("Signal", sFilename, false);			 
                //System.out.println("Upload append succesful! ");

                Ftp f = new Ftp("82.165.87.24", "3376-227", "45780176");
                sFilename = "account"+TerminalID+".txt";
                f.uploadAppend("FONIC", sFilename, false);			 
                //System.out.println("Upload append succesful! ");

                /*
                Ftp d = new Ftp("www.gries.name", "3376-227", "45780176");
                //d.download("Michael", "Gries.shtm", false);
                d.download("Michael", "textdatei.txt", false);
                System.out.println("Download succesful! ");
                 */
            } catch (java.io.IOException ioe) {
                System.out.println("FtpDemo: catch: IOException & Stacktrace:");
                ioe.printStackTrace();
            }
        }
        
        
        
        public String getAccount() {
                        //fonicGuthaben = atLis.getAccount();
                        fonicGuthaben = atLis.fonicAccount;
                        System.out.println("Called: TC65Terminal.getAccount() = " + fonicGuthaben);
            return fonicGuthaben;
        }

        public void shutdownTC65T() {
            sendAT("AT^SMSO", "OK");
        }
 
        public void deactivateGPIO() {
            sendAT("AT^SPIO=0", "OK");
        }
 
        public void activateGPIO() {
            sendAT("AT^SPIO=1", "OK");
        }
        
       public void activatePort5() {
            sendAT("AT^SCPIN=0,4", "OK");
            sendAT("AT^SCPIN=1,4,1", "OK");    // Output
            sendAT("AT^SSIO=4,0", "OK");
        }
        
        public void resetPort5() {
            sendAT("AT^SSIO=4,0", "OK");
        }
        
        public void setPort5() {
            sendAT("AT^SSIO=4,1", "OK");
        }
        
        public void activatePort9() {
            sendAT("AT^SCPIN=0,8", "OK");
            sendAT("AT^SCPIN=1,8,0", "OK");    // Input
            sendAT("AT^SCPOL=1,8", "OK");
        }
         public boolean pollPort9() {
            String result = getATresult("AT^SGIO=8", "OK");
            int iOffset = result.indexOf("^SGIO: 1");
            if (iOffset != -1) { 
                return true; 
            } else {
                return false;
            }
        }
        
        public void activatePort7() {
            sendAT("AT^SCPIN=0,6", "OK");
            sendAT("AT^SCPIN=1,6,1", "OK");   // Output
            sendAT("AT^SSIO=6,0", "OK");
        }
        public void resetPort7() {
            sendAT("AT^SSIO=6,0", "OK");
        }
        
        public void setPort7() {
            sendAT("AT^SSIO=6,1", "OK");
        }       
        
        public void activatePort8() {
            sendAT("AT^SCPIN=0,7", "OK");
            sendAT("AT^SCPIN=1,7,1", "OK");   // Output
            sendAT("AT^SSIO=7,0", "OK");
        }
        public void resetPort8() {
            sendAT("AT^SSIO=7,0", "OK");
        }
        
        public void setPort8() {
            sendAT("AT^SSIO=7,1", "OK");
        }       
        
        
        public void activatePort10() {
            sendAT("AT^SCPIN=0,9", "OK");
            sendAT("AT^SCPIN=1,9,1", "OK");   // Output
            sendAT("AT^SSIO=9,0", "OK");
        }
        public void resetPort10() {
            sendAT("AT^SSIO=9,0", "OK");
        }
        
        public void setPort10() {
            sendAT("AT^SSIO=9,1", "OK");
        }       
        
        
        /* Mapping */
        public boolean updateAbwesenheit()     { 
            if (pollPort9()) { // low activ -> inverter
                System.out.print(".");
                bAbwesenheit = false;
            } else {
                System.out.print(":");
                bAbwesenheit = true;
            }
            return bAbwesenheit;
        }
        
        public void gpioTest() {
            gpio.setFlurLicht();
        }
        
        public void setFlurLicht()      { setPort5();       }
        public void resetFlurLicht()    { resetPort5();     }
                 
        public void setKlingel()        { setPort8();       }
        public void resetKlingel()      { resetPort8();     }
        public void toggleKlingel()     {
            // simulates bipolar relais
            setPort8();
            delay(100);
            resetPort8();
        }
                 
        public void setAussenLicht()    { setPort10();      }
        public void resetAussenLicht()  { resetPort10();    }
                 
        public void checkPrepaid() {
            sendAT("ATD*101#;", "OK");
        }
        
       public int getADC0() {
            int measured = 2222;
            int offset = 0;
            final int DIV = 4096;
            int gain = DIV * 2;
            float calculated = 0;
            int voltage = 0;
            String  result = "None";
            // e.g. String '^SAADC: -xx,8581,-yy,8565'
            result = getAT("AT^SAADC?", "OK");
            int iOffset = result.indexOf(":") + 2 ;
            int iKomma = result.indexOf(",");
            String sOffset = result.substring(iOffset,iKomma);
            //System.out.println(iOffset + " & " + iKomma + "S OFFSET " + sOffset);
            offset = Integer.valueOf(sOffset).intValue();
            String sGain = result.substring(iKomma+1,iKomma+1 + 4);
            gain = Integer.valueOf(sGain).intValue();
            // e.g. String '^SRADC: 0,1,1367'
            result = getAT("AT^SRADC=0", "OK");
            iOffset = result.indexOf(":") + 6 ;
            //int len = result.length();
            int iOK = result.indexOf("OK") - 4; // zweimal \n\r
            result = result.substring(iOffset,iOK);
            measured = Integer.valueOf(result).intValue();
            //System.out.println("Messured: " + measured);
            calculated = (measured-offset)*gain/DIV;
            return voltage = (int)calculated;
        }
                 
	public boolean setDate(Date date) {
		offsetCalendar = date.getTime() - System.currentTimeMillis();
		calendar.setTime(date);
		//TODO improve if possible this mechanism
		return sendAT("at+CCLK=\"" + "YY" + "\"",
				"OK");
	}

        public boolean getRingState() throws ATCommandFailedException {
            return ata.getRING();
	}

        
        /**
        * Sets Real Time Clock (RTC) as given by date, time string [AT+CCLK].
        * <p>
        * @param date 'YY/MM/DD,hh:mm:ss'
        * @return boolean'
        */
	public boolean setDate(String date) {
		return sendAT("at+CCLK=\"" + date + "\"",
				"OK");
	}

	public static Date getDate() {
		//Date date=new Date(System.currentTimeMillis()+offsetCalendar);
		return new Date(System.currentTimeMillis() + offsetCalendar);
	}

	public static long getTime() {
		return System.currentTimeMillis() + offsetCalendar;
	}

	public boolean pinCode(int password) {
		return sendAT("at+cpin=" + password, "OK");
	}

	public boolean setQoSforGRPS(int cid, int precedence, int delay,
			int reliability, int peak, int mean) {
		return sendAT("AT+CGQREQ=" + cid + "," + precedence + "," + delay + ","
				+ reliability + "," + peak + "," + mean, "OK");
	}

	public boolean ATCommandOTAP(String sms_pwd, String jad_URL,
			String appl_Dir, String http_user, String http_Pwd, String bs,
			String dest, String net_user, String net_pwd, String dns,
			String notifyURL) {
		return sendAT("AT^SJOTAP=\"" + sms_pwd + "\",\"" + jad_URL + "\",\""
				+ appl_Dir + "\",\"" + http_user + "\",\"" + http_Pwd + "\",\""
				+ bs + "\",\"" + dest + "\",\"" + net_user + "\",\"" + net_pwd
				+ "\",\"" + dns + "\",\"" + notifyURL + "\"", "OK");
	}

	public boolean ATCommandOTAP() {
		String sms_pwd = "tc";
		String jad_URL = "http://myurl";
		String appl_Dir = "a:/apps";
		String http_user = "";
		String http_Pwd = "";
		String bs = "gprs";
		String dest = "gprs.swisscom.ch";
		String net_user = "";
		String net_pwd = "";
		String dns = "164.128.36.34";
		String notifyURL = "";
		return sendAT("AT^SJOTAP=\"" + sms_pwd + "\",\"" + jad_URL + "\",\""
				+ appl_Dir + "\",\"" + http_user + "\",\"" + http_Pwd + "\",\""
				+ bs + "\",\"" + dest + "\",\"" + net_user + "\",\"" + net_pwd
				+ "\",\"" + dns + "\",\"" + notifyURL + "\"", "OK");

	}

	public void setSyncedTime() {
		synced_Time = true;
	}

	public void resetSyncedTime() {
		synced_Time = false;
	}

	public boolean IsSyncedTime() {
		return synced_Time;
	}

	public static String getFlashPath() {
		return FILE_CONN_FLASH_FS;
	}

	public String getGprsConf() {
		String conf = readProp("GRPSCONF");
		if (conf == null) {
			conf = FACTORY_GPRS_OPTS;
		}
		return conf;
	}

	/**
	 * @param prop
	 * @return property value, null if the property is not set.
	 */
	public String readProp(String prop) {
		// check if value is already in the table
		if (propertiesTable.containsKey(prop)) {
			return (String) propertiesTable.get(prop);

		} else { // read property from flash
			String val = null;
			try {
				FileConnection fconn = (FileConnection) Connector.open(
						FILE_CONN_FLASH_FS + prop, Connector.READ);
				if (fconn.exists()) {
					InputStream is = fconn.openInputStream();
					val = "";
					int b;
					while (true) {
						b = is.read();
						if (b == -1)
							break;
						val = val + ((char) b);
					}
					is.close();
				}
				fconn.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			// add the new prop in the properties table
			if (val != null) {
				propertiesTable.put(prop, val);
			} else {
			}
			return val;
		}
	}

	public String setProp(String prop, String val) {
		// write to flash
		try {
			FileConnection fconn = (FileConnection) Connector.open(
					FILE_CONN_FLASH_FS + prop, Connector.READ_WRITE);
			if (fconn.exists()) {
                            fconn.delete();
			} 
                        fconn.create();
                        OutputStream output = fconn.openOutputStream();
			InputStream input = new ByteArrayInputStream(val.getBytes());
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while ((bytesRead = input.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			output.flush();
			output.close();
			fconn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		// and change it in the properties table
                /*
		if (propertiesTable.containsKey(prop)) {
			// no need to do propretiesTable.remove(prop); because put will replace
			// the value if the key already exist.
		} else {
		}
		propertiesTable.put(prop, val);
                 */
		return val;
	}

	/**
	 * Used AT Commands:<br>
	 * +CNMI, +CMGF, +CMEE, \Q, &V
	 */
	public void init() {
            /*
		// for the pin code
		String pin = readProp("PIN_PASSWORD");
		if (pin != null) {
			pinCode(Integer.valueOf(pin).intValue());
		}
             */
		// received SMS should go to MT memory, set preferred storage
		/*
            sendAT("AT+CPMS=\"SM\"", "OK");
		
                // delete all msg, TODO : this is best-effort!
                // /*
		for (int i = 1; i <= 25; i++) {
			String atcom = "AT+CMGD=" + i;
			sendAT(atcom, "OK");
                        System.out.println("Index " + i  +" deleted");
		}
		sendAT("AT+CPMS=\"ME\"", "OK");
		
                // delete all msg, TODO : this is best-effort!
                // /*
		for (int i = 1; i <= 25; i++) {
			String atcom = "AT+CMGD=" + i;
			sendAT(atcom, "OK");
                        System.out.println("Index " + i  +" deleted");
		}
		sendAT("AT+CPMS=\"MT\"", "OK");
		
                // delete all msg, TODO : this is best-effort!
                // /*
		for (int i = 1; i <= MT_CAPACITY; i++) {
			String atcom = "AT+CMGD=" + i;
			sendAT(atcom, "OK");
                        System.out.println("Index " + i  +" deleted");
		}
                */
		
                // init for SMS URC
		// AT+CNMI=[<mode>][, <mt>][, <bm>][, <ds>][, <bfr>]
                System.out.println("init for SMS URC");
		sendAT("AT+CNMI=3,1,0,2,1", "OK");
		// init for text mode sms
                System.out.println("init for text mode sms");
		sendAT("AT+CMGF=1", "OK");
		// Enable error result code with verbose (string) values
                System.out.println("Enable error result code with verbose (string) values");
		sendAT("AT+CMEE=2", "OK"); 
                // ATTENTION
		// no RTS/CTS flow control if FV00019U used
                System.out.println("no RTS/CTS flow control if FV00019U used");
		sendAT("AT\\Q0", "OK"); // beachte escape Zeichen
		sendAT("AT&V", "OK"); // prüfe \Q status (erste Zeile)
	}

	// this is the default, ugly wrapper.
	protected boolean sendAT(String command, String expect) {
		try {
			System.out.println(command);
			String response = ata.send(command + "\r");
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
			String response = ata.send(command + "\r");
			System.out.println(response);
			if (response.indexOf(expect) >= 0) {
				return response;
			}
		} catch (ATCommandFailedException e) {
			return null;
		}
		return null;
	}

	/**
         *   
         */
        protected String getATresult(String command, String expect) {
            String s = "NONE";
		try {
                    String response = ata.send(command + "\r");
                    if (response.indexOf(expect) >= 0) {
                        int i = response.indexOf('\r');
                        int l = response.lastIndexOf('K');
                        if (i >= 0) {
                            s = response.substring(i,l-5);
                         }
                    return s;
                    }
		} catch (ATCommandFailedException e) {
                    return null;
		}
		return null;
	}

	public void sendSms(String num, String msg) {
		sendAT("AT+CMGF=1", "OK");
		sendAT("AT+CMGS=" + num, SMS_TEXT_READY);
		sendAT(msg + AT_CTRL_Z, "OK");
	}

	public String getTc65Status() {
		return "\nFreemem=" + (Runtime.getRuntime()).freeMemory()
				+ "\nFreeflash=" + getFreeFlashSpace();
	}

	public void listFs() {
            try {
                Enumeration e = FileSystemRegistry.listRoots();
                while (e.hasMoreElements()) {
                    String root = (String) e.nextElement();
                    FileConnection fc = (FileConnection) Connector.open("file:///" + root);
                    System.out.println("Filesystem: " + root + " " + fc.availableSize() + " Byte");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
	}

	// serial is imei-imsi
	public String getDeviceSerial() {
		return getAT("AT+CGSN", "OK") + "-" + getAT("AT+CIMI", "OK");
	}

	public long getFreeFlashSpace() {
		long fs = -1;
		try {
			FileConnection fconn = (FileConnection) Connector.open(
					FILE_CONN_FLASH_FS, Connector.READ);
			fs = fconn.availableSize();
			fconn.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return fs;
	}

	// listener interface

	/*
	 * when some AT event occures, like a sms reception, a call reception.
	 */
	public void ATEvent(String event) {
		System.out.println("URC ->");
		System.out.println(event);
		System.out.println(event.length());

		int idx;
		idx = event.indexOf(URC_INCOMING_SMS);
		if (idx >= 0) {
			handleSms(event, idx);
		}
	}

    public void RINGChanged(boolean SignalState) {
        Debug.print("Interface event: RingChanged");
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

	boolean once = false;

        /**
	 * 
	 * @param event
	 * @param idx
	 */
	private void handleSms(String event, int idx) {
		// fetch the sms index
		String smsId = "";
		int k = idx + URC_INCOMING_SMS.length();
		int j = 0;
		while (true) {
			if ((k + j + 1) > event.length())
				break;
			String c = event.substring(k + j, k + j + 1);
			try {
				Integer.parseInt(c);
				smsId = smsId + c;
			} catch (NumberFormatException nfe) {
				break;
			}
			j++;
		}

		String msg = ATStringConverter
				.GSM2Java(getAT("at+cmgr=" + smsId, "OK"));

		// TODO : delete message
		int otidx = msg.indexOf(OTAP_KEYWORD);

		if (otidx < 0) {
			System.out.println("Not a tinynode OTAP message, discarding");
			return;
		}

		msg = msg.substring(otidx);

		// check the secret
		String pass = getOtapPropValue(OTAP_PWD, msg);
		if (!pass.equals(OTAP_SECRET)) {
			System.out.println("Incorrect PWD");
			return;
		}

		System.out.println("CMD:" + getOtapPropValue(OTAP_COMMAND, msg));
		System.out.println("ARGS:" + getOtapPropValue(OTAP_ARGS, msg));
		//OtapCommandCalled(getOtapPropValue(OTAP_COMMAND, msg),
		//		getOtapPropValue(OTAP_ARGS, msg));

	}

	public void handleSmsRTC(String event, int idx) {
		// fetch the sms index
		String smsId = "";
		int k = idx + URC_INCOMING_SMS.length();
		int j = 0;
		while (true) {
			if ((k + j + 1) > event.length())
				break;
			String c = event.substring(k + j, k + j + 1);
			try {
				Integer.parseInt(c);
				smsId = smsId + c;
			} catch (NumberFormatException nfe) {
				break;
			}
			j++;
		}

		String msg = ATStringConverter
				.GSM2Java(getAT("at+cmgr=" + smsId, "OK"));

		// TODO : delete message
		int otidx = msg.indexOf(OTAP_KEYWORD);

		if (otidx < 0) {
			System.out.println("Not a tinynode OTAP message, discarding");
			return;
		}

		msg = msg.substring(otidx);

		// check the secret
		String pass = getOtapPropValue(OTAP_PWD, msg);
		if (!pass.equals(OTAP_SECRET)) {
			System.out.println("Incorrect PWD");
			return;
		}

		System.out.println("CMD:" + getOtapPropValue(OTAP_COMMAND, msg));
		System.out.println("ARGS:" + getOtapPropValue(OTAP_ARGS, msg));
		//OtapCommandCalled(getOtapPropValue(OTAP_COMMAND, msg),
		//		getOtapPropValue(OTAP_ARGS, msg));

	}

	//protected void OtapCommandCalled(String command, String args);

	private String getOtapPropValue(String propName, String msg) {
		String token = propName + ":";
		int idxa = msg.indexOf(token); //begin of the prop value
		if (idxa < 0)
			return null;

		int idxb = msg.indexOf(OTAP_NL, idxa); // end of the prop value

		int idxb_bis = msg.indexOf(" ", idxa); // end of the prop value
		if (idxb_bis > 0 && idxb_bis < idxb)
			idxb = idxb_bis;

		if (idxb < 0)
			return null;
		return msg.substring(idxa + token.length(), idxb);
	}

    /**
    * Returns the IMEI of the TC65 module (15 characters fixed).
    * <p>
    * IMEI will be filtered as substring out of ATCommand response.
    * <p>
    * Remarks: <br>
    * must be type String, because Integer type range to short and <br>
    * Long type does not support method valueOf(). 
    * @return String   e.g. TC63 module '355633004695531'
    */
    public String getIMEI() throws ATCommandFailedException {
        int IMEI_LEN = 15; // constant length
        int offset = 12;    // no. of command sent characters plus CR/LF
        String result = "NONE";
        result = ata.send("ATD*#06#;" + "\r"); 
        if (result.indexOf("OK") >= 0) {
            result = result.substring(offset,offset + IMEI_LEN);
        } else {
            result = "ATD ERROR";
        }
        // System.out.println("IMEI: " + result);
        return result;
    } // getIMEI
    
    /**
    * Returns the Baudrate of the TC65 module (on ASC0).
    * <p>
    * Baud rate will be filtered as substring out of ATCommand response AT+IPR?.
    * <p>
    * Remarks: <br>
    * Used to identify if thermoprinter is activated.<br>
    * Thermoprinter FV00019U requires ta baud rate of 19200. <br>
    * The rate can't be set by Java AT command, so it must be set before<br>
    * java application is running. This indicates that User wants to use the printer
    */
    public int getBaudRate() throws ATCommandFailedException {
        final String sCmd = "AT+IPR?";
        int iCmdLen = sCmd.length() + 2 ; // + CR and LF
        int iOffset; int iLast;
        int iRate = 0;
        final int V_LEN = 4; // format 'nnnn' mV
        String result = "NONE";
        String sResult = "0000";
        result = ata.send(sCmd + "\r"); 
        if (result.indexOf("OK") >= 0) {
            iLast = result.indexOf("OK") - 4;
            iOffset = result.indexOf(":") + 2 ;
            result = result.substring(iOffset,iLast);
            iRate = Integer.valueOf(result).intValue();
        } else {
            result = "Baudrate ERROR";
        }
        return (int)iRate;
    } // getBaudRate
    
    /**
    * Returns the Battery Voltage of the TC65 module (in mV).
    *
    * Voltage will be filtered as substring out of ATCommand response.
    * @return String 'Battery voltage in mV'
    */
    public int getBattery() throws ATCommandFailedException {
        final String sCmd = "AT^SBV";
        int iCmdLen = sCmd.length() + 2 ; // + CR and LF
        int iOffset;
        int iBatt = 0;
        final int V_LEN = 4; // format 'nnnn' mV
        String result = "NONE";
        String sResult = "0000";
        result = ata.send(sCmd + "\r"); 
        if (result.indexOf("OK") >= 0) {
            iOffset = result.indexOf(":") + 2 ;
            result = result.substring(iOffset,iOffset + V_LEN);
            sResult = "Battery: " + result + " mV";
            iBatt = Integer.valueOf(result).intValue();
        } else {
            result = "Battery ERROR";
        }
        return (int)iBatt;
    } // getBattery
    
    /**
    * Returns the Signal Quality of the TC65 module (AT+CSQ).
    * <p>
    * API delivers <br>
    * rssi (received signal strengh indicator and  <br>
    * ber (bit error rate) <br>
    * only rssi will be returned but translated to dBm dimension.  <br>
    * 0 = -113 dBm or less <br>
    * 1-31 = -111 .. -51dBm (i.e. -2 dB per step) <br>
    * 99 = not known or not detectable <br>
    * <p>
    * @return String 'Received Signal Strength (dBm)'
    */
    public String getSignalQuality() throws ATCommandFailedException {
        final String sCmd = "AT+CSQ";
        String result = "NONE";
        result = ata.send(sCmd + "\r"); 
        if (result.indexOf("OK") >= 0) {
            int offset = result.indexOf(",");
            result = result.substring(offset-2,offset);
            result = result.trim(); // removes possible white space
         System.out.println("Signal Quality: " + result + "(rssi)");
           // translation to dBm
            if (! result.equals("99")) {
                int rssi = Integer.valueOf(result).intValue();
                int dBm = 113 - (rssi*2);
                result = "-" + dBm + "dBm";
            }
        } else {
            result = sCmd + " ERROR";
        }
        System.out.println("Signal Quality: " + result);
        return result;
    } // getSignalQuality
    
    public boolean checkPort9() throws ATCommandFailedException {
        boolean bPortState = false;
        final String sCmd = "at^sgio=8";
        int iCmdLen = sCmd.length() + 2 ; // + CR and LF
        int iOffset;
        String result = "NONE";
        String sResult = "0000";
        result = ata.send(sCmd + "\r"); 
        if (result.indexOf("OK") >= 0) {
            iOffset = result.indexOf(":") + 2 ;
            result = result.substring(iOffset,iOffset + 1);
            if (result.equals("1") ) { bPortState=true; }
        } else {
            bPortState = false;
        }
        return bPortState;
    } // getCheckPort9()
    
    /**
    * Returns the Storage size of memory MT of the SIM/TC65 module.
    *
    * Size will be filtered as substring out of ATCommand SMSL response.
    */
    public int getMTsize() throws ATCommandFailedException {
        final String sCmd = "AT^SLMS";
        int iCmdLen = sCmd.length() + 2 ; // + CR and LF
        int iOffset;
        final int LEN = 2; // format '^SLMS: "MT",45,10'
        String result = "NONE";
        int size = 0;
        result = ata.send(sCmd + "\r"); 
            System.out.println(result);
        if (result.indexOf("OK") >= 0) {
            iOffset = result.indexOf(",") + 1 ;
            result = result.substring(iOffset,iOffset + LEN);
            size = Integer.valueOf(result).intValue();
        } else {
            System.out.println(result);
            size = 0;
        }
        return size;
    } // getMTsize

    /**
    * Activate Unsolicated Result Codes (URC) reporting  [AT+CMER].
    * <p>
    * @return boolean
    */
    public boolean activateURC() throws ATCommandFailedException {
        final String sCmd = "AT+CMER=3,0,0,2";
        String result = "NONE";
        boolean status = false;
        result = ata.send(sCmd + "\r"); 
            System.out.println(result);
        if (result.indexOf("OK") >= 0) {
            status = true;
        } else {
            status = false;
        }
        return status;
    } // end activateURL()
    
    /**
    * Activate Unsolicated Result Codes (URC) reporting  [AT+CMER].
    * <p>
    * @return boolean
    */
    public boolean testSMS() throws ATCommandFailedException {
        final String sCmd = "AT^SMGL";
        String result = "NONE";
        boolean status = false;
        result = ata.send("AT+CMGF=1" + "\r"); // set TEXT mode first
        result = ata.send(sCmd + "\r"); 
            //System.out.print(".");
        if (result.indexOf("%") >= 0) {
            status = true;
        } else {
            status = false;
        }
        return status;
    } // end testSMS()
    
    /**
    * check for calls [AT+CLCC].
    * <p> example: +CLCC: 1,1,4,0,0,"+49662184691",145
    * @return boolean
    */
    public boolean testCall() throws ATCommandFailedException {
        final String sCmd = "AT+CLCC";
        String result = "NONE";
        boolean status = false;
        result = ata.send(sCmd + "\r"); 
        System.out.println("test SMS: " + result);
        if (result.indexOf("+49") >= 0) {status = true; }
        if (result.indexOf("?")   >= 0) {status = true; }
        if (result.indexOf("Help")>= 0) {status = true; }
        return status;
    } // end testCall()
    
    /**
    * check for calls [AT+CLCC].
    * <p> example: +CLCC: 1,1,4,0,0,"+49662184691",145
    * @return String e.g. "+49662184691"
    */
    public String getCaller() throws ATCommandFailedException {
        final String sCmd = "AT+CLCC";
        String result = "NONE";
        String sNumber = "UNKNOWN";
        boolean status = false;
        result = ata.send(sCmd + "\r"); 
        if (result.indexOf("+49") >= 0) {
            status = true; 
            int posA = result.indexOf("\"+49") + 1; // -1 wenn kein "+49
            int posB = result.indexOf("\",");        // -1 wenn kein ",
            if (!((posA*posB)==0)) // 0 wenn posA und/oder posB = 0
            {
                sNumber = result.substring(posA,posB);
            }
            System.out.println(posA + " " + posB + " " + sNumber);
        }
        return sNumber;
    } // end getCaller()
    
    /**
    * Activate Unsolicated Result Codes (URC) reporting  [AT+CMER].
    * <p>
    * @return boolean
    */
    public String getNewSMS() throws ATCommandFailedException {
        final String sCmd = "AT+CMGL";
        String result = "NONE";
        boolean status = false;
        result = ata.send("AT+CMGF=1" + "\r"); // set TEXT mode first
        result = ata.send(sCmd + "\r"); 
            //System.out.print(".");
        if (result.indexOf("%") >= 0) {
            result = result;
        } else {
            result = "none";
        }
        return result;
    } // end getNewSMS()
    
    /**
    * Returns the Storage size of memory MT of the SIM/TC65 module.
    *
    * Size will be filtered as substring out of ATCommand SMSL response.
    */
    public void deleteMTindex(int index) throws ATCommandFailedException {
        final String sCmd = "AT+CMGD=" + index;
        String result = ata.send(sCmd + "\r"); 
        System.out.print(result + " deleted");
    } // deleteMTindex
    
    /**
    * Returns the Day of the TC65 module (format "mm\dd" fixed).
    *
    * Day will be filtered as substring out of ATCommand response.
    */
    public String getDay() throws ATCommandFailedException {
        String rtc = "at+cclk?\r";
        int iCmdLen = rtc.length(); 
        int offset;
        final int MM_DD_LEN = 5; // format 'mm/dd'
        String result = "NONE";
        //result = ata.send("at+cclk?" + "\r"); 
        result = getAT("at+cclk?","OK"); 
        if (result.indexOf("OK") >= 0) {
            offset = result.indexOf("/") + 1 ;
            result = result.substring(offset,offset + MM_DD_LEN);
        } else {
            result = "RTC ERROR";
        }
        // System.out.println("Month/Day: " + result);
        return result;
    } // getDay 
    
    /**
    * Returns the RTC of the TC65 module (format "YY/MM/DD,hh:mm:ss" fixed).
    *
    * Day will be filtered as substring out of ATCommand response.
    */
    public String getRTC() throws ATCommandFailedException {
        String rtc = "at+cclk?\r";
        int iCmdLen = rtc.length(); 
        int offset;
        final int RTC_LEN = 17; // format 'YY/MM/DD,hh:mm:ss' 
        String result = "NONE";
        //result = ata.send("at+cclk?" + "\r"); 
        result = getAT("at+cclk?","OK"); 
        if (result.indexOf("OK") >= 0) {
            offset = result.indexOf("/") - 2 ;
            result = result.substring(offset,offset + RTC_LEN);
        } else {
            result = "RTC ERROR";
        }
        // System.out.println("Month/Day: " + result);
        return result;
    } // getRTC 
    
    public String testGPIO() {
        return "// TODO: GPIO coding";
    }

    /**
    * Delays the actual Thread (in milli seconds).
    *
    * needed to control necessary timeouts after ATCommand sending.
    */
    public void delay(long milliSeconds) {
        String time = delayTime(milliSeconds);
        System.out.println("Delay " + time + " ...");
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    } // end delay()

    /**
    * Converts milli seconds in appropriate time scale
    *
    */
    public static String delayTime(long milliSeconds) {
        long value = milliSeconds; String unit = "ms";
        int s=1000; int m=1000*60; int h=1000*60*60;
        if (milliSeconds >= s) {value = milliSeconds / s; unit = "s";}
        if (milliSeconds >= m) {value = milliSeconds / m; unit = "m";}
        if (milliSeconds >= h) {value = milliSeconds / h; unit = "h";}
        return Long.toString(value) + unit;
   } // end delayTime()
        
}  // end class TC65Terminal



