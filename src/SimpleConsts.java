public class SimpleConsts {
  public static boolean traceOn   = true;       // TRUE to trace setup path, FALSE to just show prompts & data received

  public static boolean bDebug    = false;

  public static boolean bShow     = false;      // TRUE to activate the show(...) method to print details of the connection
  public static boolean bDetails  = bShow && true;
  public static boolean bClient   = bShow && true;
  public static boolean bServer   = bShow && true;

  public  static String host = "localhost";
  public  static int    port = 9100;

  /** Trace of processing controlled by the 'traceOn' switch above */
  public static void trace(String s)  { if(traceOn) ln(s); }
  public static void trc()            { trace(""); }

  public static void ln(String s)     { System.out.println(s); }
  public static void blk()            { ln(""); }
}
