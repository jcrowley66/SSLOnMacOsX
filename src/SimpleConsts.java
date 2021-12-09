import javax.net.ssl.*;
import java.security.KeyStore;
import java.util.Enumeration;

public class SimpleConsts {
  public static boolean traceOn   = false;       // TRUE to trace setup path, FALSE to just show prompts & data received

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
  public static void trc()            { trace(""); }

  public static void ln(String s)     { System.out.println(s); }
  public static void blk()            { ln(""); }

  // Show environment at startup - Java version, etc
  public static void showStart(String label){
    if(label!=null){
      trace("==================================");
      trace("     Starting " + label);
      trace("==================================");
      trace("        Host:port: " + host + ":" + port);
      trace("     Java Version: " + System.getProperty("java.version"));
      trace("Java Spec Version: " + System.getProperty("java.specification.version"));
      trace("   Java Class Vsn: " + System.getProperty("java.class.version"));
      if(bStartDtl) {
        listArray("  Java Class Path: ", System.getProperty("java.class.path").split(":"));
        listArray("         Lib Path: ", System.getProperty("java.library.path").split(":"));
      }
      trace("==================================");
    }
  }
  // Display all the status for any that are non-null. Label must be non-null to display anything
  public static void show(String label, String path, String pwd, KeyStore ks, KeyManagerFactory kmf, SSLContext ctx) throws Exception {
    if (bShow && label != null) {
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

//  def sysClassMajorMinor(of:Any):(Int, Int) = {
//    // NOTE: Frustrating since this must already be in memory for the class, but could not find any way to
//    //       access the version info without doing I/O to read it!!!!
//    val clazz = of.getClass
//    val name  = clazz.getName.replace(".", "/") + ".class"
//    var rslt  = if(mapMajorMinor.contains(name)) mapMajorMinor.get(name) else 0
//    if(rslt == 0){
//      // Format of class file us U4(magic number)U2(major version)U2(minor version) - ASSUME version numbers always < 1000
//      val bytes = new Array[Byte](4)
//      val strm = new BufferedInputStream(clazz.getClassLoader.getResourceAsStream(name))
//      strm.read(bytes)        // Skip initial magic number
//      strm.read(bytes)        // read U2 for minor, U2 for major version - pick up each from a single byte
//      strm.close
//          rslt = bytes(3) * 1000 + bytes(1)
//      mapMajorMinor.put(name, rslt)
//    }
//    (rslt/1000, rslt%1000)
//  }

}
