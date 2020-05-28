package cs455.overlay.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import cs455.overlay.node.Node;

public class TCPServerThread implements Runnable{
	
	//Class variables
	ServerSocket server;
	InetAddress ip;
	Node node;
	
	//Constructor that takes a port number in string and tries to start a server socket on that port 
	public TCPServerThread(String portnum, Node node) {
		this.node = node;
		try {
			server = new ServerSocket(Integer.parseInt(portnum), 128);
			ip = server.getInetAddress();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getServerIP() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
	
	public int getServerPort() {
		return server.getLocalPort();
	}

	//Thread implementation that allows server socket to listen for incoming connection requests
	@SuppressWarnings("resource")
	public void run() {
		while(true) {
			try {
				Socket socket = server.accept();
				TCPConnection tcpConnection = new TCPConnection(socket, node);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
