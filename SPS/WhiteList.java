/*
 * WhiteList.java
 *
 * Created on 11. August 2008, 13:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package name.gries.common;
import  java.util.Hashtable;
import  java.util.Enumeration;

/**
 * Whitelist contains supported Called Party Numbers (CdPA) for remoteAccess.
 *
 * @author  Michael Gries
 * @version 8.8.11
 *
 */
public class WhiteList {
    
    private Hashtable user = new Hashtable();
    
    /** 
     * Creates a new instance of WhiteList.
     * <p>
     * "+4916099408080","TESTUSER" <br>
     * "+4917696731423","TC65USER" <br>
     * "+4917696735022","AF104SPS" <br>
     */
    public WhiteList() {
        user.put("+491702237454",   "ADMIN"     );
        user.put("+4917696731423",  "TC65USER"  );
        user.put("+4917696735022",  "AF104SPS"  );
        user.put("+4916099408080",  "TESTUSER"  );
        user.put("+49662165797",    "Home"      );
        user.put("+49662184691",    "Buero"     );
        user.put("+4915150603618",  "Britta"    );
        user.put("+4916094434446",  "Stefanie"  );
        user.put("+491704625332",   "Sebastian" );
        user.put("+491759026282",   "Sarah"     );
        user.put("+491702237444",   "Elfriede"  );
      //user.put("+49",             "SUPPRESSED");
    }

    public String getCallerStatus(String CalledPartyId) {
        String status = "UNKOWN";
        if (user.containsKey(CalledPartyId)) {
            Object o = user.get(CalledPartyId);
            status = (String)o;
        }
        return status;
    }
    
    /**
     * 
     */
    public boolean checkCallerAllowed(String CalledPartyId) {
        boolean allowed = false;
        String status = "UNKOWN";
        status = this.getCallerStatus(CalledPartyId);
        if (status != "UNKOWN"){
            allowed = true;
        }
        return allowed;
    }
    
    private String convertCallerID(String CalledPartyId) {
        if (CalledPartyId.indexOf("+") >= 0) {
           //TODO 
        }
        return CalledPartyId;
    }
    
    public String printWhitelist() {
        return user.toString(); // rather long string
    }
    
    public String printWhitelistKeys() {
        String keys = "Whitelist Keys: " + "\r\n";
        try {
            Enumeration e = user.keys();
            while (e.hasMoreElements()) {
                String el = (String) e.nextElement();
                if (el.length() > 3) el = el.substring(3,el.length());
                keys = keys + el + "\r\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }
    
    public String printWhitelistUser() {
        String caller = "Whitelist User: " + "\r\n";
        try {
            Enumeration e = user.elements();
            while (e.hasMoreElements()) {
                caller = caller + (String) e.nextElement() + "\r\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return caller;
    }
    
} // end class Whitelist
