
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class SimpleClient {

  private static SimpleConsts cons = new SimpleConsts();

  public static void main(String[] args) throws Exception {
    try {
      cons.showStart("CLIENT");
      SSLSocket socket = getSocket(cons.host, cons.port, "Uses Mac Keychain", "No Password");
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

    } catch(Exception e) { cons.ln("CLIENT Exception -- " + e.toString()); }
  }
  public static SSLSocket getSocket(String ip, int port, String pathToCerts, String pwdIn) throws Exception{
    SSLContext ctx;
    KeyManagerFactory kmf;
    KeyStore ks;
    SSLSocket socket;
    char[] pwd = pwdIn==null ? null : pwdIn.toCharArray();

    ctx = SSLContext.getInstance("TLSv1.3");
    kmf = KeyManagerFactory.getInstance("SunX509");
    ks = KeyStore.getInstance("KeychainStore");
    ks.load(null, null);

    kmf.init(ks, pwd);
    ctx.init(kmf.getKeyManagers(), null, null);

    if(cons.bClient) cons.show("SimpleSocket", pathToCerts, pwdIn, ks, kmf, ctx);

    socket = (SSLSocket) ctx.getSocketFactory().createSocket(ip, port);
    socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
    trace("About to start handshake");
    socket.startHandshake();
    trace("Returned from handshake");
    return socket;
  }

  public static void trace(String s) { cons.trace(s); }
  public static void trc() { cons.trc(); }
}