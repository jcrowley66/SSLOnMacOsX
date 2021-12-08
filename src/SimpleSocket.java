import java.security.KeyStore;
import javax.net.ssl.*;

public class SimpleSocket {

    private static SimpleConsts cons = new SimpleConsts();

    public static SSLSocket getSocket(String ip, int port, String pathToCerts, String pwdIn) throws Exception{
        SSLContext ctx;
        KeyManagerFactory kmf;
        KeyStore ks;
        SSLSocket socket;
        char[] pwd = pwdIn==null ? null : pwdIn.toCharArray();

        ctx = SSLContext.getInstance("TLS");
        kmf = KeyManagerFactory.getInstance("SunX509");
        ks = KeyStore.getInstance("KeychainStore");
        ks.load(null, null);

        kmf.init(ks, pwd);
        ctx.init(kmf.getKeyManagers(), null, null);

        if(cons.bClient)
            SimpleServer.show("SimpleSocket", pathToCerts, pwdIn, ks, kmf, ctx);

        socket = (SSLSocket) ctx.getSocketFactory().createSocket(ip, port);

        ln("About to start handshake");
        socket.startHandshake();
        ln("Returned from handshake");
        return socket;
    }

    public static void ln(String s) { cons.ln(s); }
    public static void blk() { cons.blk(); }
}