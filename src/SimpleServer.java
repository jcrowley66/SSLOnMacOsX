import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.Enumeration;
import javax.net.*;
import javax.net.ssl.*;

/** SERVER side of a simple client/server SSL connection.
 *
 * Each side just reads lines from the console and passes them to the other side of the connection (and then prints).
 *
 * This runs on a Mac OS X (Catalina 10.15.7), and the key concept is that the JDK includes a Provider which uses
 * the Mac Keychain(s) to provide the necessary certificates, as follows:
 *
 *          KeyStore.getInstance("KeychainStore");
 *
 * Start the SimpleServer first (which puts up an accept()), then the SimpleClient.
 *
 * Entering QUIT on either side terminates both client and server.
 *
 * Note the flags in SimpleConsts - these can be set to True to display detailed information about the
 * flow of both Server and Client.
 */
public class SimpleServer implements Runnable {

  private static SimpleConsts cons = new SimpleConsts();

  private ServerSocket sktSvr;
  public SimpleServer(ServerSocket ss) {
    sktSvr = ss;
  }
  public static void main(String args[]) {
    try {
      trace("SERVER starting -- Java: " + System.getProperty("java.version"));
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
      if(cons.bServer) show("SERVER KeyStore", "Using Mac Keychain", "No password", ks, kmf, ctx);
      kmf.init(ks, null);
      ctx.init(kmf.getKeyManagers(), null, null);

      ssf = ctx.getServerSocketFactory();
      return ssf;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  // Display all the status for any that are non-null. Label must be non-null to display anything
  public static void show(String label, String path, String pwd, KeyStore ks, KeyManagerFactory kmf, SSLContext ctx) throws Exception {
    if (cons.bShow && label != null) {
      blk();
      ln("==============" + label + "=============");
      ln("PATH: " + path + "  PWD: " + pwd);

      if (ks != null) {
        blk();
        ln("Keystore -- " + ks.toString());
        ln("--------------------------------------");
        ln("    Type -- " + ks.getType());
        ln("Provider -- " + ks.getProvider());
        ln("    Size -- " + ks.size());
        if(cons.bDetails) {
          Enumeration<String> e = ks.aliases();
          while (e.hasMoreElements()) {
            String elem = e.nextElement();
            ln("  Alias: " + elem);
            ln("         " + ks.getCreationDate(elem).toString());

          }
        }
        blk();
      }

      if(kmf!=null){
        blk();
        ln("Key Manager Factory -- " + kmf.toString());
        ln("--------------------------------------");
      }
    }
  }

  public static void trace(String s) { cons.trace(s); }
  public static void ln(String s)    { cons.ln(s); }
  public static void blk()           { cons.blk(); }
}