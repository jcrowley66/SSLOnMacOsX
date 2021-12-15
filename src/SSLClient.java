
import javax.net.ssl.*;

/** See usage and other comments in SSLServer */
public class SSLClient {

  private static AllCommon common = new AllCommon();
  private static String    label  = "SSL CLIENT";

  public static void main(String[] args) throws Exception {
    common.setupTrustStore(false);
    try {
      if(args.length > 0){
        common.host = args[0];
        if(args.length > 1)
          common.port = Integer.parseInt(args[1]);
      }
      common.showStart(label);
      SSLSocket socket   = getSocket(common.host, common.port);
      SSLSession session = socket.getSession();
      trc();
      trace("got a socket connection");
      trace("           Protocol: " + session.getProtocol());
      trace("       Cipher Suite: " + session.getCipherSuite());
      trc();

      Thread thrdWrite = new Thread( new SSL_RW("CLIENT", false, socket));
      thrdWrite.start();
      Thread thrdRead  = new Thread( new SSL_RW("CLIENT", true, socket));
      thrdRead.start();

    } catch(Exception e) { common.err(label, "Exception -- " + e.toString()); }
  }
  public static SSLSocket getSocket(String ip, int port) throws Exception{
    SSLContext ctx   = common.getSSLContext(false);
    SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket(ip, port);
    socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
    trace("About to start handshake");
    socket.startHandshake();
    trace("Returned from handshake");
    return socket;
  }

  public static void trace(String s) { common.trace(label, s); }
  public static void trc() { common.blktrc(); }
}