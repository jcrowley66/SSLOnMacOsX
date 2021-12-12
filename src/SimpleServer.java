import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;

/** SERVER side of a simple client/server SSL connection.
 *
 * USAGE:  java -cp JavaSSL.jar SimpleServer [host [port]]  (port is an integer)
 *
 * Defaults --  for host: localhost    port: 51000
 *
 * Note: To run the client use 'SimpleClient' above instead of 'SimpleServer'
 *       Start the SimpleServer first (which puts up an accept()), then the SimpleClient.
 *
 * Each side will:
 * -- put up a prompt to the console
 * -- read the line entered
 * -- pass that line to the other end of the connection, which will print it
 *
 * This runs on a Mac OS X (Catalina 10.15.7) or Windows XXX using Java 1.8.0_311
 *
 * The key concept is that the JDK includes Providers which uses the Mac Keychain(s) or Windows root certificates.
 * See SimpleCommon.setupTrustStore.
 *
 * You may be prompted on a Mac to allow access to the Keychains so enter your password and select "Always Allow" so you
 * are not asked every time.
 *
 * Note the flags in SimpleCommon - these can be set to True to display summary and detailed information about the
 * flow of both Server and Client.
 */
public class SimpleServer implements Runnable {

  private static SimpleCommon common = new SimpleCommon();

  private ServerSocket sktSvr;
  public SimpleServer(ServerSocket ss) {
    sktSvr = ss;
  }
  public static void main(String args[]) {
    common.setupTrustStore(true);
    try {
      if(args.length > 0){
        common.host = args[0];
        if(args.length > 1)
          common.port = Integer.parseInt(args[1]);
      }
      common.showStart("SERVER");
      trace("Getting ServerSocketFactory");
      ServerSocketFactory ssf = SimpleServer.getServerSocketFactory();
      trace("Creating ServerSocket");
      ServerSocket ss = ssf.createServerSocket();
      ((SSLServerSocket) ss).setNeedClientAuth(true);

      ss.bind(new InetSocketAddress(common.host, common.port));
      trace("Created SSLServerSocket -- Local Socket Address: " + ss.getLocalSocketAddress() + " InetAddress: " + ss.getInetAddress());
      new Thread( new SimpleServer(ss)).start();
    } catch (IOException e) {
      common.err("Unable to start ClassServer: " + e.getMessage());
      e.printStackTrace();
    }
  }
  // just loop accepting connections. For each, start a read & write thread.
  public void run() {
    try {
      Socket socket = sktSvr.accept();
      trace("SERVER -- socket was accepted");

      Thread thrdWrite = new Thread( new SimpleRW("SERVER", false, socket));
      thrdWrite.start();
      Thread thrdRead = new Thread( new SimpleRW("SERVER", true, socket));
      thrdRead.start();
    } catch(Exception e) { common.err("SERVER Exception on Accept -- " + e.toString());}
  }

  private static ServerSocketFactory getServerSocketFactory() {
    SSLServerSocketFactory ssf = null;
    try {
      // set up key manager to do server authentication
      SSLContext ctx = common.getSSLContext(true);
      ssf = ctx.getServerSocketFactory();
      return ssf;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public static void trace(String s) { common.trace(s); }
  public static void ln(String s)    { common.ln(s); }
  public static void blk()           { common.blk(); }
}