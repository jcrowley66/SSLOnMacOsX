public class SimpleConsts {
  public static boolean traceOn   = false;       // TRUE to trace setup path, FALSE to just show prompts & data received

  public static boolean bDebug    = false;      // TRUE for the debug(String s) method to display output

  public static boolean bShow     = false;          // TRUE to activate the show(...) method to print details of the connection
  public static boolean bClient   = bShow && true;  // TRUE to activate show(...) on the Client
  public static boolean bServer   = bShow && true;  // TRUE to activate show(...) on the Server
  public static boolean bDetails  = bShow && true;  // TRUE to show details -- e.g. all protocols, cipher suites, etc

  public  static String host = "localhost";         // IP for the Server
  public  static int    port = 51000;                // Port to use

  /** Trace of processing controlled by the 'traceOn' switch above */
  public static void trace(String s)  { if(traceOn) ln(s); }
  public static void trc()            { trace(""); }

  public static void ln(String s)     { System.out.println(s); }
  public static void blk()            { ln(""); }
}
