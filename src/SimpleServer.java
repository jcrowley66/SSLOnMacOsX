import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;

/** SERVER side of a simple client/server SSL connection.
 *
 * Each side just reads lines from the console and passes them to the other side of the connection (and then prints).
 *
 * This runs on a Mac OS X (Catalina 10.15.7) using Java 1.8.0_311, and the key concept is that the JDK includes a Provider
 * which uses the Mac Keychain(s) to provide the necessary certificates, as follows:
 *
 *   -- MUST have JVM option:  -Djavax.net.ssl.trustStoreType=KeychainStore
 *   -- Then in the code:      KeyStore.getInstance("KeychainStore");
 *
 * Start the SimpleServer first (which puts up an accept()), then the SimpleClient.
 *
 * Each process will put up a prompt, just enter any desired message and hit Enter. Enter QUIT to terminate both Client
 * and Server.
 *
 * You may be prompted to allow access to the Keychains so enter your password and select "Always Allow" button so you
 * are not asked every time.
 *
 * Note the flags in SimpleConsts - these can be set to True to display summary and detailed information about the
 * flow of both Server and Client.
 *
 * The JavaSSL JAR may be used to run each side:
 *   java -cp JavaSSL.jar -Djavax.net.ssl.trustStoreType=KeychainStore SimpleServer
 *   java -cp JavaSSL.jar -Djavax.net.ssl.trustStoreType=KeychainStore SimpleClient
 */
public class SimpleServer implements Runnable {

  private static SimpleConsts cons = new SimpleConsts();

  private ServerSocket sktSvr;
  public SimpleServer(ServerSocket ss) {
    sktSvr = ss;
  }
  public static void main(String args[]) {
    try {
      cons.showStart("SERVER");

      trace("Getting ServerSocketFactory");
      ServerSocketFactory ssf = SimpleServer.getServerSocketFactory();
      trace("Creating ServerSocket");
      ServerSocket ss = ssf.createServerSocket();
      ((SSLServerSocket) ss).setNeedClientAuth(true);

      ss.bind(new InetSocketAddress(cons.host, cons.port));
      trace("Created SSLServerSocket -- Local Socket Address: " + ss.getLocalSocketAddress() + " InetAddress: " + ss.getInetAddress());
      new Thread( new SimpleServer(ss)).start();
    } catch (IOException e) {
      cons.ln("Unable to start ClassServer: " + e.getMessage());
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
    } catch(Exception e) { cons.ln("SERVER Exception on Accept -- " + e.toString());}
  }

  private static ServerSocketFactory getServerSocketFactory() {
    SSLServerSocketFactory ssf = null;
    try {
      // set up key manager to do server authentication
      SSLContext ctx;
      KeyManagerFactory kmf;
      KeyStore ks;

      ctx = SSLContext.getInstance("TLS");
      kmf = KeyManagerFactory.getInstance("SunX509");
      ks  = KeyStore.getInstance("KeychainStore");
      ks.load(null, null);
      kmf.init(ks, null);
      ctx.init(kmf.getKeyManagers(), null, null);
      if(cons.bServer) cons.show("SERVER KeyStore", "Using Mac Keychain", "No password", ks, kmf, ctx);

      ssf = ctx.getServerSocketFactory();
      return ssf;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  public static void trace(String s) { cons.trace(s); }
  public static void ln(String s)    { cons.ln(s); }
  public static void blk()           { cons.blk(); }
}