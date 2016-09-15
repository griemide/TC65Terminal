/*
 * EventList.java
 *
 * Created on 22. September 2008, 10:25
 *
 */

package name.gries.common;
import  java.util.Hashtable;

/**
 * EventList contains Notifications based on a given day (format MM/DD).
 *
 * @author  Michael Gries
 * @version 8.12.14
 *
 */
public class EventList {
    
    private Hashtable event = new Hashtable();
    
    /** 
     * Creates a new instance of EventList.
     * <p>
     * "09/24", "FONIC Konto 017696731423 prüfen http://www.fonic.de" <br>
     * "09/26", "RTC supply Battery check" <br>
     * "10/25", "Umstellung MESZ auf MEZ 03:00h" <br>
     * "01/27", "27.01.1962 Michael*" <br>
     */
    public EventList() {
        event.put("99/99", "27.01.1962 ADMIN +491702237454 http://www.gries.name" );
        event.put("01/27", "27.01.1962 Michael*" );
        event.put("01/08", "08.01.1963 Britta*" );
        event.put("12/15", "15.12.1989 Stefanie*" );
        event.put("12/10", "10.12.1991 Sebastian*" );
        event.put("06/14", "14.06.1998 Sarah*" );
        event.put("05/21", "21.05.1936 Elfriede*" );
        event.put("11/01", "01.11.1937 Gregor*" );
        event.put("12/20", "20.12.1974 Luzia+" );
        event.put("--/--", " " );
        event.put("03/21", "Fruehlingsanfang" );
        event.put("06/21", "Sommeranfang" );
        event.put("09/23", "Herbstanfang" );
        event.put("12/21", "Winteranfang" );
        event.put("aa/--", " " );
        event.put("09/24", "FONIC Konto 017696731423 prüfen http://www.fonic.de" );
        event.put("09/25", "FONIC Konto 017696735022 prüfen http://www.fonic.de" );
        event.put("09/26", "RTC supply Battery check" );
        event.put("03/29", "Umstellung MEZ auf MESZ 02:00h" );
        event.put("10/25", "Umstellung MESZ auf MEZ 03:00h" );
        event.put("10/03", "Tag der Deutschen Einheit" );
        event.put("12/13", "Eventlist Test" );
    }
    
    /**
     * @param   day (format MM/DD)
     * @return  boolean (true if notification found)
     *
     */
    public boolean testEvent(String day) {
        boolean bEvent = false;
        if (event.containsKey(day)) { bEvent = true; }
        return bEvent;
    }

    /**
     * @param   day (format MM/DD)
     * @return  String Notification text
     *
     */
    public String getEvent(String day) {
        String sEvent = "UNKOWN";
        if (event.containsKey(day)) {
            Object o = event.get(day);
            sEvent = (String)o;
        }
        return sEvent;
    }
    
} // end class
