public class SimpleConsts {
  public static boolean bShow     = true;
  public static boolean bDetails  = bShow && true;
  public static boolean bClient   = bShow && true;
  public static boolean bServer   = bShow && true;

  public  static String host = "localhost";
  public  static int    port = 9100;

  public static void ln(String s) { System.out.println(s); }
  public static void blk() { ln(""); }
}
