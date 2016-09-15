package name.gries;

 import java.io.*;
 import java.util.*;
 import java.lang.*;
 import javax.microedition.io.*;
 import com.siemens.icm.io.*;

public class Ftp {

    private static final int CNTRL_PORT = 21;
    private StreamConnection csock = null;
    private StreamConnection dsock = null;
    private InputStreamReader dcis;
    private PrintStream pos;

    
    // FOR DEBUGGING: set the variable to "true"
    private boolean DEBUG = false;

    /**
    * ftp - default constructor
    * 
    * @param server servername
    * @param user username
    * @param pass password
    */
    public Ftp(String server, String user, String pass) {
        try { 
            ftpConnect(server);
            ftpLogin(user, pass);
        } catch (InterruptedException e) {
            System.out.println("Ftp: catch InterruptedException: " + e);
        } catch (IOException ioe) {
            System.out.println("Ftp: catch IOException: " + ioe);
            ioe.printStackTrace();
        }
    }

   /**
   * download()
   *
   * Method send a command to the ftp server
   * @param dir directory on the ftp server
   * @param file filename of the destination file
   * @param asc transfer type true is Asccii and false is Binary
   */
    public void download(String dir, String file, boolean asc) throws IOException {
        ftpSetDir(dir);
        ftpSetTransferType(asc);
        dsock = ftpGetDataSock();
        InputStream is = dsock.openInputStream();
	ftpSendCmd("RETR "+file);
	getAsByte(is, file);
        ftpLogout();	
    }
   
    /**
   * upload()
   *
   * Method send a command to the ftp server
   * @param dir directory on the ftp server
   * @param file filename of the destination file
   * @param asc transfer type true is Asccii and false is Binary
   */
    public void upload(String dir, String file, boolean asc) throws IOException {
        if (DEBUG) System.out.println("FtpDemo: Ftp.upload() before ftpSetDir");
	ftpSetDir(dir);
	ftpSetTransferType(asc);
	dsock = ftpGetDataSock();
	OutputStream os = dsock.openOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("STOR " + file);
	readFileAndUpload(dos, file);
        ftpLogout();
    }
   
    /**
   * upload()
   *
   * Method send a command to the ftp server
   * @param dir directory on the ftp server
   * @param file filename of the destination file
   * @param asc transfer type true is Ascci and false is Binary
   */
    public void uploadAppend(String dir, String file, boolean asc) throws IOException {
        if (DEBUG) System.out.println("FtpDemo: Ftp.uploadAppend() before ftpSetDir");
	ftpSetDir(dir);
	ftpSetTransferType(asc);
	dsock = ftpGetDataSock();
	OutputStream os = dsock.openOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        ftpSendCmd("APPE " + file);
	readFileAndUpload(dos, file);
        ftpLogout();
    }
    
   /**
   * readFileAndUpload()
   *
   * Methode read a file and write it to the DataOutputStream.
   * @param dos DataOutputStream for sending the file to the server
   * @param file file name for upload 
   */
    private void readFileAndUpload(DataOutputStream dos, String file) throws IOException  {	
	File f = new File();
	f.open(file);
	int fileLen = f.length();
	byte[] buf = new byte[ fileLen ];
	f.read( buf, 0, fileLen );
	dos.write(buf, 0, fileLen);	
	f.close( );
	f = null;
	buf = null;
	dos.flush();
	dos.close();
    }

    private boolean pauser = false;
    
   /**
   * getAsByte()
   *
   * Methode receive a file and stores the file in the flash file system
   * @param is InputStream to get the file from the server
   * @param file stores the data of the InputStream in the Flash under this file name
   */
    private void getAsByte(InputStream is, String file) throws IOException  {
        int c=0;
        byte lineBuffer[]=new byte[128], buf[] = lineBuffer;
	int room= buf.length, offset = 0;
	if (DEBUG) System.out.println(" befor try room" + room);
        try {
          loop: while (true) {
              // read chars into a buffer which grows as needed
                  switch (c = is.read() ) {
                      case -1: break loop;

                      default: if (--room < 0) {
                                   buf = new byte[offset + 128];
                                   room = buf.length - offset - 1;
                                   // clear buffer
                                   System.arraycopy(lineBuffer, 0,  
                                            buf, 0, offset);
                                   lineBuffer = buf;
                               }
                               buf[offset++] = (byte) c;
                               break;
                  }
          }
        } catch(IOException ioe) {ioe.printStackTrace();}
        if ((c == -1) && (offset == 0)) {
		System.out.println("Error InputStream");
        }
	File f = new File();
	f.open("file");
	f.write(buf, 0, offset);
	f.close( );
	f = null;
	buf = null;
    }
   /**
   * ftpConnect()
   *
   * Methode open a Connection to the specified server on standard ftp port 21
   * @param server server address
   */
    private void ftpConnect(String server)
        throws InterruptedException, IOException {
            
        // Set up socket, control streams, connect to ftp server
        // Open socket to server control port 21
	//StreamConnection csock = (StreamConnection) Connector.open("socket://" + server + ":" + CNTRL_PORT, Connector.READ_WRITE);
        String connection = "socket://" + server + ":" + CNTRL_PORT;
        if (DEBUG) System.out.println("Setup: "+connection);
	StreamConnection csock = (StreamConnection) Connector.open(connection, Connector.READ_WRITE);
        
        // Open control streams
        InputStream cis = csock.openInputStream();
        dcis = new InputStreamReader(cis);        
        
        OutputStream cos = csock.openOutputStream();        
        pos = new PrintStream(cos); // set auto flush true.
        
        // See if server is alive or dead...
        String numerals = responseHandler(null);
              
        if(numerals.substring(0,3).equals("220")) // ftp server alive
            ; //System.out.println("Connected to ftp server");
        else System.err.println("Error connecting to ftp server.");       
    }

   /**
   * ftpLogin()
   *
   * Methode for login
   * @param user user name of the ftp server
   * @param pass password of the ftp server
   */
    private void ftpLogin(String user, String pass)
        throws InterruptedException, IOException {
        ftpSendCmd("USER "+user);
        ftpSendCmd("PASS "+pass);
    }
    
   /**
   * ftpSetDir()
   *
   * Methode for changing directories
   * @param dir destination path
   */
    private void ftpSetDir(String dir)
        throws IOException { 
        // cwd to dir
        ftpSendCmd("CWD "+dir);
    }
   
    /**
   * ftpSetTransferType()
   *
   * Methode to set the transfer type
   * @param asc Asccii (true) or Binarie Mode (false)
   */
    private void ftpSetTransferType(boolean asc)
    throws IOException {
    // set file transfer type
        String ftype = (asc? "A" : "I");
        ftpSendCmd("TYPE "+ftype);
    }    
   
    /**
   * ftpGetDataSock()
   *
   * Methode to create a data connection to the ftp server
   * @return  The data connection to the ftp server
   */
    private StreamConnection ftpGetDataSock()
        throws IOException {
        // Go to PASV mode, capture server reply, parse for socket setup
        String reply = ftpSendCmd("PASV");
	
	String[] parts = new String[6];
	int endIndex = 0;
	int j = 0;
	int beginIndex = (reply.indexOf("(") + 1);
	
	if (DEBUG) System.out.println("reply for DataSock:reply: " + reply );
			
	while (j != 6)	{
		try {
		
		if (j == 5) 
			endIndex = reply.indexOf(")");
		else
			endIndex = reply.indexOf(",");
		
                    parts[j] = reply.substring(beginIndex,endIndex);
                    reply = reply.substring(endIndex + 1);
                } catch (IndexOutOfBoundsException s) {
                    System.out.println("Substring ERROR: " + beginIndex + " - " + endIndex);
                }
		beginIndex = 0;
		j++;
	}
	if (DEBUG) System.out.println("IP address: " + parts[0]+"."+parts[1]+"."+parts[2]+"."+parts[3]+" ; "+parts[4]+","+parts[5]);
	
        // Get rid of everything before first "," except digits
        /*
        String[] possNum = new String[3];
        for( j = 0; j < 3; j++) {
            try {

            // Get 3 characters, inverse order, check if digit/character
            possNum[j] = parts[0].substring(parts[0].length() - (j + 1),
                parts[0].length() - j); // next: digit or character?
            if(!Character.isDigit(possNum[j].charAt(0)))
                possNum[j] = "";
            } catch (IndexOutOfBoundsException s) {
                System.out.println("possNum Substring ERROR: ");
            }
        }
        parts[0] = possNum[2] + possNum[1] + possNum[0];
         */

        // Get only the digits after the last ","
        String[] porties = new String[3];
        for(int k = 0; k < 3; k++) {
            // Get 3 characters, in order, check if digit/character
            // May be less than 3 characters
            if((k + 1) <= parts[5].length())
                porties[k] = parts[5].substring(k, k + 1);
            else porties[k] = "FOOBAR"; // definitely not a digit!
            // next: digit or character?
            if(!Character.isDigit(porties[k].charAt(0)))
                    porties[k] = "";
        } // Have to do this one in order, not inverse order
        parts[5] = porties[0] + porties[1] + porties[2];
        // Get dotted quad IP number first
        String ip = parts[0]+"."+parts[1]+"."+parts[2]+"."+parts[3];

        // Determine port
        int port = -1;
        try { // Get first part of port, shift by 8 bits.
            int big = Integer.parseInt(parts[4]) << 8;
            int small = Integer.parseInt(parts[5]);
            port = big + small; // port number
        } catch(NumberFormatException nfe) {nfe.printStackTrace();}
        if((ip != null) && (port != -1)) {
		System.out.println("    FTP: modified IP:PORT = " + ip + ":" + port);
		dsock = (StreamConnection) Connector.open("socket://" + ip + ":" + port, Connector.READ_WRITE);
		if (DEBUG) System.out.println("dsock: " + dsock);
	}
        else throw new IOException();
        return dsock;
    }
    
   /**
   * ftpSendCmd()
   *
   * Method send a command to the ftp server
   * @param cmd Ftp command
   * @return  The response of the ftp command
   */
    private String ftpSendCmd(String cmd)
        throws IOException
    { // This sends a dialog string to the server, returns reply
      // Prints out only last response string of the lot.
      System.out.println(" -> FTP: " + cmd);
        if (pauser)
            {
		System.out.println(" -> FTP: (pauser) " + pauser);
                if (dcis != null)
                {
		    String discard = bufferReader();
		    
                    // preventing this further client request until server
                    // responds to the already outstanding one.
                    if (DEBUG) {
                        System.out.println("keeping handler in sync"+
                            " by discarding next response: ");
                        System.out.println(discard);
                    }
                    pauser = false;
                }
            }
        pos.print(cmd + "\r\n" );
        pos.flush(); 
        String response = responseHandler(cmd);
        return response;
    }

     /**
     * responseHandler()
     *
     *  Method handle the multi-line replies, if  more than one line returned
     * @param cmd is a String command or null
     * @return  returns just the last line of a possibly multi-line response
     */
     private String responseHandler(String cmd) 
         throws IOException
     { // handle more than one line returned
        
	String discard = bufferReader();
	String reply = this.responseParser(discard);
        String numerals = reply.substring(0, 3);
        String hyph_test = reply.substring(3, 4);
        String next = null;
        if (DEBUG) System.out.println("before declaration! " + discard + "\n" +  reply + "\n" + numerals + "\n" + hyph_test + "\n endD");
        if(hyph_test.equals("-")) {
            // Create "tester", marks end of multi-line output
            String tester = numerals + " ";
            boolean done = false;
            while(!done) { // read lines til finds last line
		next = bufferReader();
		
                // Read "over" blank line responses
                while (next.equals("") || next.equals("  ")) {
                    next = bufferReader();
                }

                // If next starts with "tester", we're done
               if(next.substring(0,4).equals(tester))
                   done = true;
            }

            if(DEBUG)
                if(cmd != null)
                    System.out.println("Response to:if "+cmd+" was: "+next);
                else
                    System.out.println("Response was:if "+next);
            return next;

        } else // "if (hyph_test.equals("-")) not true"
            if(DEBUG)
                if(cmd != null)
                    System.out.println("Response to:else "+cmd+" was: "+reply);
                else
                    System.out.println("Response was:else "+reply);
            return reply;
    }
    /**
     * responseParser()
     *
     *  Method check first digit of first line of response and take action based on it
     *  set up to read an extra line if the response starts with "1"
     * @param resp is the respose of the ftp command
     * @return  the command if it ok
     */
    private String responseParser(String resp)
        throws IOException
    { // Check first digit of resp, take appropriate action.
        String digit1 = resp.substring(0, 1);
        System.out.println("\r\n");
        if (DEBUG) System.out.println("before digit1! " + digit1);
        if(digit1.equals("1")) {
            // server to act, then give response
            // set pauser
            pauser = true;
            return resp;
        }
        else if(digit1.equals("2")) { // do usual handling
            // reset pauser
            pauser = false;
            return resp;
        }
        else if(digit1.equals("3") || digit1.equals("4")
            || digit1.equals("5")) { // do usual handling
            return resp;
        }
        else { // not covered, so return null
            return null;
        }
    }

     /**
     * ftpLogout()
     *
     *  Method lgout from the ftp server and close all streams and sockets
     */
    private void ftpLogout() {// logout, close streams
        try { 
            if(DEBUG) System.out.println("sending BYE");
	    pos.print("BYE" + "\r\n" );
            pos.flush();
            pos.close();
            dcis.close();
	    if (csock != null) {
		csock.close();
            }
            if (dsock != null) {
                dsock.close();
            }
        } catch(IOException ioe) {ioe.printStackTrace();}
    }
    
     /**
     * bufferReader()
     *
     *  Method read the response of ftp commands until a \r \n.
     * @return the response without CF CR.
     */
    private String bufferReader() throws IOException {
	String discard = "";
	int flag = 0;
	int chi = 0;
	while (flag != 1)	{ // Probleme mit '\r \n', wenn nur /r kommt. Bei Windowssystemen kommt beides
		chi = dcis.read();
		if (chi == '\n') {
			System.out.println(" end: \\n ");
			flag = 1;
		}
		if (chi == '\r') {
			//System.out.println(" end: \\r ");
			if ((chi = dcis.read()) == '\n') {
				//System.out.println(" end: \\r \\n ");
				flag = 1;
			}
			flag = 1;
		}
		else {
			discard = discard + (char)chi;
			System.out.print( (char)chi );
		}
	}
	return discard;
    }
}
