# SSLOnMacOsX
This is an example of a low-level SSL connection on a Mac OS X (Catalina 10.15.7) using the Mac **Keychains**.

An SSLServerSocket and an SSLSocket are created using the Mac Keychains as the source of the certificates needed to validate the connection.
These then connect and exchange simple messages (entered from the console). Entering QUIT on either side terminates both server and client.

Could not find any example of this basic, simple case. The JSSE and Java SSL documentation is voluminous, complex, and almost impossible to navigate.

Finally cracked the problem, and this POC starts an SSLServerSocket and puts up an accept() to receive a connection from a client. The SSLSocket client then opens a socket to make the connection.

Simple messages are entered on the console of either Server or Client, passed to the other end of the connection, and printed by the receiver.

The key breakthrough was finding that the JDK includes a Provider which will use the Mac Keychains:

<h4 align="center">KeyStore.getInstance("KeychainStore")</h4>

See: https://docs.oracle.com/javase/10/security/oracle-providers.htm#JSSEC-GUID-3185649A-C316-45F2-A70E-2B3FF6BDC34F

**Note:** If prompted for your password in order to access the Keychain, select the *Always Allow* button. If running from a terminal (as opposed to an IDE) this is more complicated - TBD.


See the comments at the top of SimpleServer for more details.