/*
 * Astro.java
 *
 * Created on 8. August 2008, 17:21
 *
 */

package name.gries.common;
import  java.util.Hashtable;
import  java.util.Calendar;
import  java.util.Date;
import name.gries.*;

/**
 * Astro functionality for daylight control.
 * <p>
 * Reference: <a href="http://www.volker-quaschning.de/datserv/sunpos/index.html" > 
 * Homepage Volker Quasching </a><p>
 * 
 * Sunrise and sunset times based on calulations for <br>
 * location 9°E / 50°N (Germany  - near Aschaffenburg) <br>
 * since internet source does only calculate for none fractual degrees.<br>
 * Calculated data valid for year 2008.<p>
 * 
 * Ref.: location AF104 - Bad Hersfeld: <br> 9°41?28?E / 50°52?37?N<br>
 *                                          (9.691384° / 50.876987°)<p>
 *
 * @author  <a href="http://www.gries.name/Michael/Gries.shtml">Michael Gries</a>
 * @version 8.9.10
 * @see     AF104
 * @since   2008-08-08
 *
 */
public class Astro extends TC65Terminal {
    /**
     * Hashtable of sunrise times for 366 days
     */
    Hashtable sunRise = new Hashtable();
    /**
     * Hashtable of sunset times for 366 days
     */
    Hashtable sunSet  = new Hashtable();
    
    /**
     * Dämmerungschalter.
     *
     * Aktiv (True) wenn Dämmerung erreicht ist. Automat Theben xxxx
     */
    private boolean nightTime; // controlled by Astro function
    private boolean lightTime; // = AF104 lights are on
    private String sWorkdayStartTime = "06:25:00";
    private String sWorkdayStopTime = "22:52:00";
    private String rtcDayLightRiseTime;
    private String rtcDayLightSetTime;
    public  Date rtcStart;
    public  Date rtcStop;
    public  Date dWdStart;
    public  Date dWdStop;
            
    public void setNighttime() {
        nightTime = true;
    } 
    public void resetNighttime() {
        nightTime = false;
    } 
    public boolean checkNighttime() {
        return nightTime;
    } 
    
    public void setLighttime() {
        lightTime = true;
    } 
    public void resetLighttime() {
        lightTime = false;
    } 
    public boolean checkLighttime() {
        return lightTime;
    } 
    
    public String updateNightTime(String day) {
        String sDayLight = "unknown";
        rtcDayLightRiseTime = getSunRise(day);
        rtcStart = convertTime(rtcDayLightRiseTime);
        dWdStart = convertTime(sWorkdayStartTime);
        rtcDayLightSetTime = getSunSet(day);
        rtcStop = convertTime(rtcDayLightSetTime);
        dWdStop = convertTime(sWorkdayStopTime);
        sDayLight = rtcDayLightRiseTime + "-" + rtcDayLightSetTime;
        if (checkWeekend() == true) {sDayLight = sDayLight + " (Wochenende) ";}
        return sDayLight;
    }
    
    public String getWorkdayStartTime() {
        return sWorkdayStartTime;
    } 
    
    public String getWorkdayStopTime() {
        return sWorkdayStopTime;
    } 

    public boolean checkWeekend(){
        boolean bWeekend = false;
        Date now = new Date(System.currentTimeMillis() );
        String timestamp = now.toString();  // e.g. 'Wed Aug 13 23:47:01 UTC 2008'
        timestamp = timestamp.substring(0,3);
        if (timestamp.equals("Sat") == true) {bWeekend = true;}
        if (timestamp.equals("Sun") == true) {bWeekend = true;}
        return bWeekend;
    }
    
    public static int getRandomOffset() {
        final int MIN =  1; // Minute
        final int MAX = 15; // Minute        
        java.util.Random gen = new java.util.Random();
        int r = gen.nextInt(MAX-MIN+1) + MIN;
        return r;
    }
    
    public Date convertTime(String time){
        String sH = time.substring(0,2);
        String sM = time.substring(3,5);
        String sS = time.substring(6,8);
        int iH = Integer.valueOf(sH).intValue();
        int iM = Integer.valueOf(sM).intValue();
        int iS = Integer.valueOf(sS).intValue();

        Calendar c = Calendar.getInstance();
        //c.set( Calendar.MONTH, Calendar.OCTOBER );
        //c.set( Calendar.DAY_OF_MONTH, 18 );
        //c.set( Calendar.YEAR, 1996 );
        c.set( Calendar.HOUR_OF_DAY, iH );
        c.set( Calendar.MINUTE, iM );
        c.set( Calendar.SECOND, iS );
        //c.set( Calendar.MILLISECOND, 0 );
        Date rtc = c.getTime();
        return rtc;
    }

    /**
     * gets sunrise time for a given date.
     * @param date in format 'MM/DD'
     * @return Sunrise time in format 'hh:mm:ss'
     */
    public String getSunRise(String date) {
        Object o = sunRise.get(date);
        String sunRiseTime = (String)o;
        return sunRiseTime;
    }
    /**
     * gets sunset time for a given date.
     * @param date in format 'MM/DD'
     * @return Sunrise time in format 'hh:mm:ss'
     */
    public String getSunSet(String date) {
        Object o = sunSet.get(date);
        String sunSetTime = (String)o;
        return sunSetTime;
    }
    
    /** Creates a new instance of Astro.
     *  <p>
     *  Initializes both sunrise and sunset times
     *  for a complete year (366 day - incl. February, 29th)
     */
    public Astro() {
        /*
         * SunRise data table
         */
        sunRise.put("01/01","08:24:51");  // -0:00
        sunRise.put("01/02","08:24:47");  // -0:04
        sunRise.put("01/03","08:24:42");  // -0:05
        sunRise.put("01/04","08:24:32");  // -0:10
        sunRise.put("01/05","08:24:19");  // -0:13
        sunRise.put("01/06","08:24:02");  // -0:17
        sunRise.put("01/07","08:23:44");  // -0:18
        sunRise.put("01/08","08:23:21");  // -0:23
        sunRise.put("01/09","08:22:57");  // -0:24
        sunRise.put("01/10","08:22:29");  // -0:28
        sunRise.put("01/11","08:21:57");  // -0:32
        sunRise.put("01/12","08:21:22");  // -0:35
        sunRise.put("01/13","08:20:44");  // -0:38
        sunRise.put("01/14","08:20:05");  // -0:39
        sunRise.put("01/15","08:19:22");  // -0:43
        sunRise.put("01/16","08:18:35");  // -0:47
        sunRise.put("01/17","08:17:47");  // -0:48
        sunRise.put("01/18","08:16:57");  // -0:50
        sunRise.put("01/19","08:16:02");  // -0:55
        sunRise.put("01/20","08:15:04");  // -0:58
        sunRise.put("01/21","08:14:07");  // -0:57
        sunRise.put("01/22","08:13:05");  // -1:02
        sunRise.put("01/23","08:12:00");  // -1:05
        sunRise.put("01/24","08:10:53");  // -1:07
        sunRise.put("01/25","08:09:44");  // -1:09
        sunRise.put("01/26","08:08:33");  // -1:11
        sunRise.put("01/27","08:07:19");  // -1:14
        sunRise.put("01/28","08:06:03");  // -1:16
        sunRise.put("01/29","08:04:45");  // -1:18
        sunRise.put("01/30","08:03:23");  // -1:22
        sunRise.put("01/31","08:02:01");  // -1:22
        sunRise.put("02/01","08:00:36");  // -1:25
        sunRise.put("02/02","07:59:10");  // -1:26
        sunRise.put("02/03","07:57:42");  // -1:28
        sunRise.put("02/04","07:56:11");  // -1:31
        sunRise.put("02/05","07:54:39");  // -1:32
        sunRise.put("02/06","07:53:04");  // -1:35
        sunRise.put("02/07","07:51:29");  // -1:35
        sunRise.put("02/08","07:49:53");  // -1:36
        sunRise.put("02/09","07:48:12");  // -1:41
        sunRise.put("02/10","07:46:32");  // -1:40
        sunRise.put("02/11","07:44:51");  // -1:41
        sunRise.put("02/12","07:43:07");  // -1:44
        sunRise.put("02/13","07:41:21");  // -1:46
        sunRise.put("02/14","07:39:36");  // -1:45
        sunRise.put("02/15","07:37:48");  // -1:48
        sunRise.put("02/16","07:36:00");  // -1:48
        sunRise.put("02/17","07:34:09");  // -1:51
        sunRise.put("02/18","07:32:16");  // -1:53
        sunRise.put("02/19","07:30:24");  // -1:52
        sunRise.put("02/20","07:28:30");  // -1:54
        sunRise.put("02/21","07:26:35");  // -1:55
        sunRise.put("02/22","07:24:39");  // -1:56
        sunRise.put("02/23","07:22:42");  // -1:57
        sunRise.put("02/24","07:20:44");  // -1:58
        sunRise.put("02/25","07:18:45");  // -1:59
        sunRise.put("02/26","07:16:47");  // -1:58
        sunRise.put("02/27","07:14:45");  // -2:02
        sunRise.put("02/28","07:12:44");  // -2:01
        sunRise.put("02/29","07:10:41");  // -2:03
        sunRise.put("03/01","07:08:38");  // -2:03
        sunRise.put("03/02","07:06:34");  // -2:04
        sunRise.put("03/03","07:04:30");  // -2:04
        sunRise.put("03/04","07:02:26");  // -2:04
        sunRise.put("03/05","07:00:20");  // -2:06
        sunRise.put("03/06","06:58:15");  // -2:05
        sunRise.put("03/07","06:56:07");  // -2:08
        sunRise.put("03/08","06:54:01");  // -2:06
        sunRise.put("03/09","06:51:53");  // -2:08
        sunRise.put("03/10","06:49:44");  // -2:09
        sunRise.put("03/11","06:47:36");  // -2:08
        sunRise.put("03/12","06:45:27");  // -2:09
        sunRise.put("03/13","06:43:18");  // -2:09
        sunRise.put("03/14","06:41:08");  // -2:10
        sunRise.put("03/15","06:38:59");  // -2:09
        sunRise.put("03/16","06:36:48");  // -2:11
        sunRise.put("03/17","06:34:39");  // -2:09
        sunRise.put("03/18","06:32:29");  // -2:10
        sunRise.put("03/19","06:30:18");  // -2:11
        sunRise.put("03/20","06:28:08");  // -2:10
        sunRise.put("03/21","06:25:56");  // -2:12
        sunRise.put("03/22","06:23:47");  // -2:09
        sunRise.put("03/23","06:21:35");  // -2:12
        sunRise.put("03/24","06:19:25");  // -2:10
        sunRise.put("03/25","06:17:14");  // -2:11
        sunRise.put("03/26","06:15:04");  // -2:10
        sunRise.put("03/27","06:12:53");  // -2:11
        sunRise.put("03/28","06:10:43");  // -2:10
        sunRise.put("03/29","06:08:32");  // -2:11
        sunRise.put("03/30","07:06:22");  // -2:10
        sunRise.put("03/31","07:04:12");  // -2:10
        sunRise.put("04/01","07:02:02");  // -2:10
        sunRise.put("04/02","06:59:53");  // -2:09
        sunRise.put("04/03","06:57:45");  // -2:08
        sunRise.put("04/04","06:55:36");  // -2:09
        sunRise.put("04/05","06:53:28");  // -2:08
        sunRise.put("04/06","06:51:20");  // -2:08
        sunRise.put("04/07","06:49:12");  // -2:08
        sunRise.put("04/08","06:47:05");  // -2:07
        sunRise.put("04/09","06:45:00");  // -2:05
        sunRise.put("04/10","06:42:53");  // -2:07
        sunRise.put("04/11","06:40:48");  // -2:05
        sunRise.put("04/12","06:38:43");  // -2:05
        sunRise.put("04/13","06:36:39");  // -2:04
        sunRise.put("04/14","06:34:35");  // -2:04
        sunRise.put("04/15","06:32:33");  // -2:02
        sunRise.put("04/16","06:30:31");  // -2:02
        sunRise.put("04/17","06:28:30");  // -2:01
        sunRise.put("04/18","06:26:30");  // -2:00
        sunRise.put("04/19","06:24:31");  // -1:59
        sunRise.put("04/20","06:22:32");  // -1:59
        sunRise.put("04/21","06:20:34");  // -1:58
        sunRise.put("04/22","06:18:38");  // -1:56
        sunRise.put("04/23","06:16:42");  // -1:56
        sunRise.put("04/24","06:14:47");  // -1:55
        sunRise.put("04/25","06:12:54");  // -1:53
        sunRise.put("04/26","06:11:01");  // -1:53
        sunRise.put("04/27","06:09:10");  // -1:51
        sunRise.put("04/28","06:07:19");  // -1:51
        sunRise.put("04/29","06:05:31");  // -1:48
        sunRise.put("04/30","06:03:43");  // -1:48
        sunRise.put("05/01","06:01:57");  // -1:46
        sunRise.put("05/02","06:00:12");  // -1:45
        sunRise.put("05/03","05:58:28");  // -1:44
        sunRise.put("05/04","05:56:46");  // -1:42
        sunRise.put("05/05","05:55:06");  // -1:40
        sunRise.put("05/06","05:53:27");  // -1:39
        sunRise.put("05/07","05:51:49");  // -1:38
        sunRise.put("05/08","05:50:14");  // -1:35
        sunRise.put("05/09","05:48:39");  // -1:35
        sunRise.put("05/10","05:47:07");  // -1:32
        sunRise.put("05/11","05:45:36");  // -1:31
        sunRise.put("05/12","05:44:07");  // -1:29
        sunRise.put("05/13","05:42:41");  // -1:26
        sunRise.put("05/14","05:41:15");  // -1:26
        sunRise.put("05/15","05:39:52");  // -1:23
        sunRise.put("05/16","05:38:31");  // -1:21
        sunRise.put("05/17","05:37:12");  // -1:19
        sunRise.put("05/18","05:35:55");  // -1:17
        sunRise.put("05/19","05:34:40");  // -1:15
        sunRise.put("05/20","05:33:26");  // -1:14
        sunRise.put("05/21","05:32:15");  // -1:11
        sunRise.put("05/22","05:31:08");  // -1:07
        sunRise.put("05/23","05:30:01");  // -1:07
        sunRise.put("05/24","05:28:58");  // -1:03
        sunRise.put("05/25","05:27:56");  // -1:02
        sunRise.put("05/26","05:26:57");  // -0:59
        sunRise.put("05/27","05:26:00");  // -0:57
        sunRise.put("05/28","05:25:06");  // -0:54
        sunRise.put("05/29","05:24:15");  // -0:51
        sunRise.put("05/30","05:23:25");  // -0:50
        sunRise.put("05/31","05:22:40");  // -0:45
        sunRise.put("06/01","05:21:55");  // -0:45
        sunRise.put("06/02","05:21:15");  // -0:40
        sunRise.put("06/03","05:20:35");  // -0:40
        sunRise.put("06/04","05:20:00");  // -0:35
        sunRise.put("06/05","05:19:26");  // -0:34
        sunRise.put("06/06","05:18:57");  // -0:29
        sunRise.put("06/07","05:18:29");  // -0:28
        sunRise.put("06/08","05:18:05");  // -0:24
        sunRise.put("06/09","05:17:43");  // -0:22
        sunRise.put("06/10","05:17:24");  // -0:19
        sunRise.put("06/11","05:17:08");  // -0:16
        sunRise.put("06/12","05:16:54");  // -0:14
        sunRise.put("06/13","05:16:44");  // -0:10
        sunRise.put("06/14","05:16:36");  // -0:08
        sunRise.put("06/15","05:16:32");  // -0:04
        sunRise.put("06/16","05:16:30");  // -0:02
        sunRise.put("06/17","05:16:30");  // -0:00
        sunRise.put("06/18","05:16:34");  // -0:04
        sunRise.put("06/19","05:16:40");  // -0:06
        sunRise.put("06/20","05:16:50");  // -0:10
        sunRise.put("06/21","05:17:02");  // -0:12
        sunRise.put("06/22","05:17:18");  // -0:16
        sunRise.put("06/23","05:17:33");  // -0:15
        sunRise.put("06/24","05:17:55");  // -0:22
        sunRise.put("06/25","05:18:17");  // -0:22
        sunRise.put("06/26","05:18:43");  // -0:26
        sunRise.put("06/27","05:19:10");  // -0:27
        sunRise.put("06/28","05:19:42");  // -0:32
        sunRise.put("06/29","05:20:14");  // -0:32
        sunRise.put("06/30","05:20:49");  // -0:35
        sunRise.put("07/01","05:21:29");  // -0:40
        sunRise.put("07/02","05:22:08");  // -0:39
        sunRise.put("07/03","05:22:52");  // -0:44
        sunRise.put("07/04","05:23:35");  // -0:43
        sunRise.put("07/05","05:24:23");  // -0:48
        sunRise.put("07/06","05:25:12");  // -0:49
        sunRise.put("07/07","05:26:04");  // -0:52
        sunRise.put("07/08","05:26:57");  // -0:53
        sunRise.put("07/09","05:27:52");  // -0:55
        sunRise.put("07/10","05:28:50");  // -0:58
        sunRise.put("07/11","05:29:49");  // -0:59
        sunRise.put("07/12","05:30:50");  // -1:01
        sunRise.put("07/13","05:31:51");  // -1:01
        sunRise.put("07/14","05:32:56");  // -1:05
        sunRise.put("07/15","05:34:02");  // -1:06
        sunRise.put("07/16","05:35:09");  // -1:07
        sunRise.put("07/17","05:36:18");  // -1:09
        sunRise.put("07/18","05:37:29");  // -1:11
        sunRise.put("07/19","05:38:40");  // -1:11
        sunRise.put("07/20","05:39:53");  // -1:13
        sunRise.put("07/21","05:41:07");  // -1:14
        sunRise.put("07/22","05:42:22");  // -1:15
        sunRise.put("07/23","05:43:38");  // -1:16
        sunRise.put("07/24","05:44:55");  // -1:17
        sunRise.put("07/25","05:46:13");  // -1:18
        sunRise.put("07/26","05:47:32");  // -1:19
        sunRise.put("07/27","05:48:53");  // -1:21
        sunRise.put("07/28","05:50:13");  // -1:20
        sunRise.put("07/29","05:51:35");  // -1:22
        sunRise.put("07/30","05:52:58");  // -1:23
        sunRise.put("07/31","05:54:20");  // -1:22
        sunRise.put("08/01","05:55:43");  // -1:23
        sunRise.put("08/02","05:57:08");  // -1:25
        sunRise.put("08/03","05:58:33");  // -1:25
        sunRise.put("08/04","05:59:58");  // -1:25
        sunRise.put("08/05","06:01:24");  // -1:26
        sunRise.put("08/06","06:02:50");  // -1:26
        sunRise.put("08/07","06:04:17");  // -1:27
        sunRise.put("08/08","06:05:43");  // -1:26
        sunRise.put("08/09","06:07:10");  // -1:27
        sunRise.put("08/10","06:08:38");  // -1:28
        sunRise.put("08/11","06:10:05");  // -1:27
        sunRise.put("08/12","06:11:34");  // -1:29
        sunRise.put("08/13","06:13:02");  // -1:28
        sunRise.put("08/14","06:14:31");  // -1:29
        sunRise.put("08/15","06:15:58");  // -1:27
        sunRise.put("08/16","06:17:28");  // -1:30
        sunRise.put("08/17","06:18:56");  // -1:28
        sunRise.put("08/18","06:20:25");  // -1:29
        sunRise.put("08/19","06:21:54");  // -1:29
        sunRise.put("08/20","06:23:23");  // -1:29
        sunRise.put("08/21","06:24:51");  // -1:28
        sunRise.put("08/22","06:26:21");  // -1:30
        sunRise.put("08/23","06:27:50");  // -1:29
        sunRise.put("08/24","06:29:20");  // -1:30
        sunRise.put("08/25","06:30:48");  // -1:28
        sunRise.put("08/26","06:32:18");  // -1:30
        sunRise.put("08/27","06:33:46");  // -1:28
        sunRise.put("08/28","06:35:16");  // -1:30
        sunRise.put("08/29","06:36:44");  // -1:28
        sunRise.put("08/30","06:38:14");  // -1:30
        sunRise.put("08/31","06:39:44");  // -1:30
        sunRise.put("09/01","06:41:12");  // -1:28
        sunRise.put("09/02","06:42:42");  // -1:30
        sunRise.put("09/03","06:44:10");  // -1:28
        sunRise.put("09/04","06:45:40");  // -1:30
        sunRise.put("09/05","06:47:08");  // -1:28
        sunRise.put("09/06","06:48:38");  // -1:30
        sunRise.put("09/07","06:50:06");  // -1:28
        sunRise.put("09/08","06:51:36");  // -1:30
        sunRise.put("09/09","06:53:05");  // -1:29
        sunRise.put("09/10","06:54:34");  // -1:29
        sunRise.put("09/11","06:56:03");  // -1:29
        sunRise.put("09/12","06:57:33");  // -1:30
        sunRise.put("09/13","06:59:01");  // -1:28
        sunRise.put("09/14","07:00:31");  // -1:30
        sunRise.put("09/15","07:02:01");  // -1:30
        sunRise.put("09/16","07:03:30");  // -1:29
        sunRise.put("09/17","07:05:01");  // -1:31
        sunRise.put("09/18","07:06:30");  // -1:29
        sunRise.put("09/19","07:08:00");  // -1:30
        sunRise.put("09/20","07:09:30");  // -1:30
        sunRise.put("09/21","07:10:59");  // -1:29
        sunRise.put("09/22","07:12:30");  // -1:31
        sunRise.put("09/23","07:14:00");  // -1:30
        sunRise.put("09/24","07:15:31");  // -1:31
        sunRise.put("09/25","07:17:01");  // -1:30
        sunRise.put("09/26","07:18:32");  // -1:31
        sunRise.put("09/27","07:20:04");  // -1:32
        sunRise.put("09/28","07:21:35");  // -1:31
        sunRise.put("09/29","07:23:07");  // -1:32
        sunRise.put("09/30","07:24:38");  // -1:31
        sunRise.put("10/01","07:26:11");  // -1:33
        sunRise.put("10/02","07:27:42");  // -1:31
        sunRise.put("10/03","07:29:15");  // -1:33
        sunRise.put("10/04","07:30:48");  // -1:33
        sunRise.put("10/05","07:32:21");  // -1:33
        sunRise.put("10/06","07:33:55");  // -1:34
        sunRise.put("10/07","07:35:28");  // -1:33
        sunRise.put("10/08","07:37:02");  // -1:34
        sunRise.put("10/09","07:38:37");  // -1:35
        sunRise.put("10/10","07:40:10");  // -1:33
        sunRise.put("10/11","07:41:45");  // -1:35
        sunRise.put("10/12","07:43:20");  // -1:35
        sunRise.put("10/13","07:44:56");  // -1:36
        sunRise.put("10/14","07:46:32");  // -1:36
        sunRise.put("10/15","07:48:09");  // -1:37
        sunRise.put("10/16","07:49:44");  // -1:35
        sunRise.put("10/17","07:51:21");  // -1:37
        sunRise.put("10/18","07:52:58");  // -1:37
        sunRise.put("10/19","07:54:36");  // -1:38
        sunRise.put("10/20","07:56:13");  // -1:37
        sunRise.put("10/21","07:57:52");  // -1:39
        sunRise.put("10/22","07:59:30");  // -1:38
        sunRise.put("10/23","08:01:08");  // -1:38
        sunRise.put("10/24","08:02:46");  // -1:38
        sunRise.put("10/25","08:04:25");  // -1:39
        sunRise.put("10/26","08:06:06");  // -1:41
        sunRise.put("10/27","07:07:44");  // -1:41
        sunRise.put("10/28","07:09:25");  // -1:41
        sunRise.put("10/29","07:11:03");  // -1:38
        sunRise.put("10/30","07:12:44");  // -1:41
        sunRise.put("10/31","07:14:24");  // -1:40
        sunRise.put("11/01","07:16:04");  // -1:40
        sunRise.put("11/02","07:17:44");  // -1:40
        sunRise.put("11/03","07:19:25");  // -1:41
        sunRise.put("11/04","07:21:05");  // -1:40
        sunRise.put("11/05","07:22:46");  // -1:41
        sunRise.put("11/06","07:24:26");  // -1:40
        sunRise.put("11/07","07:26:07");  // -1:41
        sunRise.put("11/08","07:27:46");  // -1:39
        sunRise.put("11/09","07:29:26");  // -1:40
        sunRise.put("11/10","07:31:05");  // -1:39
        sunRise.put("11/11","07:32:45");  // -1:40
        sunRise.put("11/12","07:34:24");  // -1:39
        sunRise.put("11/13","07:36:03");  // -1:39
        sunRise.put("11/14","07:37:40");  // -1:37
        sunRise.put("11/15","07:39:19");  // -1:39
        sunRise.put("11/16","07:40:56");  // -1:37
        sunRise.put("11/17","07:42:33");  // -1:37
        sunRise.put("11/18","07:44:08");  // -1:35
        sunRise.put("11/19","07:45:43");  // -1:35
        sunRise.put("11/20","07:47:16");  // -1:33
        sunRise.put("11/21","07:48:51");  // -1:35
        sunRise.put("11/22","07:50:23");  // -1:32
        sunRise.put("11/23","07:51:54");  // -1:31
        sunRise.put("11/24","07:53:24");  // -1:30
        sunRise.put("11/25","07:54:53");  // -1:29
        sunRise.put("11/26","07:56:21");  // -1:28
        sunRise.put("11/27","07:57:47");  // -1:26
        sunRise.put("11/28","07:59:13");  // -1:26
        sunRise.put("11/29","08:00:36");  // -1:23
        sunRise.put("11/30","08:01:58");  // -1:22
        sunRise.put("12/01","08:03:19");  // -1:21
        sunRise.put("12/02","08:04:37");  // -1:18
        sunRise.put("12/03","08:05:55");  // -1:18
        sunRise.put("12/04","08:07:10");  // -1:15
        sunRise.put("12/05","08:08:22");  // -1:12
        sunRise.put("12/06","08:09:33");  // -1:11
        sunRise.put("12/07","08:10:42");  // -1:09
        sunRise.put("12/08","08:11:49");  // -1:07
        sunRise.put("12/09","08:12:52");  // -1:03
        sunRise.put("12/10","08:13:56");  // -1:04
        sunRise.put("12/11","08:14:55");  // -0:59
        sunRise.put("12/12","08:15:51");  // -0:56
        sunRise.put("12/13","08:16:45");  // -0:54
        sunRise.put("12/14","08:17:38");  // -0:53
        sunRise.put("12/15","08:18:28");  // -0:50
        sunRise.put("12/16","08:19:13");  // -0:45
        sunRise.put("12/17","08:19:58");  // -0:45
        sunRise.put("12/18","08:20:39");  // -0:41
        sunRise.put("12/19","08:21:16");  // -0:37
        sunRise.put("12/20","08:21:52");  // -0:36
        sunRise.put("12/21","08:22:23");  // -0:31
        sunRise.put("12/22","08:22:51");  // -0:28
        sunRise.put("12/23","08:23:18");  // -0:27
        sunRise.put("12/24","08:23:40");  // -0:22
        sunRise.put("12/25","08:24:00");  // -0:20
        sunRise.put("12/26","08:24:17");  // -0:17
        sunRise.put("12/27","08:24:30");  // -0:13
        sunRise.put("12/28","08:24:40");  // -0:10
        sunRise.put("12/29","08:24:47");  // -0:07
        sunRise.put("12/30","08:24:51");  // -0:04
        sunRise.put("12/31","08:24:56");  // -0:05
        /*
         * SunSet data table
         */
        sunSet.put("01/01","16:29:55");  // +0:57
        sunSet.put("01/02","16:30:55");  // +1:00
        sunSet.put("01/03","16:32:00");  // +1:05
        sunSet.put("01/04","16:33:05");  // +1:05
        sunSet.put("01/05","16:34:13");  // +1:08
        sunSet.put("01/06","16:35:24");  // +1:11
        sunSet.put("01/07","16:36:36");  // +1:12
        sunSet.put("01/08","16:37:50");  // +1:14
        sunSet.put("01/09","16:39:08");  // +1:18
        sunSet.put("01/10","16:40:27");  // +1:19
        sunSet.put("01/11","16:41:48");  // +1:21
        sunSet.put("01/12","16:43:10");  // +1:22
        sunSet.put("01/13","16:44:34");  // +1:24
        sunSet.put("01/14","16:46:00");  // +1:26
        sunSet.put("01/15","16:47:27");  // +1:27
        sunSet.put("01/16","16:48:57");  // +1:30
        sunSet.put("01/17","16:50:27");  // +1:30
        sunSet.put("01/18","16:51:58");  // +1:31
        sunSet.put("01/19","16:53:32");  // +1:34
        sunSet.put("01/20","16:55:06");  // +1:34
        sunSet.put("01/21","16:56:40");  // +1:34
        sunSet.put("01/22","16:58:17");  // +1:37
        sunSet.put("01/23","16:59:54");  // +1:37
        sunSet.put("01/24","17:01:33");  // +1:39
        sunSet.put("01/25","17:03:11");  // +1:38
        sunSet.put("01/26","17:04:51");  // +1:40
        sunSet.put("01/27","17:06:31");  // +1:40
        sunSet.put("01/28","17:08:11");  // +1:40
        sunSet.put("01/29","17:09:53");  // +1:42
        sunSet.put("01/30","17:11:36");  // +1:43
        sunSet.put("01/31","17:13:17");  // +1:41
        sunSet.put("02/01","17:15:01");  // +1:44
        sunSet.put("02/02","17:16:43");  // +1:42
        sunSet.put("02/03","17:18:27");  // +1:44
        sunSet.put("02/04","17:20:11");  // +1:44
        sunSet.put("02/05","17:21:55");  // +1:44
        sunSet.put("02/06","17:23:38");  // +1:43
        sunSet.put("02/07","17:25:22");  // +1:44
        sunSet.put("02/08","17:27:07");  // +1:45
        sunSet.put("02/09","17:28:51");  // +1:44
        sunSet.put("02/10","17:30:35");  // +1:44
        sunSet.put("02/11","17:32:19");  // +1:44
        sunSet.put("02/12","17:34:04");  // +1:45
        sunSet.put("02/13","17:35:48");  // +1:44
        sunSet.put("02/14","17:37:32");  // +1:44
        sunSet.put("02/15","17:39:15");  // +1:43
        sunSet.put("02/16","17:41:00");  // +1:45
        sunSet.put("02/17","17:42:42");  // +1:42
        sunSet.put("02/18","17:44:25");  // +1:43
        sunSet.put("02/19","17:46:08");  // +1:43
        sunSet.put("02/20","17:47:51");  // +1:43
        sunSet.put("02/21","17:49:34");  // +1:43
        sunSet.put("02/22","17:51:16");  // +1:42
        sunSet.put("02/23","17:52:59");  // +1:43
        sunSet.put("02/24","17:54:40");  // +1:41
        sunSet.put("02/25","17:56:22");  // +1:42
        sunSet.put("02/26","17:58:02");  // +1:40
        sunSet.put("02/27","17:59:44");  // +1:42
        sunSet.put("02/28","18:01:24");  // +1:40
        sunSet.put("02/29","18:03:04");  // +1:40
        sunSet.put("03/01","18:04:44");  // +1:40
        sunSet.put("03/02","18:06:24");  // +1:40
        sunSet.put("03/03","18:08:03");  // +1:39
        sunSet.put("03/04","18:09:42");  // +1:39
        sunSet.put("03/05","18:11:21");  // +1:39
        sunSet.put("03/06","18:13:00");  // +1:39
        sunSet.put("03/07","18:14:39");  // +1:39
        sunSet.put("03/08","18:16:16");  // +1:37
        sunSet.put("03/09","18:17:54");  // +1:38
        sunSet.put("03/10","18:19:31");  // +1:37
        sunSet.put("03/11","18:21:08");  // +1:37
        sunSet.put("03/12","18:22:46");  // +1:38
        sunSet.put("03/13","18:24:23");  // +1:37
        sunSet.put("03/14","18:25:59");  // +1:36
        sunSet.put("03/15","18:27:35");  // +1:36
        sunSet.put("03/16","18:29:12");  // +1:37
        sunSet.put("03/17","18:30:48");  // +1:36
        sunSet.put("03/18","18:32:23");  // +1:35
        sunSet.put("03/19","18:33:59");  // +1:36
        sunSet.put("03/20","18:35:34");  // +1:35
        sunSet.put("03/21","18:37:10");  // +1:36
        sunSet.put("03/22","18:38:44");  // +1:34
        sunSet.put("03/23","18:40:19");  // +1:35
        sunSet.put("03/24","18:41:54");  // +1:35
        sunSet.put("03/25","18:43:29");  // +1:35
        sunSet.put("03/26","18:45:03");  // +1:34
        sunSet.put("03/27","18:46:37");  // +1:34
        sunSet.put("03/28","18:48:12");  // +1:35
        sunSet.put("03/29","18:49:47");  // +1:35
        sunSet.put("03/30","19:51:21");  // +1:34
        sunSet.put("03/31","19:52:55");  // +1:34
        sunSet.put("04/01","19:54:29");  // +1:34
        sunSet.put("04/02","19:56:03");  // +1:34
        sunSet.put("04/03","19:57:37");  // +1:34
        sunSet.put("04/04","19:59:11");  // +1:34
        sunSet.put("04/05","20:00:44");  // +1:33
        sunSet.put("04/06","20:02:19");  // +1:35
        sunSet.put("04/07","20:03:52");  // +1:33
        sunSet.put("04/08","20:05:27");  // +1:35
        sunSet.put("04/09","20:07:00");  // +1:33
        sunSet.put("04/10","20:08:34");  // +1:34
        sunSet.put("04/11","20:10:08");  // +1:34
        sunSet.put("04/12","20:11:41");  // +1:33
        sunSet.put("04/13","20:13:15");  // +1:34
        sunSet.put("04/14","20:14:49");  // +1:34
        sunSet.put("04/15","20:16:23");  // +1:34
        sunSet.put("04/16","20:17:56");  // +1:33
        sunSet.put("04/17","20:19:30");  // +1:34
        sunSet.put("04/18","20:21:04");  // +1:34
        sunSet.put("04/19","20:22:37");  // +1:33
        sunSet.put("04/20","20:24:11");  // +1:34
        sunSet.put("04/21","20:25:43");  // +1:32
        sunSet.put("04/22","20:27:16");  // +1:33
        sunSet.put("04/23","20:28:50");  // +1:34
        sunSet.put("04/24","20:30:23");  // +1:33
        sunSet.put("04/25","20:31:56");  // +1:33
        sunSet.put("04/26","20:33:29");  // +1:33
        sunSet.put("04/27","20:35:02");  // +1:33
        sunSet.put("04/28","20:36:33");  // +1:31
        sunSet.put("04/29","20:38:05");  // +1:32
        sunSet.put("04/30","20:39:36");  // +1:31
        sunSet.put("05/01","20:41:09");  // +1:33
        sunSet.put("05/02","20:42:40");  // +1:31
        sunSet.put("05/03","20:44:11");  // +1:31
        sunSet.put("05/04","20:45:42");  // +1:31
        sunSet.put("05/05","20:47:12");  // +1:30
        sunSet.put("05/06","20:48:41");  // +1:29
        sunSet.put("05/07","20:50:11");  // +1:30
        sunSet.put("05/08","20:51:39");  // +1:28
        sunSet.put("05/09","20:53:07");  // +1:28
        sunSet.put("05/10","20:54:34");  // +1:27
        sunSet.put("05/11","20:56:02");  // +1:28
        sunSet.put("05/12","20:57:28");  // +1:26
        sunSet.put("05/13","20:58:54");  // +1:26
        sunSet.put("05/14","21:00:18");  // +1:24
        sunSet.put("05/15","21:01:43");  // +1:25
        sunSet.put("05/16","21:03:05");  // +1:22
        sunSet.put("05/17","21:04:26");  // +1:21
        sunSet.put("05/18","21:05:49");  // +1:23
        sunSet.put("05/19","21:07:08");  // +1:19
        sunSet.put("05/20","21:08:27");  // +1:19
        sunSet.put("05/21","21:09:44");  // +1:17
        sunSet.put("05/22","21:11:02");  // +1:18
        sunSet.put("05/23","21:12:16");  // +1:14
        sunSet.put("05/24","21:13:29");  // +1:13
        sunSet.put("05/25","21:14:42");  // +1:13
        sunSet.put("05/26","21:15:54");  // +1:12
        sunSet.put("05/27","21:17:03");  // +1:09
        sunSet.put("05/28","21:18:10");  // +1:07
        sunSet.put("05/29","21:19:16");  // +1:06
        sunSet.put("05/30","21:20:21");  // +1:05
        sunSet.put("05/31","21:21:23");  // +1:02
        sunSet.put("06/01","21:22:24");  // +1:01
        sunSet.put("06/02","21:23:23");  // +0:59
        sunSet.put("06/03","21:24:19");  // +0:56
        sunSet.put("06/04","21:25:14");  // +0:55
        sunSet.put("06/05","21:26:07");  // +0:53
        sunSet.put("06/06","21:26:58");  // +0:51
        sunSet.put("06/07","21:27:45");  // +0:47
        sunSet.put("06/08","21:28:32");  // +0:47
        sunSet.put("06/09","21:29:14");  // +0:42
        sunSet.put("06/10","21:29:56");  // +0:42
        sunSet.put("06/11","21:30:35");  // +0:39
        sunSet.put("06/12","21:31:12");  // +0:37
        sunSet.put("06/13","21:31:45");  // +0:33
        sunSet.put("06/14","21:32:16");  // +0:31
        sunSet.put("06/15","21:32:45");  // +0:29
        sunSet.put("06/16","21:33:11");  // +0:26
        sunSet.put("06/17","21:33:35");  // +0:24
        sunSet.put("06/18","21:33:55");  // +0:20
        sunSet.put("06/19","21:34:13");  // +0:18
        sunSet.put("06/20","21:34:28");  // +0:15
        sunSet.put("06/21","21:34:39");  // +0:11
        sunSet.put("06/22","21:34:50");  // +0:11
        sunSet.put("06/23","21:34:56");  // +0:06
        sunSet.put("06/24","21:35:00");  // +0:04
        sunSet.put("06/25","21:35:01");  // +0:01
        sunSet.put("06/26","21:34:58");  // -0:03
        sunSet.put("06/27","21:34:53");  // -0:05
        sunSet.put("06/28","21:34:45");  // -0:08
        sunSet.put("06/29","21:34:35");  // -0:10
        sunSet.put("06/30","21:34:21");  // -0:14
        sunSet.put("07/01","21:34:04");  // -0:17
        sunSet.put("07/02","21:33:44");  // -0:20
        sunSet.put("07/03","21:33:22");  // -0:22
        sunSet.put("07/04","21:32:58");  // -0:24
        sunSet.put("07/05","21:32:28");  // -0:30
        sunSet.put("07/06","21:31:58");  // -0:30
        sunSet.put("07/07","21:31:25");  // -0:33
        sunSet.put("07/08","21:30:48");  // -0:37
        sunSet.put("07/09","21:30:08");  // -0:40
        sunSet.put("07/10","21:29:28");  // -0:40
        sunSet.put("07/11","21:28:43");  // -0:45
        sunSet.put("07/12","21:27:55");  // -0:48
        sunSet.put("07/13","21:27:06");  // -0:49
        sunSet.put("07/14","21:26:13");  // -0:53
        sunSet.put("07/15","21:25:19");  // -0:54
        sunSet.put("07/16","21:24:21");  // -0:58
        sunSet.put("07/17","21:23:22");  // -0:59
        sunSet.put("07/18","21:22:19");  // -1:03
        sunSet.put("07/19","21:21:15");  // -1:04
        sunSet.put("07/20","21:20:07");  // -1:08
        sunSet.put("07/21","21:18:58");  // -1:09
        sunSet.put("07/22","21:17:47");  // -1:11
        sunSet.put("07/23","21:16:33");  // -1:14
        sunSet.put("07/24","21:15:17");  // -1:16
        sunSet.put("07/25","21:14:00");  // -1:17
        sunSet.put("07/26","21:12:38");  // -1:22
        sunSet.put("07/27","21:11:16");  // -1:22
        sunSet.put("07/28","21:09:53");  // -1:23
        sunSet.put("07/29","21:08:27");  // -1:26
        sunSet.put("07/30","21:07:00");  // -1:27
        sunSet.put("07/31","21:05:28");  // -1:32
        sunSet.put("08/01","21:03:56");  // -1:32
        sunSet.put("08/02","21:02:23");  // -1:33
        sunSet.put("08/03","21:00:48");  // -1:35
        sunSet.put("08/04","20:59:11");  // -1:37
        sunSet.put("08/05","20:57:34");  // -1:37
        sunSet.put("08/06","20:55:53");  // -1:41
        sunSet.put("08/07","20:54:12");  // -1:41
        sunSet.put("08/08","20:52:29");  // -1:43
        sunSet.put("08/09","20:50:45");  // -1:44
        sunSet.put("08/10","20:48:58");  // -1:47
        sunSet.put("08/11","20:47:12");  // -1:46
        sunSet.put("08/12","20:45:21");  // -1:51
        sunSet.put("08/13","20:43:32");  // -1:49
        sunSet.put("08/14","20:41:41");  // -1:51
        sunSet.put("08/15","20:39:48");  // -1:53
        sunSet.put("08/16","20:37:56");  // -1:52
        sunSet.put("08/17","20:36:01");  // -1:55
        sunSet.put("08/18","20:34:06");  // -1:55
        sunSet.put("08/19","20:32:10");  // -1:56
        sunSet.put("08/20","20:30:11");  // -1:59
        sunSet.put("08/21","20:28:12");  // -1:59
        sunSet.put("08/22","20:26:13");  // -1:59
        sunSet.put("08/23","20:24:12");  // -2:01
        sunSet.put("08/24","20:22:11");  // -2:01
        sunSet.put("08/25","20:20:08");  // -2:03
        sunSet.put("08/26","20:18:05");  // -2:03
        sunSet.put("08/27","20:16:01");  // -2:04
        sunSet.put("08/28","20:13:57");  // -2:04
        sunSet.put("08/29","20:11:51");  // -2:06
        sunSet.put("08/30","20:09:46");  // -2:05
        sunSet.put("08/31","20:07:40");  // -2:06
        sunSet.put("09/01","20:05:32");  // -2:08
        sunSet.put("09/02","20:03:25");  // -2:07
        sunSet.put("09/03","20:01:16");  // -2:09
        sunSet.put("09/04","19:59:08");  // -2:08
        sunSet.put("09/05","19:57:00");  // -2:08
        sunSet.put("09/06","19:54:49");  // -2:11
        sunSet.put("09/07","19:52:40");  // -2:09
        sunSet.put("09/08","19:50:29");  // -2:11
        sunSet.put("09/09","19:48:19");  // -2:10
        sunSet.put("09/10","19:46:07");  // -2:12
        sunSet.put("09/11","19:43:57");  // -2:10
        sunSet.put("09/12","19:41:45");  // -2:12
        sunSet.put("09/13","19:39:33");  // -2:12
        sunSet.put("09/14","19:37:22");  // -2:11
        sunSet.put("09/15","19:35:10");  // -2:12
        sunSet.put("09/16","19:32:57");  // -2:13
        sunSet.put("09/17","19:30:45");  // -2:12
        sunSet.put("09/18","19:28:33");  // -2:12
        sunSet.put("09/19","19:26:20");  // -2:13
        sunSet.put("09/20","19:24:08");  // -2:12
        sunSet.put("09/21","19:21:56");  // -2:12
        sunSet.put("09/22","19:19:44");  // -2:12
        sunSet.put("09/23","19:17:31");  // -2:13
        sunSet.put("09/24","19:15:20");  // -2:11
        sunSet.put("09/25","19:13:08");  // -2:12
        sunSet.put("09/26","19:10:56");  // -2:12
        sunSet.put("09/27","19:08:44");  // -2:12
        sunSet.put("09/28","19:06:32");  // -2:12
        sunSet.put("09/29","19:04:22");  // -2:10
        sunSet.put("09/30","19:02:11");  // -2:11
        sunSet.put("10/01","19:00:01");  // -2:10
        sunSet.put("10/02","18:57:51");  // -2:10
        sunSet.put("10/03","18:55:41");  // -2:10
        sunSet.put("10/04","18:53:32");  // -2:09
        sunSet.put("10/05","18:51:22");  // -2:10
        sunSet.put("10/06","18:49:15");  // -2:07
        sunSet.put("10/07","18:47:07");  // -2:08
        sunSet.put("10/08","18:45:00");  // -2:07
        sunSet.put("10/09","18:42:53");  // -2:07
        sunSet.put("10/10","18:40:46");  // -2:07
        sunSet.put("10/11","18:38:41");  // -2:05
        sunSet.put("10/12","18:36:36");  // -2:05
        sunSet.put("10/13","18:34:32");  // -2:04
        sunSet.put("10/14","18:32:29");  // -2:03
        sunSet.put("10/15","18:30:26");  // -2:03
        sunSet.put("10/16","18:28:25");  // -2:01
        sunSet.put("10/17","18:26:24");  // -2:01
        sunSet.put("10/18","18:24:24");  // -2:00
        sunSet.put("10/19","18:22:26");  // -1:58
        sunSet.put("10/20","18:20:27");  // -1:59
        sunSet.put("10/21","18:18:31");  // -1:56
        sunSet.put("10/22","18:16:36");  // -1:55
        sunSet.put("10/23","18:14:41");  // -1:55
        sunSet.put("10/24","18:12:47");  // -1:54
        sunSet.put("10/25","18:10:54");  // -1:53
        sunSet.put("10/26","18:09:04");  // -1:50
        sunSet.put("10/27","17:07:13");  // -1:51
        sunSet.put("10/28","17:05:25");  // -1:48
        sunSet.put("10/29","17:03:38");  // -1:47
        sunSet.put("10/30","17:01:52");  // -1:46
        sunSet.put("10/31","17:00:07");  // -1:45
        sunSet.put("11/01","16:58:24");  // -1:43
        sunSet.put("11/02","16:56:43");  // -1:41
        sunSet.put("11/03","16:55:03");  // -1:40
        sunSet.put("11/04","16:53:26");  // -1:37
        sunSet.put("11/05","16:51:49");  // -1:37
        sunSet.put("11/06","16:50:15");  // -1:34
        sunSet.put("11/07","16:48:41");  // -1:34
        sunSet.put("11/08","16:47:10");  // -1:31
        sunSet.put("11/09","16:45:41");  // -1:29
        sunSet.put("11/10","16:44:13");  // -1:28
        sunSet.put("11/11","16:42:49");  // -1:24
        sunSet.put("11/12","16:41:25");  // -1:24
        sunSet.put("11/13","16:40:04");  // -1:21
        sunSet.put("11/14","16:38:45");  // -1:19
        sunSet.put("11/15","16:37:29");  // -1:16
        sunSet.put("11/16","16:36:13");  // -1:16
        sunSet.put("11/17","16:35:02");  // -1:11
        sunSet.put("11/18","16:33:52");  // -1:10
        sunSet.put("11/19","16:32:44");  // -1:08
        sunSet.put("11/20","16:31:40");  // -1:04
        sunSet.put("11/21","16:30:37");  // -1:03
        sunSet.put("11/22","16:29:38");  // -0:59
        sunSet.put("11/23","16:28:40");  // -0:58
        sunSet.put("11/24","16:27:45");  // -0:55
        sunSet.put("11/25","16:26:54");  // -0:51
        sunSet.put("11/26","16:26:05");  // -0:49
        sunSet.put("11/27","16:25:19");  // -0:46
        sunSet.put("11/28","16:24:35");  // -0:44
        sunSet.put("11/29","16:23:54");  // -0:41
        sunSet.put("11/30","16:23:16");  // -0:38
        sunSet.put("12/01","16:22:42");  // -0:34
        sunSet.put("12/02","16:22:10");  // -0:32
        sunSet.put("12/03","16:21:41");  // -0:29
        sunSet.put("12/04","16:21:15");  // -0:26
        sunSet.put("12/05","16:20:53");  // -0:22
        sunSet.put("12/06","16:20:35");  // -0:18
        sunSet.put("12/07","16:20:18");  // -0:17
        sunSet.put("12/08","16:20:06");  // -0:12
        sunSet.put("12/09","16:19:55");  // -0:11
        sunSet.put("12/10","16:19:49");  // -0:06
        sunSet.put("12/11","16:19:45");  // -0:04
        sunSet.put("12/12","16:19:45");  // -0:00
        sunSet.put("12/13","16:19:49");  // +0:04
        sunSet.put("12/14","16:19:55");  // +0:06
        sunSet.put("12/15","16:20:04");  // +0:09
        sunSet.put("12/16","16:20:18");  // +0:14
        sunSet.put("12/17","16:20:33");  // +0:15
        sunSet.put("12/18","16:20:53");  // +0:20
        sunSet.put("12/19","16:21:15");  // +0:22
        sunSet.put("12/20","16:21:41");  // +0:26
        sunSet.put("12/21","16:22:10");  // +0:29
        sunSet.put("12/22","16:22:42");  // +0:32
        sunSet.put("12/23","16:23:16");  // +0:34
        sunSet.put("12/24","16:23:54");  // +0:38
        sunSet.put("12/25","16:24:35");  // +0:41
        sunSet.put("12/26","16:25:19");  // +0:44
        sunSet.put("12/27","16:26:05");  // +0:46
        sunSet.put("12/28","16:26:56");  // +0:51
        sunSet.put("12/29","16:27:47");  // +0:51
        sunSet.put("12/30","16:28:43");  // +0:56
        sunSet.put("12/31","16:28:38");  // +0:56

    } // end constructor Astro

} // end class Astro
