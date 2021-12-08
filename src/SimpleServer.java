import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.Enumeration;
import javax.net.*;
import javax.net.ssl.*;

public class SimpleServer implements Runnable {

  private static SimpleConsts cons = new SimpleConsts();

  private ServerSocket sktSvr;
  public SimpleServer(ServerSocket ss) {
    sktSvr = ss;
  }
  public static void main(String args[]) {
    String type = "TLS1.3";
    try {
      ln("SERVER starting -- Java: " + System.getProperty("java.version"));
      ln("Getting ServerSocketFactory");
      ServerSocketFactory ssf = SimpleServer.getServerSocketFactory(type);
      ln("Creating ServerSocket");
      ServerSocket ss = ssf.createServerSocket();
      ((SSLServerSocket) ss).setNeedClientAuth(true);

      ss.bind(new InetSocketAddress(cons.host, cons.port));
      ln("Created SSLServerSocket -- Local Socket Address: " + ss.getLocalSocketAddress() + " InetAddress: " + ss.getInetAddress());
      new Thread( new SimpleServer(ss)).start();
    } catch (IOException e) {
      System.out.println("Unable to start ClassServer: " + e.getMessage());
      e.printStackTrace();
    }
  }
  // just loop accepting connections. For each, start a read & write thread.
  public void run() {
    try {
      Socket socket = sktSvr.accept();
      ln("SERVER -- socket was accepted");

      Thread thrdWrite = new Thread( new SimpleRW("SERVER", false, socket));
      thrdWrite.start();
      Thread thrdRead = new Thread( new SimpleRW("SERVER", true, socket));
      thrdRead.start();

      while(thrdWrite.isAlive() || thrdWrite.isAlive())
        Thread.sleep(200);
      System.exit(0);

    } catch(Exception e) { ln("SERVER Exception on Accept -- " + e.toString());}
  }

  private static ServerSocketFactory getServerSocketFactory(String type) {
    if (type.equals("TLS1.3")) {
      SSLServerSocketFactory ssf = null;
      try {
        // set up key manager to do server authentication
        SSLContext ctx;
        KeyManagerFactory kmf;
        KeyStore ks;

        ctx = SSLContext.getInstance("TLS");
        kmf = KeyManagerFactory.getInstance("SunX509");
        ks = KeyStore.getInstance("KeychainStore");
        ks.load(null, null);
        if(cons.bServer) show("SERVER KeyStore", "Using Mac Keychain", "No password", ks, null, null);
        kmf.init(ks, null);
        ctx.init(kmf.getKeyManagers(), null, null);

        ssf = ctx.getServerSocketFactory();
        return ssf;
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      return ServerSocketFactory.getDefault();
    }
    return null;
  }

  // Display all the status for any that are non-null. Label must be non-null to display anything
  public static void show(String label, String path, String pwd, KeyStore ks, KeyManagerFactory kmf, SSLContext ctx) throws Exception {
    if (label != null) {
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
        if(cons.bDetails) {
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
      }
    }
  }

  public static void ln(String s) { cons.ln(s); }
  public static void blk() { cons.blk(); }
}