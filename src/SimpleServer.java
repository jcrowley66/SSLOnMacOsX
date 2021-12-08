import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Enumeration;
import javax.net.*;
import javax.net.ssl.*;

/** SERVER side of a simple client/server SSL connection.
 *
 * Each side just reads lines from the console and passes them to the other side of the connection (and then prints).
 *
 * This runs on a Mac OS X (Catalina 10.15.7) using Java 1.8.0_311, and the key concept is that the JDK includes a Provider
 * which uses the Mac Keychain(s) to provide the necessary certificates, as follows:
 *
 *          KeyStore.getInstance("KeychainStore");
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
 * The JavaSSL JAR may be used to run each side
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
      kmf.init(ks, null);
      ctx.init(kmf.getKeyManagers(), null, null);
      if(cons.bServer) show("SERVER KeyStore", "Using Mac Keychain", "No password", ks, kmf, ctx);

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
        ln("  Provider -- " + kmf.getProvider());
        ln("# Key Mgrs -- " + kmf.getKeyManagers().length);
        if(cons.bDetails){
          KeyManager[] mgrs = kmf.getKeyManagers();
          String[] strMgrs  = new String[mgrs.length];
          for( int i=0; i<mgrs.length; i++){
            KeyManager mgr = mgrs[i];
            strMgrs[i] = "Type: " + mgr.getClass().getSimpleName() + " -- " + mgr.getClass().getName();
          }
          listArray("   KeyManager", strMgrs);
        }
      }

      if(ctx != null){
        blk();
        ln("SSLContext -- " + ctx.toString());
        ln("----------------------------------");
        ln("   Provider -- " + ctx.getProvider());
        ln("   Protocol -- " + ctx.getProtocol());
        if(cons.bDetails){
          SSLParameters params = ctx.getSupportedSSLParameters();
                       ln("   Need Client Auth -- " + params.getNeedClientAuth());
                       ln("   Want Client Auth -- " + params.getWantClientAuth());
          String[] sArray= params.getProtocols();
          listArray("          Protocol", sArray);
          sArray = params.getApplicationProtocols();
          listArray("     App Protocols", sArray);
          sArray = params.getCipherSuites();
          listArray("           Ciphers", sArray);
        }
      }
    }
  }

  private static String allBlanks = "                                                       ";

  private static void listArray(String labelParam, String[] array) {
    String label  = labelParam;
    String blanks = allBlanks.substring(0, label.length());
    for(int i=0; i<array.length; i++){
      ln(label + " -- [" + i + "] " + array[i]);
      label = blanks;
    }
  }

  public static void trace(String s) { cons.trace(s); }
  public static void ln(String s)    { cons.ln(s); }
  public static void blk()           { cons.blk(); }
}