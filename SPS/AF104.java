/**
 * Copyright (C) 2008, Michael Gries. All Rights reserved.
 *
 * AF104 sends and receives SMS to control the AF104 environment. 
 * Siemens TC65 Terminal with GPIO functionality 
 *
 */
package name.gries; 


import  name.gries.common.*;
import  javax.microedition.midlet.*;
import  com.siemens.icm.io.*; 
import  java.util.Timer;
import  java.util.TimerTask;
import  java.util.Date;
import  java.util.Hashtable;
import  java.util.Vector;
import  java.util.Enumeration;
import  name.gries.common.Astro;
import  name.gries.common.EventList;


/**
 * MIDlet for control Siemens TC65 terminal via GPIO
 */
//public abstract class AF104 extends MIDlet implements ATCommandListener {
public class AF104 extends MIDlet {
    
    final boolean SIMULATE  = false;
    boolean SIMULATE_TC65T = false;
    boolean SIMULATE_TC65T_PORT9 = true;
    boolean TC65_TERMINATE_BY_CALL = false;
    boolean TC65_TERMINATE_BY_SMS = false;
    boolean bAF104Terminal = false;     // set by IMEI check
    String  sUsedTerminal = "not checked yet"; // set by IMEI check
    boolean bUserAbort = false; // ends main loop if true
    String  sUserAbortReason = "unknown";
    boolean bAstroActivated = true; // Astro Funktion generell
    boolean bAbsent = false; // Astro Funktion für Flurlicht
    boolean bTestRTCSupply = false; // activated at Timer Taeglich
    boolean bTestRTCEvent = false; // activated at Timer Taeglich
    boolean bCheckPrepaid = false; // Prepaid Account
    boolean SIMULATE_TIME = false;
    static boolean noFTP = false;  // set to true in case of testing
    static boolean noSMS = true;
    static boolean noEventSMS = false; // controls SMS signaling for Event
    
    
    final String J2ME       = "MIDlet";
    final String APP        = "AF104";
    final String ACTIVE     = "startApp()";
    final String PAUSED     = "pauseApp()";
    final String DESTROYED  = "destroyApp()";
    
    final  String ADMIN  = "+491702237454";
    static String CdPA = "0662184691"; 
    static String CdPA1 = "01702237454"; 
    static String CLIP = "i";
    String sDay = "NONE";

    String SMS_TEXT = APP;
    String sNotification = "no Notification";
    String sIMEI = "35";
    boolean bTimerSMS = false;
    String sAstro = "Astro not updated for today"; // in Timer Routine für SMS-Versand
    String account; // FONIC Guthaben (s. URC Auswertung)
    

    boolean boolAussenlichtEin = false;
    boolean boolAussenlichtAus = false;
    long    longAussenlichtTimer = 1000 * 60 *3; // 3 min
    
    // diverse Timer
    private Timer timAussenlicht = new Timer();
    private AussenlichtAus timerAussenlicht;
    private Timer timDaylightOn = new Timer();
    private TageslichtEIN timerTageslichtON;
    private Timer timDayTime = new Timer();
    private DayTime timerDayTime;
    private Timer timDaylightOff = new Timer();
    private TageslichtAUS timerTageslichtOFF;
    private Timer timNightTime = new Timer();
    private NightTime timerNightTime;
    private Timer timDaily = new Timer();
    private Taeglich dailyRun;
    
    String response = "a";
    //GPIO device = new GPIO();
    
    //Simulation simulationSPS = new Simulation();
    //Trace Debug = new Trace();
    //Astro astro = new Astro();
    // TC65Terminal tc65 = new TC65Terminal();
    // GPIO gpio = new GPIO();
    TC65Terminal tc65;
    GPIO gpio;
    
    Trace Debug;
    Astro astro;
    WhiteList caller;
    EventList event;
    Status status;
    Watchdog watchdog;
    //ATListenerSPS atLis;

    

    /** Internal class that provides a TimerTask.   */
    class AussenlichtAus extends TimerTask {

        /**
         * Public constructor: 
         */
        public AussenlichtAus() {
        }

        /**
         * As the timer fires, this method is invoked. 
         */
        public void run() {
            boolAussenlichtAus = true;
            Debug.print("Timer Aussenlicht Aus");
            boolAussenlichtEin = false; // wieder scharfmachen
        }
    } // end class AussenLichtAus


    /** Internal class that provides a TimerTask. */
    class Taeglich extends TimerTask {
        
        Date tagRunsAt = new Date();
        public long tagPeriod = 1000 * 60 * 60 * 24; // ms * s * m * h = h hours
        
        /** Public constructor:  */
        public Taeglich(long period, Date at) {
            tagRunsAt = at;
            tagPeriod = period;
            String hours = tc65.delayTime(tagPeriod);
            String time = Debug.convertTimeToString(tagRunsAt);
            System.out.println("activate Timer(Taeglich): alle " + hours + " um " + time);
        }
        
        /** As the timer fires, this method is invoked.<p>
         * This method should NOT contain any AT Commands since this Thread <br>
         * is not synchronized with main application method.
         */
        public void run() {
            sDay = Debug.getAstroDay();
            String time = Debug.convertTimeToString(tagRunsAt);
            Debug.print("Timer(Taeglich): ab " + sDay + " um " + time);
            String sDayLight = astro.updateNightTime(sDay);
            sAstro = "%Astro= function for day " + sDay + " > " + sDayLight;
            //System.out.println(sAstro);
            Debug.print(sAstro);
            tc65.delay(1000); // for print to Thermodrucker
            bTimerSMS = true; // SMS wird später in Endless Loop versendet
            
            // 4am: Nacht = ja und Lichtzeit = nein
            astro.setNighttime();
            astro.resetLighttime();
            
            bTestRTCSupply = true; // tests RTC supply in Main Loop
            if (event.testEvent(sDay)) {
                bTestRTCEvent = true; // tests Events on a given day in Main Loop
                sNotification = event.getEvent(sDay);
            }
            
            bCheckPrepaid = true; // checks FONIC Prepaid accound daily

            // Reihenfolge beachten, da Timer auch aufgerufen werden wenn der Termin
            // schon abgelaufen ist. Damnit die Flags lightTime und nightTime immer 
            // richtig gesetzt sind.
            
            // Änderung: wegen Reihenfolge wurden die Timer nacheinander 
            // aktiviert, d.h ein Timer aktiviert den nächsten
            
            /**** Gesamttest kann mit Uhrzeit 23:50 optimal erfolgen *****/
            
            /* Timer Reihenfolge:
             * - TageslichtEIN
             * - DayTime
             * - NightTime
             * - TageslichtAUS
             */
            if (bAstroActivated) {
                timerTageslichtON = new TageslichtEIN(astro.dWdStart);
                timDaylightOn.schedule(timerTageslichtON, astro.dWdStart);
                tc65.delay(2000); // 2s delay if next timer start imediately
            }

             /*
            timerDayTime = new DayTime(astro.rtcStart);
            timDayTime.schedule(timerDayTime, astro.rtcStart);

            timerNightTime = new NightTime(astro.rtcStop);
            timNightTime.schedule(timerNightTime, astro.rtcStop);
            
            timerTageslichtOFF = new TageslichtAUS(astro.dWdStop);
            timDaylightOff.schedule(timerTageslichtOFF, astro.dWdStop);
             */

        }
    } // end subclass Taeglich


    /** Internal class that provides a TimerTask. */
    class TageslichtEIN extends TimerTask {
        Date tagRunsAt = new Date();
        /** Public constructor:  */
        public TageslichtEIN(Date at) {
            tagRunsAt = at;
            String time = Debug.convertTimeToString(tagRunsAt);
            System.out.println("activate Timer(TageslichtEIN) um : " + time);
        }
        /** As the timer fires, this method is invoked. */
        public void run() {
            if (bAstroActivated) {
                astro.setLighttime();
                String time = Debug.convertTimeToString(tagRunsAt);
                Debug.print("Timer(TageslichtEIN): Licht aktiv (" + time + ")" );
                System.out.println("LightTime=true / NightTime=" + astro.checkNighttime());
                if (astro.checkNighttime() & bAbsent) {
                    tc65.setFlurLicht();
                }
                timerDayTime = new DayTime(astro.rtcStart);
                timDayTime.schedule(timerDayTime, astro.rtcStart);
                tc65.delay(2000); // 2s delay if next timer start imediately
            }
        }
    } // end class internal TageslichtEIN

    /** Internal class that provides a TimerTask. */
    class DayTime extends TimerTask {
        Date tagRunsAt = new Date();
        /** Public constructor:  */
        public DayTime(Date at) {
            tagRunsAt = at;
            String time = Debug.convertTimeToString(tagRunsAt);
            System.out.println("activate Timer(DayTime) um : " + time);
        }
        /** As the timer fires, this method is invoked. */
        public void run() {
            if (bAstroActivated) {
                astro.resetNighttime();
                String time = Debug.convertTimeToString(tagRunsAt);
                Debug.print("Timer(DayTime): Astro Tagesanbruch (" + time + ")" );
                System.out.println("NightTime=false / LightTime=" + astro.checkLighttime());
                if (astro.checkLighttime() & bAbsent) {
                    tc65.resetFlurLicht();
                }
                timerNightTime = new NightTime(astro.rtcStop);
                timNightTime.schedule(timerNightTime, astro.rtcStop);
                tc65.delay(2000); // 2s delay if next timer start imediately
            }
        }
    } // end class internal TageslichtAUS

    /** Internal class that provides a TimerTask. */
    class NightTime extends TimerTask {
        Date tagRunsAt = new Date();
        /** Public constructor:  */   
        public NightTime(Date at) {
            tagRunsAt = at;
            String time = Debug.convertTimeToString(tagRunsAt);
            System.out.println("activate Timer(NightTime) um : " + time);
        }
        /** As the timer fires, this method is invoked. */
        public void run() {
            if (bAstroActivated) {
                astro.setNighttime();
                String time = Debug.convertTimeToString(tagRunsAt);
                Debug.print("Timer(NightTime): Astro Nachtanbruch (" + time + ")" );
                System.out.println("NightTime=true / LightTime=" + astro.checkLighttime());
                if (astro.checkLighttime() & bAbsent) {
                    tc65.setFlurLicht();
                }
                timerTageslichtOFF = new TageslichtAUS(astro.dWdStop);
                timDaylightOff.schedule(timerTageslichtOFF, astro.dWdStop);
                tc65.delay(2000); // 2s delay if next timer start imediately
            }
       }
    } // end class internal TageslichtEIN
    
    /** Internal class that provides a TimerTask. */
    class TageslichtAUS extends TimerTask {
        Date tagRunsAt = new Date();
        /** Public constructor:  */
        public TageslichtAUS(Date at) {
            tagRunsAt = at;
            String time = Debug.convertTimeToString(tagRunsAt);
            System.out.println("activate Timer(TageslichtAUS) um : " + time);
        }
        /** As the timer fires, this method is invoked. */
        public void run() {
            if (bAstroActivated) {
                astro.resetLighttime();
                String time = Debug.convertTimeToString(tagRunsAt);
                Debug.print("Timer(TageslichtAUS): Licht inaktiv (" + time + ")" );
                System.out.println("LichtTime=false / NightTime=" + astro.checkNighttime());
                if (astro.checkNighttime() & bAbsent) {
                    tc65.resetFlurLicht();
                }
            }
        }
    } // end class internal TageslichtAUS

    /**
    * Default constructor 
    */
    public AF104() {
        
        name.gries.Display.testSequence();
        
        System.out.println(J2ME + ": " + APP + " - " + "Constructor");
        tc65    = new TC65Terminal();
        gpio    = new GPIO();
        astro   = new Astro();
        Debug   = new Trace();
        caller  = new WhiteList();
        event   = new EventList();
        status  = new Status();
        watchdog  = new Watchdog();
        //atLis   = new ATListenerSPS();
    }

    /**
     * This is the main application entry point. Here we simply give some
     * text output and close the application immediately again.
     */
    public void startApp() throws MIDletStateChangeException {
        
        
        
        System.out.println(J2ME + ": " + APP + " - " + ACTIVE);
        try {
            long startupTimeMillis = System.currentTimeMillis();
            String startupTime = Long.toString(startupTimeMillis); // static wrapper method
            Debug.print("Startup timestamp (long): " + startupTime);
            
            // simulationSPS.deleteMTmemory();
            // simulationSPS.printATtest();
            // Simulation.getRandomOffsetTime();
            this.debugAppProperties();
            
            tc65.activateATparser();
            tc65.init();
            
            tc65.listFs();
            long fFS = tc65.getFreeFlashSpace();
            System.out.println("Free Flash Space: " + fFS + " Byte");
            String prop = "voltage.txt";
            String val  = "voltage[2009][314] = \"1234\" // mV" ;
            tc65.setProp(prop,val);
            prop = "signal.txt";
            val  = "signal[2009][314] = \"-101\" // dBm" ;
            tc65.setProp(prop,val);
            fFS = tc65.getFreeFlashSpace();
            System.out.println("Free Flash Space: " + fFS + " Byte" + "\r\n");
           
            int iBaud = tc65.getBaudRate();
            switch (iBaud) {
                case 19200:
                    Debug.bThermoPrinter = true;
                    status.updatePrinter("Drucker EIN");
                    System.out.println("Baudrate 19200: Thermoprinter activated");
                    break;
                case 115200:  
                    Debug.bThermoPrinter = false;
                    status.updatePrinter("Drucker AUS");
                    System.out.println("Baudrate 115200: no Thermoprinter usable");
                    break; 
                default:
                    break;
            }
                    

            System.out.println("Start GPIO test ..."); 
            String sTest = tc65.testGPIO();
            System.out.println(sTest); 
            // System.out.println("\\1"); // Thermoprinter

            String response = gpio.getDevicePort("AUSSENLICHT");
            System.out.println("Steuerung: " + response);

            boolean b = tc65.activateURC();

            int iBatt = tc65.getBattery(); //Battery voltage (mV))
            Debug.print("Battery: " + iBatt + " mV");
            

            //GPIO Pin5 Aussenlicht (high activ)
            tc65.deactivateGPIO();
            tc65.activateGPIO();
            tc65.activatePort5();
            tc65.activatePort7();
            tc65.activatePort8();
            tc65.activatePort10();
            
            System.out.println("\r\n\n\n GPIOstatic test");
            tc65.gpio.resetTestLicht();

                        
            //GPIO Pin9 as Input (polling) 
            // ACHTUNG: muss vor Timer Initialisierung stehen
            System.out.println("\nActivating Input Ports ... \n");
            tc65.activatePort9();
            boolean bPoll = tc65.updateAbwesenheit();
            System.out.println("Port 9: " + bPoll);
            if (bPoll != bAbsent) { // = status change
                if (bPoll) {
                    bAbsent = true;
                    status.updateAbsent("Abwesenheit EIN");
                    Debug.print("Abwesenheit aktiviert (ON)");
                } else {
                    bAbsent = false;
                    status.updateAbsent("Abwesenheit AUS");
                    Debug.print("Abwesenheit deaktiviert (OFF)");
                }
            }

            if (SIMULATE_TIME) {
                tc65.sendAT("AT+CCLK=\"08/09/04,23:50:30\"","OK");
            }
            
            sIMEI = tc65.getIMEI();
            sUsedTerminal = this.checkIMEI(sIMEI);
            Debug.print("IMEI: " + sIMEI + " " + sUsedTerminal);
            if (sUsedTerminal.indexOf("AF104") != -1) {
                bAF104Terminal = true;
                sUsedTerminal = "AF104";
            }
            if (sUsedTerminal.indexOf("TC65T") != -1) {
                SIMULATE_TC65T = false;  // only for test purposes
                sUsedTerminal = "TC65T";
            }
            
            String sRTC = tc65.getRTC();
            boolean bOn = watchdog.dateCompare(sRTC);
            System.out.println("********* bON=" + bOn);
            watchdog.activateWatchdog();
            
            String rtcDay = Debug.getAstroDay();
            
            // check Whitelist user
            System.out.println("\r\n" + "Whitelist: ");
            System.out.println(caller.printWhitelist());
            String msgList;
            msgList= caller.printWhitelistKeys();
            if (msgList.length() > 140) msgList = msgList.substring(0,140-3)+ "...";
            System.out.println("\r\n");
            System.out.println(msgList);
            msgList = caller.printWhitelistUser();
            if (msgList.length() > 140) msgList = msgList.substring(0,140)+ "...";
            System.out.println("\r\n");
            System.out.println(msgList);
            //tc65.sendSms(ADMIN, msgList );
            System.out.println("\r\n");
            
            String testuser = "017696735022";
            String atResponse = "Default";
            if (caller.checkCallerAllowed(testuser)) {
                atResponse = caller.getCallerStatus(testuser);
                Debug.print("Test-Caller: " + testuser + " = " + atResponse);
            } else {
                Debug.print("Test-Caller:   " + testuser + " = Caller not identified");
            }
            testuser = "+4917696735022";
            if (caller.checkCallerAllowed(testuser)) {
                atResponse = caller.getCallerStatus(testuser);
                Debug.print("Test-Caller: " + testuser + " = " + atResponse);
            } else {
                Debug.print("Test-Caller: " + testuser + " = Caller not identified");
            }
            
            
          
            final long ONE_MINUTE = 1000 *60;
            Date now = new Date();
            Date in1Minutes = new Date(now.getTime() + 1L * ONE_MINUTE);
            Date in2Minutes = new Date(now.getTime() + 2L * ONE_MINUTE);
            Date in3Minutes = new Date(now.getTime() + 3L * ONE_MINUTE);

            /* check for Astro functionality once */
            Date at4am  = astro.convertTime("04:00:00");
            long lPeriod = 1000 * 60 * 60 * 24 ;    // ms * s * m * h = h hours
            if (SIMULATE) {
                lPeriod = 1000 * 60 * 60 ;          // ms * s * m = m minutes
            } 
            //if (bAstroActivated) {
                dailyRun = new Taeglich(lPeriod, at4am);
                //tc65.delay(1000*2); // delay required if Timer raises immediately
                timDaily.scheduleAtFixedRate(dailyRun, at4am, dailyRun.tagPeriod);
                tc65.delay(10000); // 10s delay required if all 4 Timer raises immediately
            //}

            Debug.print("Active Loop until SMS %exit= or Caller=HOME ...");
            // System.out.println("\\1"); // Thermoprinter

            
            // name.gries.Display.testSequence();

            while (bUserAbort == false) {

                if (boolAussenlichtAus) {
                    this.boolAussenlichtAus = false;
                    tc65.resetAussenLicht();
                }
                // FONIC Account check - must be before TC65.updateFTP() method
                if (bCheckPrepaid) {
                    bCheckPrepaid = false; // zurücksetzen beachten
                    tc65.checkPrepaid();
                    tc65.delay(5000); // notwendig, damit URC auftritt zwecks einmaliger Auswertung
                    account = tc65.getAccount();
                    prop = "account"+sUsedTerminal+".txt";
                    val  = "account[2009][\"" + sDay + "\"] = " + account + "; // EUR" + "\r\n" ;
                    System.out.println("Set Property: " + prop + "=" + val);
                    tc65.setProp(prop,val);
                    System.out.println("Called: tc65.getAccount() = " + account);
                    status.updateAccount("FONIC " + account + " EUR");
                 }
                
                if (bTestRTCSupply) {
                    sRTC = tc65.getRTC();
                    watchdog.setDateReference(sRTC);
                    bTestRTCSupply = false; // zurücksetzen beachten
                    int voltage = tc65.getADC0();
                    Debug.print("RTC supply: " + voltage + " mV");
                    status.updateVoltageRTC("RTC " + voltage + "mV");
                    prop = "voltage"+sUsedTerminal+".txt";
                    val  = "voltage[2009][\"" + sDay + "\"] = " + voltage + "; // mV"  + "\r\n" ;
                    System.out.println("Set Property: " + prop + "=" + val);
                    tc65.setProp(prop,val);
                    if (voltage < 2400 && bAF104Terminal) // min allowed voltage 2400mV
                    {
                        String msg = "WARNING: \nRTC supply voltage below 2400mV - change Battery";
                        tc65.sendSms(ADMIN, msg + " (" + voltage + "mV)");
                        tc65.delay(5000);
                    }
                    String csq = tc65.getSignalQuality();
                    status.updateSignalQuality("Signal " + csq );
                    prop = "signal"+sUsedTerminal+".txt";
                    csq=csq.substring(0,csq.length()-3);
                    val  = "signal[2009][\"" + sDay + "\"] = " + csq + "; // dBm" + "\r\n" ;
                    System.out.println("Set Property: " + prop + "=" + val);
                    tc65.setProp(prop,val);
                    if (! noFTP)  {
                        System.out.println("FTP: connection to www.gries.name ");
                        tc65.uploadFtp(sUsedTerminal);
                    } else {
                         System.out.println("FTP: upload suppressed due to testing");
                    }
               }
                
                
                if (bTestRTCEvent) {
                    bTestRTCEvent = false; // zurücksetzen beachten
                    if (bAF104Terminal && ! noEventSMS) // TODO dependencies
                    {
                        String sMessage = "Notification: " + sNotification;
                        tc65.sendSms(ADMIN, sMessage);
                        tc65.delay(5000);
                        Debug.print(sMessage);
                    }
                }
                
                bPoll = tc65.updateAbwesenheit();
                if (bPoll != bAbsent) { // = status change
                    if (bPoll) {
                        bAbsent = true;
                        status.updateAbsent("Abwesenheit EIN");
                        Debug.print("Abwesenheit aktiviert (ON)");
                        dailyRun = new Taeglich(lPeriod, at4am);
                        timDaily = new Timer();
                        timDaily.scheduleAtFixedRate(dailyRun, at4am, dailyRun.tagPeriod);
                        tc65.delay(10000); // 10s delay required if all 4 Timer raises immediately
                    } else {
                        bAbsent = false;
                        status.updateAbsent("Abwesenheit AUS");
                        Debug.print("Abwesenheit deaktiviert (OFF)");
                        timDaily.cancel();
                        tc65.resetFlurLicht();
                    }
                }
                
                if (TC65_TERMINATE_BY_SMS) {bUserAbort = true;} // Schleife verlassen
                
                 // poll after Port 9 test for new SMS received
                
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (tc65.testSMS() == true) {
                    response = tc65.getNewSMS();
                    final String FORMAT = "YY/MM/DD,hh:mm:ss";
                    int LEN = FORMAT.length();
                    System.out.println(">>>SMS>>>");
                    System.out.println(response);
                    int pos0 = response.indexOf("?") + 0; // ? = help
                    int posH = response.indexOf("Help") + 0; // ? = help
                    int posA = response.indexOf("%") + 1; // -1 wenn kein %
                    int posB = response.indexOf("=") + 1; // -1 wenn kein %
                    String sFunction = "none";
                    if (!((posA*posB)==0)) // 0 wenn posA und/oder posB = 0
                    {
                        sFunction = response.substring(posA,posB-1);
                    }
                    System.out.println("pos0: " + pos0);
                    if (pos0 >= 0) {sFunction = "help";}
                    if (posH >= 0) {sFunction = "help";}
                    Hashtable function = new Hashtable();
                    function.put("none",        new Integer(0));
                    function.put("?",           new Integer(1));
                    function.put("help",        new Integer(1));
                    function.put("Help",        new Integer(1));
                    function.put("licht",       new Integer(2));
                    function.put("rtc",         new Integer(3));
                    function.put("astro",       new Integer(9));
                    function.put("astro aus",   new Integer(10));
                    function.put("astro ein",   new Integer(11));
                    function.put("sms aus",     new Integer(20));
                    function.put("sms ein",     new Integer(21));
                    function.put("keys",        new Integer(30));
                    function.put("reset",       new Integer(99));
                    function.put("exit",        new Integer(999));

                    Integer code = new Integer(99); // keine Funktion gefunden
                    if (function.containsKey(sFunction)) {
                        code = (Integer)function.get(sFunction);
                    }
                    int mode = code.intValue();
                    switch (mode) {
                    case 0 :
                        Debug.print("New SMS Received - Command: " + sFunction);
                        break;
 
                    case 1 : 
                        System.out.println ("Help Anfrage");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        String help =   "%licht=         " +
                                        "%rtc=           " +
                                        "%astro aus      " +
                                        "%sms ein=       ";
                        tc65.sendSms(ADMIN,help);
                        tc65.deleteMTindex(1); // Annahme: Platz 1 //TODO : genauer
                        break;

                    case 2 : 
                        System.out.println ("Mein Wert ist Licht");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        tc65.setAussenLicht();
                        System.out.println("Licht ein");
                        timerAussenlicht = new AussenlichtAus();
                        timAussenlicht.schedule(timerAussenlicht, longAussenlichtTimer);
                        String time = TC65Terminal.delayTime(longAussenlichtTimer);
                        Debug.print("Aussenlicht-Timer aktiviert fuer " + time + " ...");
                        String smsMessage = "Aussenlicht EIN " + status.getStatus();
                        tc65.sendSms(CdPA1,smsMessage);
                        tc65.deleteMTindex(1); // Annahme: Platz 1 //TODO : genauer
                        break;
                        
                   case 3: 
                        System.out.println ("Mein Wert ist RTC");
                        String sDate = response.substring(posB,posB+LEN);
                        tc65.setDate(sDate);
                        Debug.print("New SMS Received - Command: " + sFunction + " " + sDate);
                        break;

                   case 9 : 
                        System.out.println ("Astro function Abfrage");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        String message = sAstro + "    %" + sFunction + "= ";
                        tc65.sendSms(ADMIN,message);
                        tc65.deleteMTindex(1); // Annahme: Platz 1 //TODO : genauer
                        break;

                    case 10 : 
                        System.out.println ("Astro function AUS");
                        status.updateAbsent("Astro AUS");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        this.bAstroActivated = false;
                        break;

                    case 11 : 
                        System.out.println ("Astro function EIN");
                        status.updateAbsent("Astro EIN");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        this.bAstroActivated = true;
                        break;

                    case 20 : 
                        System.out.println ("SMS function AUS");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        this.noSMS = true;
                        break;

                    case 21 : 
                        System.out.println ("SMS function EIN");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        this.noSMS = false;
                        String messageSMS = "SMS ist eingeschaltet > fuer %sms aus= diese SMS als Antwort senden";
                        tc65.sendSms(ADMIN,messageSMS);
                        tc65.deleteMTindex(1); // Annahme: Platz 1 //TODO : genauer
                        break;

                    case 30 : 
                        System.out.println ("List Keys");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        this.noSMS = false;
                        msgList= caller.printWhitelistKeys();
                        if (msgList.length() > 140) msgList = msgList.substring(0,140-3)+ "...";
                        System.out.println("\r\n");
                        System.out.println(msgList);
                        tc65.sendSms(ADMIN, msgList );
                        tc65.deleteMTindex(1); // Annahme: Platz 1 //TODO : genauer
                        break;

                    case 99 : 
                        System.out.println ("Reset Terminal");
                        Debug.print("New SMS Received - Command: " + sFunction);
                        this.noSMS = false;
                        smsMessage = "Terminal will be terminated now " + status.getStatus();
                        tc65.sendSms(ADMIN,smsMessage);
                        tc65.deleteMTindex(1); // Annahme: Platz 1 //TODO : genauer
                        tc65.shutdownTC65T();
                        break;

                    case 999 :
                        Debug.print("New SMS Received - Command: " + sFunction);
                        bUserAbort = true; // Scheife verlassen
                        sUserAbortReason = "SMS command %exit=";
                        tc65.deleteMTindex(1); // Annahme: Platz 1 //TODO : genauer
                        break;

                    default :
                        System.out.println ("Ich habe einen anderen Wert." + sFunction);
                    } // end switch
                    
                } // end if(test.SMS)
                
                if (bTimerSMS && ! noSMS) {
                    bTimerSMS = false; // zurücksetzen
                    tc65.sendSms(ADMIN,sAstro);
                }

                if (tc65.getRingState() == true) {
                    System.out.println("getRing=True");
                    if (tc65.testCall() == true) {
                        //System.out.println("Caller +49...");
                        String sCallerID = tc65.getCaller();
                        if (caller.checkCallerAllowed(sCallerID)) {
                            atResponse = caller.getCallerStatus(sCallerID);
                            Debug.print("Caller: " + sCallerID + " = " + atResponse);
                            /*
                            tc65.setPort7();
                            tc65.delay(500);
                            tc65.resetPort7();
                            tc65.setKlingel();
                            tc65.delay(500);
                            tc65.resetKlingel();
                             */
                            
                        } else {
                            Debug.print("Caller: " + sCallerID + " = Caller not identified");
                        }
                        if (sCallerID.equals("+49662184691") == true  ) {
                            System.out.println("Buero => Port9=false");
                            bUserAbort = true;
                            sUserAbortReason = "call from BUERO";
                            TC65_TERMINATE_BY_CALL=true; // für Testzwecke
                        }
                        if (sCallerID.equals("+49662165797") == true  ) {
                            System.out.println("call from Home => User Abort");
                            bUserAbort = true;
                            sUserAbortReason = "call from HOME";
                            TC65_TERMINATE_BY_CALL=true; // für Testzwecke
                        }
                        if (! boolAussenlichtEin && ! bUserAbort) {
                            boolAussenlichtEin = true;
                            tc65.setAussenLicht();
                            System.out.println("Aussenlicht ein");
                            timerAussenlicht = new AussenlichtAus();
                            timAussenlicht.schedule(timerAussenlicht, longAussenlichtTimer);
                            String time = TC65Terminal.delayTime(longAussenlichtTimer);
                            Debug.print("Aussenlicht-Timer aktiviert fuer " + time + " ...");
                            tc65.toggleKlingel();
                            System.out.println("Test Klingelrelais Port8 ... 500ms ....");

/*
                        } else {
                            tc65.resetAussenlicht();
                            System.out.println(response);
                            System.out.println("Licht aus");
 */
                        }
                        tc65.delay(3000); // delay due to toggle not too often
                    } // if testCall()
                    
                } // end if(getRingState) 
                if (TC65_TERMINATE_BY_CALL) {
                    bUserAbort = true; // Abbruchbed. für Testzweck TC65
                } else {
                    // bUserAbort = false; // Loop für Testzwecke bis Call
                }
            } // end while
            
            Debug.print("User abort by: " + sUserAbortReason );
            Debug.printStack(); // Debug daten erneut ausgeben.
            String stack = Debug.writeStack(); // .printStack MUSS zuerst sein
            System.out.println("Writing STACK to Logfile ("+sUsedTerminal+".log) ...");
            response = tc65.setProp("log"+sUsedTerminal+".txt", stack);

            if (SIMULATE_TC65T) {
                tc65.delay(1000 * 60 * 60 * 24); // delay wenn kein Port9 angeschlossen
                //tc65.delay(1000 * 60 * 5); // delay wenn kein Port9 angeschlossen
            } 

        } catch (ATCommandFailedException e) {
            System.out.println("ATCommandFailedException: " + e);
            //destroyApp(true);
        } catch (IllegalStateException s) {
            System.out.println("IllegalStateException: " + s);
            //destroyApp(true);
        } catch (IllegalArgumentException a) {
            System.out.println("IllegalArgumentException: " + a);
            //destroyApp(true);
        } finally {
            System.out.println(J2ME + ": " + APP + " - " + ACTIVE + " - finally");
            tc65.resetFlurLicht();
            tc65.resetAussenLicht();
            pauseApp();
        } 
    }

    /**
    * Called when the application has to be temporary paused.
    * 
    * Not used in this application directly.
    */
    public void pauseApp() {
        System.out.println(J2ME + ": " + APP + " - " + PAUSED);
        destroyApp(true);
    } // end pauseApp()

    /**
    * Called when the application is destroyed. Here we must clean
    * up everything not handled by the garbage collector.
    * In this case there is nothing to clean.
    */
    public void destroyApp(boolean cond) {
        System.out.println(J2ME + ": " + APP + " - " + DESTROYED + cond);
        if (Debug.bThermoPrinter) { // Thermoprinter
            // 4 Leerzeilen und Perforation (\P) oder Abschneiden (\C)
            System.out.println("\\0 \n\n\n\n \\P \n\n\n\n \\1"); 
        }
        //tc65.shutdownTC65T();
        notifyDestroyed();    
    } // end destroyApp()
    
    /**
     * Method prints all project related application properties.
     *
     */    
    private void debugAppProperties() {
        Vector prop = new Vector(5);
        prop.addElement("Author");
        prop.addElement("IMEI-AF104");
        prop.addElement("IMEI-TC65T");
        prop.addElement("MIDlet-Description");
        String appPropName = prop.lastElement().toString();
        String appPropValue = this.getAppProperty(appPropName);
        for (Enumeration e = prop.elements() ; e.hasMoreElements() ; ) {
            appPropName = e.nextElement().toString();
            appPropValue = this.getAppProperty(appPropName);
            Debug.print(appPropName + " = " + appPropValue);
        }
    } // end debugAppProperties()
    
    /**
     * Method checks if given IMEI is know to project. <p>
     *
     * @param  imei     has be read by AT command
     * @return String   (e.g. unknown, not allowed, identified, approved)
     */
    private String checkIMEI(String imei) {
        final String IMEI0 = "355633004695531"; // TC63
        final String IMEI1 = "355632005221909"; // TC65 (AF104)
        final String IMEI2 = "355632005217881"; // TC65 (TC65T)
        String result = "(unknown to AF104)";
        String imei1 = this.getAppProperty("IMEI-AF104");
        String imei2 = this.getAppProperty("IMEI-TC65T");
        if (imei.equals(IMEI0) ) {result = "(not allowed)"; } 
        if (imei.equals(IMEI1) ) {result = "(identified - AF104)"; } // hardcoded proved
        if (imei.equals(IMEI2) ) {result = "(identified - TC65T)"; } // hardcoded proved
        if (imei.equals(imei1) ) {result = "(approved - AF104)"; } // by Project settings
        if (imei.equals(imei2) ) {result = "(approved - TC65T)"; } // by Project settings
        return result;
    } // end checkIMEI()

} // end class AF104



