# SSLOnMacOsX
This is an example on a Mac OS X (10.15.7) of an SSL connection.

An SSLServerSocket and an SSLSocket are created using the Keychains to provide the certificates needed to validate the connection.

These then connect and exchange simple messages (entered from the console).

The driving force was that could not find any example of this basic, simple case. The JSSE and Java SSL documentation is voluminous, complex, and almost impossible to navigate.

Finally cracked the problem, and this is a trivial case which starts an SSLServerSocket and puts up an accept() to receive a connection from a client. The SSLSocket client then opens a socket to make the connection.

Simple messages are entered on the console and passed between server and client.