
import java.net.*;
import javax.net.ssl.*;

public class SimpleClient {

  private static SimpleConsts cons = new SimpleConsts();

  public static void main(String[] args) throws Exception {
    try {
      SSLSocket socket = SimpleSocket.getSocket(cons.host, cons.port, "Uses Mac Keychain", "No Password");
      SSLSession session = socket.getSession();
      blk();
      ln("CLIENT got a socket connection");
      ln("           Protocol: " + session.getProtocol());
      ln("       Cipher Suite: " + session.getCipherSuite());
      blk();

      Thread thrdWrite = new Thread( new SimpleRW("CLIENT", false, socket));
      thrdWrite.start();
      Thread thrdRead = new Thread( new SimpleRW("CLIENT", true, socket));
      thrdRead.start();

      while(thrdWrite.isAlive() || thrdWrite.isAlive())
        Thread.sleep(200);
      System.exit(0);
    } catch(Exception e) { ln("CLIENT Exception -- " + e.toString()); }
  }

  public static void ln(String s) { cons.ln(s); }
  public static void blk() { cons.blk(); }
}