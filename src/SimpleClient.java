
import java.security.KeyStore;
import javax.net.ssl.*;

public class SimpleClient {

  private static SimpleCommon common = new SimpleCommon();

  public static void main(String[] args) throws Exception {
    common.setupTrustStore(false);
    common.showStart("CLIENT");
    try {
      SSLSocket socket   = getSocket(common.host, common.port);
      SSLSession session = socket.getSession();
      trc();
      trace("CLIENT got a socket connection");
      trace("           Protocol: " + session.getProtocol());
      trace("       Cipher Suite: " + session.getCipherSuite());
      trc();

      Thread thrdWrite = new Thread( new SimpleRW("CLIENT", false, socket));
      thrdWrite.start();
      Thread thrdRead = new Thread( new SimpleRW("CLIENT", true, socket));
      thrdRead.start();

    } catch(Exception e) { common.err("CLIENT Exception -- " + e.toString()); }
  }
  public static SSLSocket getSocket(String ip, int port) throws Exception{
    SSLContext ctx;
    KeyManagerFactory kmf;
    KeyStore ks;
    SSLSocket socket;

    ctx = SSLContext.getInstance("TLSv1.3");
    kmf = KeyManagerFactory.getInstance("SunX509");
    ks  = KeyStore.getInstance(common.keystoreName);
    ks.load(common.keystoreInStrm, common.keystorePwd);

    kmf.init(ks, null);
    ctx.init(kmf.getKeyManagers(), null, null);

    if(common.bClient) common.show("SimpleSocket", ks, kmf, ctx);

    socket = (SSLSocket) ctx.getSocketFactory().createSocket(ip, port);
    socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
    trace("About to start handshake");
    socket.startHandshake();
    trace("Returned from handshake");
    return socket;
  }

  public static void trace(String s) { common.trace(s); }
  public static void trc() { common.blktrc(); }
}