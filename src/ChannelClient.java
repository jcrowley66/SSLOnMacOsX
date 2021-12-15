
import javax.net.ssl.*;
import java.net.InetSocketAddress;
import java.net.*;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/** See usage and other comments in SSLServer */
public class ChannelClient {

  private static AllCommon common = new AllCommon();
  private static String    label  = "CHANNEL CLIENT";

  public static void main(String[] args) throws Exception {
    common.setupTrustStore(false);
    try {
      if(args.length > 0){
        common.host = args[0];
        if(args.length > 1)
          common.port = Integer.parseInt(args[1]);
      }
      common.showStart(label);
      SocketChannel channel   = getSocket(common.host, common.port);
      channel.configureBlocking(false);
      Socket skt = channel.socket();
      common.ln("skt " + skt.getClass().getName());
      trc();
      trace("got a socket connection");
      trc();

      Thread thrdWrite = new Thread( new ChannelRW(label, false, channel));
      thrdWrite.start();
      Thread thrdRead  = new Thread( new ChannelRW(label, true, channel));
      thrdRead.start();

    } catch(Exception e) { common.err(label, "Exception -- " + e.toString()); }
  }
  public static SocketChannel getSocket(String ip, int port) throws Exception {
    SSLContext ctx       = common.getSSLContext(false);
    SocketChannel socket = SocketChannel.open();
    socket.connect(new InetSocketAddress(ip, port));
    socket.configureBlocking(false);
    if(common.traceOn){
      Set<SocketOption<?>> options = socket.supportedOptions();
      Iterator<?> it = options.iterator();
      while(it.hasNext()) trace("Option: " + it.next().toString());
    }
//    socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
//    trace("About to start handshake");
//    socket.startHandshake();
//    trace("Returned from handshake");
    return socket;
  }

  public static void trace(String s) { common.trace(label, s); }
  public static void trc() { common.blktrc(); }
}