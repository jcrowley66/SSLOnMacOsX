import java.net.Socket;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/** For all messages, 1st 4 bytes are 32-bit integer length of message (w/o the length), rest is characters (ASCII).
 *
 * If READ, read 1st 4 bytes, get the length, read and print the rest of the message.
 * If ! READ, get a line from the console, send length of message, then send the message.
 *
 * If the message entered from console or read from stream is QUIT, then terminate
 */
public class ChannelRW implements Runnable {

  private AllCommon common = new AllCommon();

  private String  label;
  private boolean read;
  private Socket  skt;

  private ByteBuffer bb = ByteBuffer.allocate(1024);

  private BufferedReader rdConsole = new BufferedReader( new InputStreamReader( System.in )); // If we're the Write side

  private SocketChannel channel = null;

  public ChannelRW(String label, boolean read, SocketChannel channel){
    this.label   = label;
    this.read    = read;
    this.channel = channel;
  }
  public void run() {
    try {
      if (read) {
        debug("Starting 'doRead'");
        doRead();
      } else {
        debug("Starting: 'doWrite'");
        doWrite();
      }
    } catch(Exception e){
      common.err(label, (read ? " READ -- " : " WRITE -- ") + "Exception processing In/Out Stream -- " + e.toString());
      e.printStackTrace();
    }
    trace(label + " R/W 'run' terminating");
    System.exit(0);
  }
  // READs a message from the channel & prints to the console
  public void doRead() throws Exception {
    while(true){
      bb.position(0);
      bb.limit(4);
      debug("Reading the length");
      while(bb.position() < 4) {
        int n = channel.read(bb);
//        if(n < 4) debug("Read " + n + " bytes");
      }
      int lnth = bb.getInt(0);
      debug("Reading the length -- Done: " + lnth);
      Thread.sleep(20);
      bb.limit(lnth);
      while(bb.position() < lnth){
        int n = channel.read(bb);
        if(bb.position() < bb.limit()) debug("Read " + n + " bytes - length = " + lnth);
      }
      String str = new String(bb.array(), 4, lnth - 4);
      ln(" READ: " + str);
      if(str.equals("QUIT")) {
        trace(label + " doRead terminating");
        System.exit(0);
      }
    }
  }
  // Reads a message from the console, writes length then message to the socket
  public void doWrite() throws Exception {
    while(true){
      System.out.print(label + " ---> ");
      String str = rdConsole.readLine();
      int lnth   = 4 + str.length();
      bb.position(0);
      bb.limit(bb.capacity());
      bb.putInt(lnth);
      bb.put(str.getBytes());
      bb.position(0);
      bb.limit(lnth);
      debug("Writing length: " + lnth + " String: " + str);
      while(bb.position() < lnth){
        channel.write(bb);
        if(bb.position() < lnth) debug("Buffer Position: " + bb.position() + " Length: " + lnth);
      }
      trace(label + " WROTE: " + str);
      if(str.equals("QUIT")) {
        trace(label + " doWrite terminating");
        Thread.sleep(100);        // To let the channel.write complete
        System.exit(0);
      }
    }
  }

  private void trace(String s) { common.trace(s); }
  private void ln(String s)    { common.ln(label + " -- " + s); }
  private void debug(String s) { common.debug(label, s); }
}
