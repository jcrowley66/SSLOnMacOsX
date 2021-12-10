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
 * Note the flags in SimpleCommon - these can be set to True to display summary and detailed information about the
 * flow of both Server and Client.
 *
 * The JavaSSL JAR may be used to run each side:
 *   java -cp JavaSSL.jar SimpleServer
 *   java -cp JavaSSL.jar SimpleClient
 */
public class SimpleServer implements Runnable {

  private static SimpleCommon common = new SimpleCommon();

  private ServerSocket sktSvr;
  public SimpleServer(ServerSocket ss) {
    sktSvr = ss;
  }
  public static void main(String args[]) {
    common.setupTrustStore(true);
    common.showStart("SERVER");
    try {

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
      SSLContext ctx;
      KeyManagerFactory kmf;
      KeyStore ks;

      ctx = SSLContext.getInstance("TLS");
      kmf = KeyManagerFactory.getInstance("SunX509");
      ks  = KeyStore.getInstance(common.keystoreName);
      ks.load(common.keystoreInStrm, common.keystorePwd);
      kmf.init(ks, null);
      ctx.init(kmf.getKeyManagers(), null, null);
      if(common.bServer) common.show("SERVER KeyStore", ks, kmf, ctx);

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