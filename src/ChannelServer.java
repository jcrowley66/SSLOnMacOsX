import java.net.*;
import java.io.IOException;
import java.nio.channels.*;
import java.util.*;

/** Same as SSLServer but uses the CHANNEL logic to create a SocketChannel to do non-blocking I/O */
public class ChannelServer implements Runnable {

  private static AllCommon common = new AllCommon();
  private static String label        = "CHANNEL SERVER";

  private ServerSocketChannel sktSvr;
  public ChannelServer(ServerSocketChannel ss) {
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
      common.showStart(label);
      trace(label, "Getting ServerSocketChannel");
      ServerSocketChannel ssc = ServerSocketChannel.open();
      ssc.bind(new InetSocketAddress(common.host, common.port));
      if(common.traceOn){
        Set<SocketOption<?>> options = ssc.supportedOptions();
        Iterator<?> it = options.iterator();
        while(it.hasNext()) trace(label, "Option: " + it.next().toString());
      }

      trace("Created ServerSocketChannel -- Local Socket Address: " + ssc.getLocalAddress());
      new Thread( new ChannelServer(ssc)).start();
    } catch (IOException e) {
      common.err(label, "Unable to start ClassServer: " + e.getMessage());
      e.printStackTrace();
    }
  }
  // just loop accepting connections. For each, start a read & write thread.
  public void run() {
    try {
      SocketChannel socket = sktSvr.accept();
      trace(label,"socket was accepted");
      socket.configureBlocking(false);

      Thread thrdWrite = new Thread( new ChannelRW("SERVER", false, socket));
      thrdWrite.start();
      Thread thrdRead = new Thread( new ChannelRW("SERVER", true, socket));
      thrdRead.start();
    } catch(Exception e) { common.err(label, "Exception on Accept -- " + e.toString());}
  }

  public static void trace(String s) { common.trace(s); }
  public static void trace(String label, String s) { common.trace(label, s); }
  public static void ln(String s)    { common.ln(s); }
  public static void blk()           { common.blk(); }
}