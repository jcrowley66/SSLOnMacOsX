import java.net.Socket;
import java.io.*;
import java.nio.*;

/** For all messages, 1st 4 bytes are 32-bit integer length of message (w/o the length), rest is characters (ASCII).
 *
 * If READ, read 1st 4 bytes, get the length, read and print the rest of the message.
 * If ! READ, get a line from the console, send length of message, then send the message.
 *
 * If the message entered from console or read from stream is QUIT, then terminate
 */
public class SimpleRW implements Runnable {

  private static boolean bDebug = true;

  private SimpleConsts cons = new SimpleConsts();

  private String label;
  private boolean read;
  private Socket  skt;

  private ByteBuffer  bbBfr   = ByteBuffer.allocate(1024);
  private byte[]      bfr     = bbBfr.array();
  private ByteBuffer  bbInt   = ByteBuffer.allocate(4); // Read/write the length here
  private byte[]      bfrInt  = bbInt.array();

  private BufferedReader rdConsole = new BufferedReader( new InputStreamReader( System.in )); // If we're the Write side

  private InputStream   inStrm = null;      // One or the other will be initialized
  private OutputStream  outStrm= null;

  public SimpleRW(String label, boolean read, Socket skt){
    this.label = label;
    this.read  = read;
    this.skt   = skt;
  }
  public void run() {
    try {
      if (read) {
        inStrm = skt.getInputStream();
        debug("Starting 'doRead'");
        doRead();
      } else {
        outStrm = skt.getOutputStream();
        debug("Starting: 'doWrite'");
        doWrite();
      }
    } catch(Exception e){
      cons.ln(label + (read ? " READ -- " : " WRITE -- ") + "Exception processing In/Out Stream -- " + e.toString());
    }
    ln(label + " R/W 'run' terminating");
  }
  // READs a message from the socket & prints to the console
  public void doRead() throws Exception {
    boolean keepGoing = true;
    while(keepGoing){
      int amt = 0;
      debug("Reading the length");
      while(amt < 4) {
        amt += inStrm.read(bfrInt, amt, 4 - amt);
      }
      int lnth = bbInt.getInt(0);
      debug("Reading the length -- Done: " + lnth);
      amt = 0;
      while(amt < lnth){
        amt += inStrm.read(bfr, amt, lnth - amt);
      }
      String str = new String(bfr, 0, lnth);
      debug(" READ: " + str);
      if(str.equals("QUIT")) {
        ln(label + " doRead terminating");
        keepGoing = false;
      }
    }
  }
  // Reads a message from the console, writes length then message to the socket
  public void doWrite() throws Exception {
    boolean keepGoing = true;
    while(keepGoing){
      System.out.println(label + " ---> ");
      String str = rdConsole.readLine();
      bbInt.putInt(0, str.length());
      debug("Writing length: " + bbInt.getInt(0) + " String: " + str);
      outStrm.write(bfrInt);
      byte[] strBytes = str.getBytes();
      bbBfr.position(0);
      bbBfr.put(strBytes);
      debug("Writing string: " + str);
      outStrm.write(bfr, 0, strBytes.length);
      cons.ln(label + " WRITE: " + str);
      if(str.equals("QUIT")) {
        ln(label + " doWrite terminating");
        keepGoing = false;
      }
    }
  }

  private void ln(String s)    {cons.ln(label + " -- " + s); }
  private void debug(String s) { if(bDebug) cons.ln("DEBUG: " + label + " -- " + s); }
}
