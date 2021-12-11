import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.util.Enumeration;
import org.apache.commons.lang3.SystemUtils;

public class SimpleCommon {
  public static boolean traceOn   = true;       // TRUE for the trace(String s) method to display output, FALSE to suppress

  public static boolean bDebug    = false;      // TRUE for the debug(String s) method to display output

  public static boolean bStartDtl = false;

  public static boolean bShow     = false;          // TRUE to activate the show(...) method to print details of the connection
  public static boolean bClient   = bShow && true;  // TRUE to activate show(...) on the Client
  public static boolean bServer   = bShow && true;  // TRUE to activate show(...) on the Server
  public static boolean bDetails  = bShow && true;  // TRUE to show details -- e.g. all protocols, cipher suites, etc

  public  static String host = "localhost";         // IP for the Server
  public  static int    port = 51000;               // Port to use

  /** Trace of processing controlled by the 'traceOn' switch above */
  public static void trace(String s)  { if(traceOn) ln(s); }
  public static void blktrc()         { trace(""); }

  public static void ln(String s)     { System.out.println(s); }
  public static void blk()            { ln(""); }

  public static void err(String s)    { ln("ERROR: " + s); }        // Errors never suppressed

  public static String javaHome    = System.getProperty("java.home");
  public static String cacertsPath = javaHome + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";

  // Following must all be initialized by setupTrustStore method!
  public static String      keystoreName    = null;
  public static char[]      keystorePwd     = null;
  public static InputStream keystoreInStrm  = null;

  /** KEY SETUP - sets up appropriate KeyStore for Mac or Windows
  */
  public static void setupTrustStore(boolean forServer) {
    if(SystemUtils.IS_OS_MAC){
      // NOTE: Tried the standard Java 'cacerts' on both sides and did NOT work
      //       Tried 'cacerts' on Server side, KeychainStore on Client side - did NOT work
      //       KeychainStore on both sides DOES WORK
      //    System.setProperty("javax.net.ssl.trustStore", cacertsPath);
      System.setProperty("javax.net.ssl.trustStoreType", "KeychainStore");
      keystoreName = "KeychainStore";
    } else if(SystemUtils.IS_OS_WINDOWS) {
      // https://github.com/gradle/gradle/issues/6584
      // javax.net.ssl.trustStore=C:\\Windows\\win.ini
      // javax.net.ssl.trustStoreType=Windows-ROOT
      System.setProperty("javax.net.ssl.trustStore", "C:\\Windows\\win.ini" );
      System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
      keystoreName = "Windows-ROOT";
    } else
      err("NOT YET IMPLEMENTED for " + SystemUtils.OS_NAME);
  }
  // Show environment at startup - Java version, etc
  public static void showStart(String label){
    if(label!=null){
      trace("==================================");
      trace("     Starting " + label);
      trace("==================================");
      trace("        Host:port: " + host + ":" + port);
      trace("               OS: " + System.getProperty("os.name"));
      trace("       OS Version: " + System.getProperty("os.version"));
      trace("          OS Arch: " + System.getProperty("os.arch"));
      trace("     Java Version: " + System.getProperty("java.version"));
      trace("Java Spec Version: " + System.getProperty("java.specification.version"));
      trace("   Java Class Vsn: " + System.getProperty("java.class.version"));
      trace("        Java Home: " + javaHome);
      trace("       TrustStore: " + System.getProperty("javax.net.ssl.trustStore"));
      trace("   TrustStoreType: " + System.getProperty("javax.net.ssl.trustStoreType"));
      if(bStartDtl) {
        listArray("  Java Class Path: ", System.getProperty("java.class.path").split(":"));
        listArray("         Lib Path: ", System.getProperty("java.library.path").split(":"));
      }
      trace("==================================");
    }
  }
  // Display all the status for any that are non-null. Label must be non-null to display anything
  public static void show(String label, KeyStore ks, KeyManagerFactory kmf, SSLContext ctx) throws Exception {
    if (bShow && label != null) {
      blk();
      ln("==============" + label + "=============");

      if (ks != null) {
        blk();
        ln("Keystore -- " + ks.toString());
        ln("--------------------------------------");
        ln("    Type -- " + ks.getType());
        ln("Provider -- " + ks.getProvider());
        ln("    Size -- " + ks.size());
        if(bDetails) {
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
        if(bDetails){
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
        if(bDetails){
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
      ln(label + " -- [" + (i<10 ? " " + i : i) + "] " + array[i]);
      label = blanks;
    }
  }

}
